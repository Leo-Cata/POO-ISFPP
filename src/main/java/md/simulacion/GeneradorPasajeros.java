package md.simulacion;

import java.util.List;
import java.util.Random;

import md.logica.GestorViajes;
import md.modelo.Servicio;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Viaje;

/**
 * Hilo productor. Toma un pasajero libre, arma un viaje con un servicio y un
 * recorrido al azar, y se lo entrega al Despachador. Entre pedido y pedido hace
 * una pausa. Cuando todos los pasajeros están en viaje, espera sin consumir CPU.
 */
public class GeneradorPasajeros implements Runnable {

    private final List<Usuario> pasajeros;
    private final List<Servicio> servicios;
    private final Despachador despachador;
    private final GeneradorUbicacionesViales generadorUbicaciones;
    private final long intervaloMs;
    private final GestorViajes cotizador = new GestorViajes();
    private final Random random = new Random();

    /**
     * @param pasajeros            usuarios sin perfil de conductor.
     * @param servicios            servicios que se pueden pedir.
     * @param despachador          quien recibe los viajes.
     * @param generadorUbicaciones genera origen y destino sobre calles.
     * @param intervaloMs          pausa entre dos pedidos.
     */
    public GeneradorPasajeros(List<Usuario> pasajeros, List<Servicio> servicios, Despachador despachador,
                              GeneradorUbicacionesViales generadorUbicaciones, long intervaloMs) {
        if (pasajeros == null || pasajeros.isEmpty() || servicios == null || servicios.isEmpty()
                || despachador == null || generadorUbicaciones == null) {
            throw new IllegalArgumentException("Faltan pasajeros, servicios o dependencias.");
        }
        this.pasajeros = pasajeros;
        this.servicios = servicios;
        this.despachador = despachador;
        this.generadorUbicaciones = generadorUbicaciones;
        this.intervaloMs = intervaloMs;
    }

    @Override
    public void run() {
        try {
            Usuario pasajero;
            while ((pasajero = despachador.esperarPasajeroLibre(pasajeros)) != null) {
                Viaje viaje = crearViaje(pasajero);
                if (viaje != null) {
                    despachador.solicitar(viaje);
                }
                Thread.sleep(intervaloMs);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Arma el viaje sin tocar estado compartido. Devuelve null si no encuentra un punto vial válido. */
    private Viaje crearViaje(Usuario pasajero) {
        try {
            Servicio servicio = servicios.get(random.nextInt(servicios.size()));
            Ubicacion origen = generadorUbicaciones.generar();
            Ubicacion destino = generadorUbicaciones.generar();

            GestorViajes.Cotizacion cotizacion = cotizador.calcularCotizacion(servicio, origen, destino);

            Viaje viaje = new Viaje(pasajero, origen, destino, servicio);
            viaje.setCostoTotal(cotizacion.costoTotal());
            viaje.setDuracionEstimadaMinutos(cotizacion.duracionMinutos());
            viaje.setDistanciaTotal(cotizacion.distanciaKm());
            return viaje;
        } catch (IllegalStateException e) {
            // No se encontró un punto vial válido: se descarta este pedido
            return null;
        }
    }
}
