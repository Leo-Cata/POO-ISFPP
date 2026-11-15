package md.logica;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import md.modelo.Viaje;
import md.rutas.api.RutaNoDisponibleException;

/**
 * Coordina temporalmente el ciclo de vida de un viaje solicitado por un
 * cliente.
 *
 * Se encarga de programar las transiciones entre los distintos estados del
 * viaje,
 * delegando las operaciones de negocio en GestorViajes.
 *
 * Las esperas se ejecutan en un hilo independiente para no bloquear la
 * interfaz.
 */
public class GestorCicloViajeCliente implements AutoCloseable {

    // Tiempo de espera antes de intentar asignar un conductor al viaje.
    private static final long ESPERA_ACEPTACION_SEGUNDOS = 20;

    // Tiempo real equivalente a un minuto estimado por el motor de rutas.
    private static final long MILISEGUNDOS_REALES_POR_MINUTO = 10000;

    // Gestor encargado de ejecutar las operaciones reales del negocio.
    private final GestorViajes gestorViajes;

    // Ejecutor que programa las transiciones del ciclo en un único hilo.
    private final ScheduledExecutorService ejecutor;

    // Referencia a la tarea programada actualmente, para poder cancelarla.
    private ScheduledFuture<?> tareaPendiente;

    // Viaje cuyo ciclo de vida está siendo coordinado.
    private Viaje viajeActual;

    // Resultado de la aceptación, que contiene el conductor y ambas rutas.
    private GestorViajes.ResultadoSolicitud resultadoSolicitud;

    // Indica si el coordinador fue cerrado y no debe aceptar nuevas operaciones.
    private volatile boolean cerrado;

    // Notifica al controlador cuando cambia el estado del viaje.
    private Consumer<Viaje> notificadorCambio;

    // Permite informar errores ocurridos durante el ciclo.
    private Consumer<RuntimeException> notificadorError;

    // Indica si el ciclo actual fue cancelado.
    private volatile boolean cancelado;

