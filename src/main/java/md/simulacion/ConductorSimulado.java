package md.simulacion;

import md.modelo.Usuario;
import md.modelo.Viaje;
import md.rutas.api.RutasViaje;

/**
 * Hilo de un conductor. Espera a que el Despachador le asigne un viaje y para
 * cada
 * uno:
 * 
 * recorre el acercamiento: desde donde está hasta el pasajero;
 * inicia el viaje (el pasajero sube);
 * recorre el traslado: desde el origen hasta el destino;
 * finaliza el viaje y vuelve a quedar disponible.
 * 
 */
public class ConductorSimulado implements Runnable {

    private final Usuario conductor;
    private final Despachador despachador;
    private final double factorAceleracion;

    public ConductorSimulado(Usuario conductor, Despachador despachador, double factorAceleracion) {
        if (conductor == null || despachador == null) {
            throw new IllegalArgumentException("El conductor y el despachador no pueden ser null.");
        }
        this.conductor = conductor;
        this.despachador = despachador;
        this.factorAceleracion = factorAceleracion;
    }

    @Override
    public void run() {
        try {
            Viaje viaje;
            while ((viaje = despachador.esperarAsignacion(conductor)) != null) {
                atender(viaje);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void atender(Viaje viaje) throws InterruptedException {
        try {
            RutasViaje rutas = despachador.obtenerRutas(viaje);
            if (rutas == null || viaje.getVehiculo() == null) {
                // No debería suceder: el despachador prepara la ruta antes de aceptar.
                despachador.cancelarViaje(viaje, conductor, "No se pudo obtener la ruta del viaje.");
                return;
            }

            RecorridoVehiculo recorrido = new RecorridoVehiculo(viaje.getVehiculo(), factorAceleracion);

            boolean llegóAlPasajero = recorrido.recorrer(
                    rutas.acercamiento(), () -> despachador.sigueActivo(viaje));
            if (!llegóAlPasajero) {
                return;
            }

            if (!despachador.iniciarViaje(viaje)) {
                return;
            }

            boolean llegóAlDestino = recorrido.recorrer(
                    rutas.traslado(), () -> despachador.sigueActivo(viaje));
            if (!llegóAlDestino) {
                return;
            }

            despachador.finalizarViaje(viaje);
        } finally {
            despachador.liberarAtencion(conductor, viaje);
        }
    }
}
