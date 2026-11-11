package md.simulacion;

import md.modelo.Usuario;
import md.modelo.Viaje;
import md.rutas.api.RutasViaje;

/**
 * Hilo de un conductor. Espera a que el Despachador le asigne un viaje y para cada
 * uno:
 * 
 *   recorre el acercamiento: desde donde está hasta el pasajero;
 *   inicia el viaje (el pasajero sube);
 *   recorre el traslado: desde el origen hasta el destino;
 *   finaliza el viaje y vuelve a quedar disponible.
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
        RutasViaje rutas = viaje.getRutas();
        RecorridoVehiculo recorrido = new RecorridoVehiculo(viaje.getVehiculo(), factorAceleracion);

        recorrido.recorrer(rutas.acercamiento());
        despachador.iniciarViaje(viaje);

        recorrido.recorrer(rutas.traslado());
        despachador.finalizarViaje(viaje);

        System.out.printf("Viaje finalizado por %s: $%.2f, %.1f min estimados%n",
                conductor.getNombre(), viaje.getCostoTotal(), viaje.getDuracionEstimadaMinutos());
    }
}
