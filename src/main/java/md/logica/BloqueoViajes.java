package md.logica;

import java.util.concurrent.locks.Condition; 
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lock único que protege toda decisión de asignación y todo cambio de estado
 * de viajes, conductores y clientes.
 *
 * La interfaz gráfica (GestorViajes) y la simulación (Despachador) usan este
 * mismo lock. Si cada parte usara un lock distinto, no se excluirían entre sí.
 *
 * Patrón obligatorio:
 * BloqueoViajes.LOCK.lock();
 * try {
 *     ... leer o cambiar estados ...
 * } finally {
 *     BloqueoViajes.LOCK.unlock();
 * }
 */
public final class BloqueoViajes {

    /** Lock compartido por toda la aplicación. */
    public static final ReentrantLock LOCK = new ReentrantLock();

    /**
     * Condición asociada al lock. Se señala (signalAll) cada vez que cambia algo
     * que alguien podría estar esperando: un viaje nuevo, un conductor libre, un
     * pasajero libre o el cierre de la simulación.
     */
    public static final Condition CAMBIO = LOCK.newCondition();

    private BloqueoViajes() {
    }
}
