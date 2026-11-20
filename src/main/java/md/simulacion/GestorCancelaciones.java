
package md.simulacion;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import md.modelo.Usuario;
import md.modelo.Viaje;

public class GestorCancelaciones {

    private static final double PROBABILIDAD_CANCELACION = 0.10;
    private static final int DEMORA_MINIMA_MS = 3000;
    private static final int DEMORA_MAXIMA_MS = 8000;

    private final Despachador despachador;
    private final Random random = new Random();

    // se utiliza newSingleThreadScheduledExecutor ya que termine programar tareas
    // para que se ejecuten despues de un tiempo determinado
    private final ScheduledExecutorService planificador = Executors.newSingleThreadScheduledExecutor();

    public GestorCancelaciones(Despachador despachador) {
        this.despachador = despachador;
    }

    public void programar(Viaje viaje) {
        int demora = DEMORA_MINIMA_MS
                + random.nextInt(DEMORA_MAXIMA_MS - DEMORA_MINIMA_MS + 1);

        planificador.schedule(
                () -> intentarCancelar(viaje),
                demora,
                TimeUnit.MILLISECONDS);
    }

    private void intentarCancelar(Viaje viaje) {

        // Determinar aleatoriamente si ocurre una cancelación.
        if (random.nextDouble() >= PROBABILIDAD_CANCELACION) {
            return;
        }

        Usuario cliente = viaje.getCliente();
        Usuario conductor = viaje.getConductor();

        if (cliente == null) {
            return;
        }

        Usuario responsable;

        // Si todavía no hay conductor asignado, cancela el cliente.
        if (conductor == null) {
            responsable = cliente;
        } else {
            // Si ambos están disponibles, elegir aleatoriamente.
            responsable = random.nextBoolean() ? cliente : conductor;
        }

        try {
            despachador.cancelarViaje(
                    viaje,
                    responsable,
                    "Cancelación aleatoria de la simulación");
        } catch (IllegalStateException | IllegalArgumentException e) {
            // El viaje pudo haber finalizado o cambiado de estado.
        }
    }

    public void cerrar() {
        planificador.shutdownNow();
    }
}
