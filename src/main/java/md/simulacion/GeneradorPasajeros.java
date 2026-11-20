package md.simulacion;

import java.util.List;
import java.util.Random;

import md.logica.GeneradorUbicacionesViales;
import md.logica.GestorViajes;
import md.modelo.Servicio;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Viaje;

/**
 * Hilo productor. Toma un pasajero libre, arma un viaje con un servicio y un
 * recorrido al azar, y se lo entrega al Despachador. Entre pedido y pedido hace
 * una pausa. Cuando todos los pasajeros están en viaje, espera sin consumir
 * CPU.
 */
public class GeneradorPasajeros implements Runnable {

    private final List<Usuario> pasajeros;
    private final List<Servicio> servicios;
    private final Despachador despachador;
    private final GeneradorUbicacionesViales generadorUbicaciones;
    private final long intervaloMs;
    private final GestorViajes cotizador;
    private final Random random = new Random();
    private final GestorCancelaciones gestorCancelaciones;

    /**
     * @param pasajeros            usuarios sin perfil de conductor.
     * @param servicios            servicios que se pueden pedir.
     * @param despachador          quien recibe los viajes.
     * @param generadorUbicaciones genera origen y destino sobre calles.
     * @param intervaloMs          pausa entre dos pedidos.
     */
    public GeneradorPasajeros(
            List<Usuario> pasajeros,
            List<Servicio> servicios,
            Despachador despachador,
            GeneradorUbicacionesViales generadorUbicaciones,
            long intervaloMs,
            GestorViajes gestorViajes, GestorCancelaciones gestorCancelaciones) {
        if (pasajeros == null || pasajeros.isEmpty() || servicios == null || servicios.isEmpty()
                || despachador == null || generadorUbicaciones == null) {
            throw new IllegalArgumentException("Faltan pasajeros, servicios o dependencias.");
        }
        if (gestorViajes == null) {
            throw new IllegalArgumentException("El gestor de viajes no puede ser null.");
        }
        this.pasajeros = pasajeros;
        this.servicios = servicios;
        this.despachador = despachador;
        this.generadorUbicaciones = generadorUbicaciones;
        this.intervaloMs = intervaloMs;
        this.cotizador = gestorViajes;
        this.gestorCancelaciones = gestorCancelaciones;
    }

    @Override
    public void run() {
        try {
            Usuario pasajero;
            while ((pasajero = despachador.esperarPasajeroLibre(pasajeros)) != null) {
                crearViaje(pasajero);
                Thread.sleep(intervaloMs);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Arma el viaje y lo entrega al Despachador junto con su cotización.
     */
    private void crearViaje(Usuario pasajero) {
        try {
            Servicio servicio = servicios.get(random.nextInt(servicios.size()));
            Ubicacion origen = generadorUbicaciones.generar();
            Ubicacion destino = generadorUbicaciones.generar();

            GestorViajes.Cotizacion cotizacion = cotizador.calcularCotizacion(servicio, origen, destino);

            Viaje viaje = new Viaje(pasajero, origen, destino, servicio);
            despachador.solicitar(viaje, cotizacion);
            gestorCancelaciones.programar(viaje);
        } catch (IllegalStateException e) {
            // No se encontró un punto vial válido: se descarta este pedido
            // no se lanza la excepcion para que no se corte la simulacion
        }
    }
}
