package md.logica;

import java.util.Optional;

import md.modelo.Ubicacion;
import md.rutas.api.PlanificadorRutas;

/**
 * Valida y ajusta ubicaciones geográficas dentro del área de cobertura.
 *
 * Utiliza los límites geográficos y la tolerancia de ajuste recibidos
 * durante su construcción, junto con un motor vial para encontrar
 * calles transitables cercanas.
 */
public final class ValidacionUbicaciones {

    private final double toleranciaAjusteMetros;
    private final double latitudMin;
    private final double latitudMax;
    private final double longitudMin;
    private final double longitudMax;

    /**
     * Construye un validador con los límites geográficos y la tolerancia
     * de ajuste especificados.
     *
     * @param toleranciaAjusteMetros distancia máxima para ajustar una ubicación.
     * @param latitudMin             latitud mínima permitida.
     * @param latitudMax             latitud máxima permitida.
     * @param longitudMin            longitud mínima permitida.
     * @param longitudMax            longitud máxima permitida.
     * @throws IllegalArgumentException si la tolerancia o los límites geográficos
     *                                  no son válidos.
     */
    public ValidacionUbicaciones(
            double toleranciaAjusteMetros,
            double latitudMin,
            double latitudMax,
            double longitudMin,
            double longitudMax) {

        if (!Double.isFinite(toleranciaAjusteMetros) || toleranciaAjusteMetros <= 0) {
            throw new IllegalArgumentException("La tolerancia de ajuste debe ser positiva.");
        }

        if (!Double.isFinite(latitudMin) || !Double.isFinite(latitudMax)
                || !Double.isFinite(longitudMin) || !Double.isFinite(longitudMax)
                || latitudMin < -90 || latitudMax > 90
                || longitudMin < -180 || longitudMax > 180
                || latitudMin >= latitudMax || longitudMin >= longitudMax) {
            throw new IllegalArgumentException("Los límites geográficos no son válidos.");
        }

        this.toleranciaAjusteMetros = toleranciaAjusteMetros;
        this.latitudMin = latitudMin;
        this.latitudMax = latitudMax;
        this.longitudMin = longitudMin;
        this.longitudMax = longitudMax;
    }

    /**
     * Valida una ubicación y la ajusta a la calle transitable más cercana.
     *
     * Primero comprueba que las coordenadas sean válidas y estén dentro del
     * área de cobertura. Luego solicita al motor vial el ajuste utilizando
     * la tolerancia recibida durante la construcción y vuelve a validar
     * las coordenadas obtenidas.
     *
     * @param candidata ubicación elegida por el usuario o generada.
     * @param motorVial motor de rutas utilizado para ajustar la ubicación.
     * @return ubicación ajustada y validada.
     * @throws IllegalArgumentException si algún parámetro es null, las
     *                                  coordenadas son inválidas, están fuera
     *                                  del área o no se encuentra una calle
     *                                  cercana.
     * @throws IllegalStateException    si el motor vial devuelve un resultado null.
     */
    public Ubicacion validarYAjustar(
            Ubicacion candidata,
            PlanificadorRutas motorVial) {

        if (candidata == null || motorVial == null) {
            throw new IllegalArgumentException(
                    "La ubicación y el motor vial son obligatorios.");
        }

        validarCoordenadas(candidata);

        validarDentroDelArea(candidata);

        double tolerancia = toleranciaAjusteMetros;

        Optional<Ubicacion> resultado = motorVial.ajustarACalle(candidata, tolerancia);

        if (resultado == null) {
            throw new IllegalStateException("El motor vial devolvió un resultado inválido.");
        }

        Ubicacion ajustada = resultado.orElseThrow(() -> new IllegalArgumentException(
                "No se encontró una calle cercana a la ubicación."));

        validarCoordenadas(ajustada);
        validarDentroDelArea(ajustada);

        return ajustada;
    }

    /**
     * Comprueba que las coordenadas de una ubicación sean válidas
     * según los rangos geográficos permitidos.
     *
     * @param ubicacion ubicación cuyas coordenadas se desean validar.
     * @throws IllegalArgumentException si la latitud o longitud no son finitas
     *                                  o están fuera de sus rangos permitidos.
     */
    private static void validarCoordenadas(Ubicacion ubicacion) {

        double latitud = ubicacion.getLatitud();
        double longitud = ubicacion.getLongitud();

        if (!Double.isFinite(latitud) ||
                latitud < -90 || latitud > 90) {
            throw new IllegalArgumentException("Latitud inválida.");
        }

        if (!Double.isFinite(longitud) ||
                longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("Longitud inválida.");
        }
    }

    /**
     * Comprueba que una ubicación se encuentre dentro del área de cobertura.
     *
     * @param ubicacion ubicación que se desea comprobar.
     * @throws IllegalArgumentException si la ubicación está fuera del área.
     */
    private void validarDentroDelArea(Ubicacion ubicacion) {

        boolean fueraDelArea = ubicacion.getLatitud() < latitudMin ||
                ubicacion.getLatitud() > latitudMax ||
                ubicacion.getLongitud() < longitudMin ||
                ubicacion.getLongitud() > longitudMax;

        if (fueraDelArea) {
            throw new IllegalArgumentException("La ubicación está fuera del área de cobertura.");
        }
    }
}