package md.rutas.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.PointList;

import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.rutas.api.PlanificadorRutas;
import md.rutas.api.PuntoRuta;
import md.rutas.api.RutaCalculada;
import md.rutas.api.RutaNoDisponibleException;

public class GraphHopperPlanificador implements PlanificadorRutas {

    private static final String PERFIL_AUTO = "car";
    private static final String PERFIL_MOTO = "motorcycle";

    private static final String VALOR_ACCESO_AUTO = "car_access";

    private final GraphHopper graphHopper;
    private final BooleanEncodedValue accesoAuto;

    private volatile boolean cerrado;

    /**
     * Construye e inicializa el motor vial a partir de un archivo OSM
     * y un directorio donde GraphHopper almacenará su grafo.
     *
     * @param archivoOsm archivo local con los datos viales.
     * @param directorioCache directorio de almacenamiento del grafo.
     */
    public GraphHopperPlanificador(String archivoOsm,String directorioCache) {

        if (archivoOsm == null || archivoOsm.isBlank() || directorioCache == null || directorioCache.isBlank()) {
            throw new IllegalArgumentException(
                    "El archivo OSM y el directorio de caché son obligatorios.");
        }

        Path rutaOsm = Path.of(archivoOsm).toAbsolutePath().normalize();

        Path rutaCache = Path.of(directorioCache).toAbsolutePath().normalize();

        if (!Files.isRegularFile(rutaOsm)) {
            throw new IllegalArgumentException(
                    "No se encontró el archivo OSM: " + rutaOsm);
        }

        try {
            Files.createDirectories(rutaCache);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo preparar el directorio de caché.", e);
        }

        GraphHopper motor = new GraphHopper();

        try {
            configurarMotor(motor, rutaOsm, rutaCache);

            // Importa el OSM la primera vez o carga el grafo existente.
            motor.importOrLoad();

            BooleanEncodedValue acceso = motor.getEncodingManager().getBooleanEncodedValue(VALOR_ACCESO_AUTO);

            this.graphHopper = motor;
            this.accesoAuto = acceso;

        } catch (RuntimeException e) {
            motor.close();

            throw new IllegalStateException("No se pudo inicializar GraphHopper.", e);
        }
    }

    /**
     * Configura los datos, valores de acceso y perfiles de transporte.
     */
    private void configurarMotor(GraphHopper motor,Path rutaOsm,Path rutaCache) {

        motor.setOSMFile(rutaOsm.toString());

        motor.setGraphHopperLocation(rutaCache.toString());

        // Valores adicionales requeridos por los modelos de automóvil
        // y motocicleta.
        motor.setEncodedValuesString("car_access, car_average_speed, track_type, surface");

        motor.setProfiles(new Profile(PERFIL_AUTO).setCustomModel(GHUtility.loadCustomModelFromJar("car.json")),
        				new Profile(PERFIL_MOTO).setCustomModel(GHUtility.loadCustomModelFromJar("motorcycle.json")));
    }

