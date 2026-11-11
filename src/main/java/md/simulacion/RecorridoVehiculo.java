package md.simulacion;

import java.util.List;
import java.util.function.BooleanSupplier;

import md.logica.BloqueoViajes;
import md.modelo.Ubicacion;
import md.modelo.Vehiculo;
import md.rutas.api.PuntoRuta;
import md.rutas.api.RutaCalculada;

/**
 * Mueve un vehículo a lo largo de una ruta calculada.
 *
 * Se ejecuta en el hilo que lo llama (el del conductor), no crea hilos propios.
 * En cada punto de la ruta actualiza la posición del vehículo y espera el tiempo
 * correspondiente, reducido por el factor de aceleración.
 */
public class RecorridoVehiculo {

    /** Pausa mínima entre dos puntos, para que la animación no sea imperceptible. */
    private static final long PASO_MINIMO_MS = 20;

    private final Vehiculo vehiculo;
    private final double factorAceleracion;

    /**
     * @param vehiculo           vehículo que se mueve.
     * @param factorAceleracion  cuántas veces más rápido que el tiempo real. Por ejemplo,
     *                           60 significa que 1 minuto de viaje dura 1 segundo.
     */
    public RecorridoVehiculo(Vehiculo vehiculo, double factorAceleracion) {
        if (vehiculo == null) {
            throw new IllegalArgumentException("El vehículo no puede ser null.");
        }
        if (!Double.isFinite(factorAceleracion) || factorAceleracion <= 0) {
            throw new IllegalArgumentException("El factor de aceleración debe ser mayor que cero.");
        }
        this.vehiculo = vehiculo;
        this.factorAceleracion = factorAceleracion;
    }

    /**
     * Recorre la ruta completa. Se conserva este método para usos que no necesiten
     * comprobar una cancelación durante el desplazamiento.
     */
    public void recorrer(RutaCalculada ruta) throws InterruptedException {
        recorrer(ruta, () -> true);
    }

    /**
     * Recorre la ruta mientras continuar devuelva true.
     *
     * @return true si llegó al último punto; false si el viaje fue cancelado.
     */
    public boolean recorrer(RutaCalculada ruta, BooleanSupplier continuar) throws InterruptedException {
        if (ruta == null || continuar == null) {
            throw new IllegalArgumentException("La ruta y la condición de continuidad son obligatorias.");
        }

        List<PuntoRuta> puntos = ruta.geometria();
        if (puntos == null || puntos.size() < 2) {
            throw new IllegalArgumentException("La ruta debe contener al menos dos puntos.");
        }

        double tramos = puntos.size() - 1.0;
        long pasoMs = Math.max(PASO_MINIMO_MS,
                Math.round(ruta.duracionMilisegundos() / tramos / factorAceleracion));

        for (PuntoRuta punto : puntos) {
            if (!continuar.getAsBoolean()) {
                return false;
            }

            BloqueoViajes.LOCK.lock();
            try {
                // Se vuelve a comprobar dentro del bloqueo para que una cancelación
                // no quede intercalada entre la comprobación y la actualización.
                if (!continuar.getAsBoolean()) {
                    return false;
                }
                vehiculo.setUbicacion(new Ubicacion(punto.latitud(), punto.longitud()));
            } finally {
                BloqueoViajes.LOCK.unlock();
            }

            Thread.sleep(pasoMs);
        }

        return continuar.getAsBoolean();
    }
}