    /**
     * Construye el coordinador del ciclo de viaje.
     *
     * @param gestorViajes gestor encargado de las operaciones del negocio.
     * @throws IllegalArgumentException si el gestor recibido es null.
     */
    public GestorCicloViajeCliente(GestorViajes gestorViajes) {

        if (gestorViajes == null) {
            throw new IllegalArgumentException(
                    "El gestor de viajes no puede ser null.");
        }

        this.gestorViajes = gestorViajes;

        // Se utiliza un único hilo para ejecutar las etapas secuencialmente.
        // Al ser daemon, no impide que la aplicación finalice.
        this.ejecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread hilo = new Thread(r, "ciclo-viaje-cliente");
            hilo.setDaemon(true);
            return hilo;
        });
    }

    /**
     * Inicia el ciclo temporal de un viaje solicitado.
     *
     * @param viaje             viaje que se va a coordinar.
     * @param notificadorCambio acción ejecutada cuando cambia el estado.
     * @param notificadorError  acción ejecutada si ocurre un error.
     */
    public synchronized void iniciar(
            Viaje viaje,
            Consumer<Viaje> notificadorCambio,
            Consumer<RuntimeException> notificadorError) {

        if (cerrado) {
            throw new IllegalStateException(
                    "El coordinador ya fue cerrado.");
        }

        if (viaje == null || notificadorCambio == null) {
            throw new IllegalArgumentException(
                    "El viaje y el notificador no pueden ser null.");
        }

        if (viajeActual != null) {
            throw new IllegalStateException(
                    "Ya existe un ciclo de viaje en ejecución.");
        }

        this.viajeActual = viaje;
        this.notificadorCambio = notificadorCambio;
        this.notificadorError = notificadorError;
        this.cancelado = false;

        // Informa que el viaje se encuentra solicitado.
        notificadorCambio.accept(viaje);

        // Programa el intento de aceptación después de 20 segundos.
        tareaPendiente = ejecutor.schedule(
                this::intentarAceptacion,
                ESPERA_ACEPTACION_SEGUNDOS,
                TimeUnit.SECONDS);
    }

    /**
     * Intenta asignar un conductor al viaje.
     *
     * Si no hay conductores disponibles, el viaje queda rechazado.
     * Si se acepta, se conserva el resultado para utilizar sus rutas.
     */
    private synchronized void intentarAceptacion() {
        if (cancelado || cerrado) {
            return;
        }

        try {
            resultadoSolicitud = gestorViajes.procesarAceptacionViaje(viajeActual);

            // Se informa tanto la aceptación como el rechazo.
            notificadorCambio.accept(viajeActual);

            if (resultadoSolicitud == null) {
                // El viaje fue rechazado porque no se encontró un conductor.
                viajeActual = null;
                resultadoSolicitud = null;
                return;
            }

            // La siguiente etapa será calcular la espera de acercamiento.
            programarAcercamiento();
        } catch (RutaNoDisponibleException e) {

            // GestorViajes rechaza el viaje cuando no puede planificar las rutas.
            notificadorCambio.accept(viajeActual);

            if (notificadorError != null) {
                notificadorError.accept(
                        new IllegalStateException(
                                "No se pudo calcular la ruta del viaje.", e));
            }

            viajeActual = null;
            resultadoSolicitud = null;

        } catch (RuntimeException e) {
            if (notificadorError != null) {
                notificadorError.accept(e);
            }
        }
    }

    /**
     * Programa el tiempo que tardará el conductor en llegar al origen.
     */
    private void programarAcercamiento() {

        long demora = Math.round(
                resultadoSolicitud.rutas().acercamiento().duracionMinutos()
                        * MILISEGUNDOS_REALES_POR_MINUTO);

        tareaPendiente = ejecutor.schedule(
                this::iniciarTraslado,
                demora,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Inicia el viaje cuando el conductor llega al origen.
     */
    private synchronized void iniciarTraslado() {
        if (cancelado || cerrado) {
            return;
        }

        try {
            boolean iniciado = gestorViajes.procesarInicioViaje(
                    resultadoSolicitud.conductor());

            if (!iniciado) {
                throw new IllegalStateException(
                        "No se pudo iniciar el viaje.");
            }

            notificadorCambio.accept(viajeActual);

            programarTraslado();

        } catch (RuntimeException e) {
            if (notificadorError != null) {
                notificadorError.accept(e);
            }
        }
    }

    /**
     * Programa el tiempo que tardará el viaje desde el origen hasta el destino.
     */
    private void programarTraslado() {

        long demora = Math.round(
                resultadoSolicitud.rutas().traslado().duracionMinutos()
                        * MILISEGUNDOS_REALES_POR_MINUTO);

        tareaPendiente = ejecutor.schedule(
                this::finalizarTraslado,
                demora,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Finaliza el viaje cuando se cumple el tiempo estimado de traslado.
     */
    private synchronized void finalizarTraslado() {
        if (cancelado || cerrado) {
            return;
        }
        try {
            boolean finalizado = gestorViajes.procesarFinalizacionViaje(
                    resultadoSolicitud.conductor());

            if (!finalizado) {
                throw new IllegalStateException(
                        "No se pudo finalizar el viaje.");
            }

            // Informa al controlador que el viaje finalizó.
            notificadorCambio.accept(viajeActual);

            // Libera las referencias al viaje terminado.
            viajeActual = null;
            resultadoSolicitud = null;

        } catch (RuntimeException e) {
            if (notificadorError != null) {
                notificadorError.accept(e);
            }
        }
    }

    /**
     * Detiene el ciclo temporal del viaje actual.
     *
     * No modifica el estado del viaje ni ejecuta su cancelación de negocio.
     * Esa operación corresponde a GestorViajes.
     */
    public synchronized void cancelar() {

        if (viajeActual == null) {
            return;
        }

        cancelado = true;

        if (tareaPendiente != null) {
            tareaPendiente.cancel(false);
            tareaPendiente = null;
        }

        viajeActual = null;
        resultadoSolicitud = null;
    }

    /**
     * Cierra el coordinador y libera los recursos utilizados.
     *
     * Cancela cualquier tarea pendiente y detiene el ejecutor.
     */
    @Override
    public synchronized void close() {
        cerrado = true;
        cancelado = true;

        if (tareaPendiente != null) {
            tareaPendiente.cancel(false);
            tareaPendiente = null;
        }

        viajeActual = null;
        resultadoSolicitud = null;
        notificadorCambio = null;
        notificadorError = null;

        ejecutor.shutdownNow();
    }

}