    /**
     * Calcula una ruta vial entre dos ubicaciones.
     *
     * @param origen ubicación inicial.
     * @param destino ubicación final.
     * @param tipoVehiculo tipo de vehículo que realizará el recorrido.
     * @return ruta calculada con distancia, duración y geometría.
     * @throws RutaNoDisponibleException si GraphHopper no puede obtener una ruta.
     */
    @Override
    public RutaCalculada calcular(Ubicacion origen,Ubicacion destino,TipoVehiculo tipoVehiculo)
            throws RutaNoDisponibleException {

        validarUbicacion(origen, "origen");
        validarUbicacion(destino, "destino");

        if (tipoVehiculo == null) {
            throw new IllegalArgumentException("El tipo de vehículo no puede ser null.");
        }

        asegurarAbierto();

        String perfil = obtenerPerfil(tipoVehiculo);

        GHRequest solicitud = new GHRequest(origen.getLatitud(),origen.getLongitud(),destino.getLatitud(),
                destino.getLongitud());

        solicitud.setProfile(perfil);
        solicitud.setLocale(Locale.forLanguageTag("es"));

        try {
            GHResponse respuesta = graphHopper.route(solicitud);

            if (respuesta.hasErrors()) {
                throw new RutaNoDisponibleException("GraphHopper no encontró una ruta: "+ respuesta.getErrors()
                );
            }

            ResponsePath camino = respuesta.getBest();

            PointList puntos = camino.getPoints();

            if (puntos == null || puntos.size() < 2) {
                throw new RutaNoDisponibleException("El recorrido no contiene una geometría válida.");
            }

            List<PuntoRuta> geometria =new ArrayList<>(puntos.size());

            for (int i = 0; i < puntos.size(); i++) {

                geometria.add(new PuntoRuta(puntos.getLat(i),puntos.getLon(i)));
            }

            return new RutaCalculada(camino.getDistance(),camino.getTime(),geometria);

        } catch (RuntimeException e) {
            throw new RutaNoDisponibleException("Error al calcular el recorrido vial.", e);
        }
    }

    /**
     * Ajusta una ubicación a una calle transitable dentro de la
     * distancia máxima permitida.
     *
     * @param candidata ubicación original.
     * @param maxDistanciaMetros distancia máxima admitida.
     * @return ubicación ajustada o Optional.empty() si no se encuentra.
     */
    @Override
    public Optional<Ubicacion> ajustarACalle(Ubicacion candidata,double maxDistanciaMetros) {

        validarUbicacion(candidata, "candidata");

        if (!Double.isFinite(maxDistanciaMetros) || maxDistanciaMetros < 0) {
            throw new IllegalArgumentException("La distancia máxima debe ser un número no negativo.");
        }

        asegurarAbierto();

        // Solo considera calles que admiten circulación de vehículos
        // motorizados, en al menos uno de sus sentidos.
        EdgeFilter filtroVehiculo = arista -> arista.get(accesoAuto) || arista.getReverse(accesoAuto);

        Snap ajuste = graphHopper.getLocationIndex().findClosest( candidata.getLatitud(),candidata.getLongitud(),
                		filtroVehiculo);

        if (!ajuste.isValid() || ajuste.getQueryDistance() > maxDistanciaMetros) {
            return Optional.empty();
        }

        // LocationIndexTree ya calcula el punto ajustado y la distancia.
        double latitud = ajuste.getSnappedPoint().lat;
        double longitud = ajuste.getSnappedPoint().lon;

        return Optional.of(new Ubicacion(latitud, longitud));
    }

    /**
     * Traduce el tipo del modelo al nombre del perfil GraphHopper.
     */
    private String obtenerPerfil(TipoVehiculo tipoVehiculo) {

        return switch (tipoVehiculo) {
            case AUTO -> PERFIL_AUTO;
            case MOTO -> PERFIL_MOTO;
        };
    }

    /**
     * Comprueba que la ubicación contenga coordenadas válidas.
     */
    private void validarUbicacion(Ubicacion ubicacion,String nombreParametro) {

        if (ubicacion == null) {
            throw new IllegalArgumentException("La ubicación " + nombreParametro + " no puede ser null.");
        }

        double latitud = ubicacion.getLatitud();
        double longitud = ubicacion.getLongitud();

        if (!Double.isFinite(latitud) || latitud < -90 || latitud > 90) {
            throw new IllegalArgumentException("La latitud de " + nombreParametro + " no es válida.");
        }

        if (!Double.isFinite(longitud) || longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("La longitud de " + nombreParametro + " no es válida.");
        }
    }

    private void asegurarAbierto() {
        if (cerrado) {
            throw new IllegalStateException("El motor de rutas ya fue cerrado.");
        }
    }

    /**
     * Libera los recursos del motor.
     */
    @Override
    public synchronized void close() {

        if (!cerrado) {
            graphHopper.close();
            cerrado = true;
        }
    }
}