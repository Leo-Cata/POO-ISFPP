package md.simulacion;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Acumula los resultados de la simulación: viajes terminados, viajes rechazados,
 * kilómetros recorridos e ingresos.
 *
 * Usa contadores atómicos, así que se puede llamar desde cualquier hilo.
 */
public class Estadisticas {

    private final AtomicInteger viajesFinalizados = new AtomicInteger();
    private final AtomicInteger viajesRechazados = new AtomicInteger();
    private final DoubleAdder kmRecorridos = new DoubleAdder();
    private final DoubleAdder ingresos = new DoubleAdder();

    /**
     * Registra un viaje terminado.
     *
     * @param km    kilómetros recorridos (acercamiento + traslado).
     * @param costo importe cobrado por el viaje.
     */
    public void registrarViajeFinalizado(double km, double costo) {
        viajesFinalizados.incrementAndGet();
        kmRecorridos.add(km);
        ingresos.add(costo);
    }

    /** Registra un viaje que no encontró conductor a tiempo o se cerró sin atender. */
    public void registrarRechazo() {
        viajesRechazados.incrementAndGet();
    }

    public int getViajesFinalizados() {
        return viajesFinalizados.get();
    }

    public int getViajesRechazados() {
        return viajesRechazados.get();
    }

    public double getKmRecorridos() {
        return kmRecorridos.sum();
    }

    public double getIngresos() {
        return ingresos.sum();
    }

    /** Texto listo para mostrar al final de la simulación. */
    public String resumen() {
        return String.format("Viajes finalizados: %d%nViajes rechazados: %d%nKm recorridos: %.2f%nIngresos: $%.2f",
                getViajesFinalizados(), getViajesRechazados(), getKmRecorridos(), getIngresos());
    }
}
