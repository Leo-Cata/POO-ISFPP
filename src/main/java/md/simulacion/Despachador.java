package md.simulacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import md.logica.BloqueoViajes;
import md.logica.Movilidad;
import md.logica.PlanificacionViajeService;
import md.modelo.CalificacionViaje;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Viaje;
import md.rutas.api.PlanificadorRutas;
import md.rutas.api.RutaNoDisponibleException;
import md.rutas.api.RutasViaje;
import net.datastructures.ProbeHashMap;

/**
 * Buffer de viajes pendientes y asignación de conductores.
 *
 * Es el punto de encuentro entre los hilos de pasajeros (que publican viajes) y
 * los hilos de conductores (que esperan asignaciones). Todo lo que cambia estado
 * se hace bajo BloqueoViajes.LOCK, y los hilos que no tienen nada que hacer
 * esperan en BloqueoViajes.CAMBIO sin consumir CPU.
 */
public class Despachador {

    /** Cada cuánto se revisan los viajes vencidos mientras un pasajero espera. */
    private static final long REVISION_MS = 200;

    private final ReentrantLock lock = BloqueoViajes.LOCK;
    private final Condition cambio = BloqueoViajes.CAMBIO;

    private final Movilidad movilidad;
    private final PlanificadorRutas motorVial;
    private final PoliticaAsignacion politica;
    private final long esperaMaximaMs;
    private final Estadisticas estadisticas;
    private final Random random = new Random();

    // Buffer: viajes SOLICITADOS que todavía no tienen conductor
    private final LinkedList<Viaje> pendientes = new LinkedList<>();
    // Instante de solicitud de cada pendiente, para vencerlos
    private final Map<Viaje, Long> instanteSolicitud = new HashMap<>();
    // Conductor -> viaje que le asignaron y todavía no empezó a atender
    private final Map<Usuario, Viaje> asignados = new HashMap<>();
    // Viajes finalizados por conductor (lo usa la política ROTACION)
    private final Map<Usuario, Integer> viajesPorConductor = new HashMap<>();

    private boolean cerrado = false;

    /**
     * @param usuarios        todos los usuarios de la simulación (conductores y pasajeros).
     * @param motorVial       motor de rutas para calcular los recorridos de cada asignación.
     * @param politica        cómo elegir entre varios conductores compatibles.
     * @param esperaMaximaMs  tiempo real que un viaje puede esperar conductor antes de rechazarse.
     * @param estadisticas    dónde se acumulan los resultados.
     */
    public Despachador(ProbeHashMap<String, Usuario> usuarios, PlanificadorRutas motorVial,
                       PoliticaAsignacion politica, long esperaMaximaMs, Estadisticas estadisticas) {
        if (usuarios == null || motorVial == null || politica == null || estadisticas == null) {
            throw new IllegalArgumentException("Los parámetros del despachador no pueden ser null.");
        }
        this.movilidad = new Movilidad(usuarios);
        this.motorVial = motorVial;
        this.politica = politica;
        this.esperaMaximaMs = esperaMaximaMs;
        this.estadisticas = estadisticas;
    }

    /**
     * Lo llama el hilo generador de pasajeros. Espera hasta que haya un pasajero
     * sin viaje en curso y lo devuelve.
     *
     * @return un pasajero libre, o null si la simulación se cerró.
     */
    public Usuario esperarPasajeroLibre(List<Usuario> pasajeros) throws InterruptedException {
        lock.lock();
        try {
            while (!cerrado) {
                expirarViajesVencidos();
                Usuario libre = elegirPasajeroLibre(pasajeros);
                if (libre != null) {
                    return libre;
                }
                // Espera con tiempo límite para poder revisar los viajes vencidos
                cambio.await(REVISION_MS, TimeUnit.MILLISECONDS);
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Registra la solicitud en el buffer y, si hay un conductor compatible con su
     * recorrido, se la asigna en el mismo momento.
     *
     * @return false si la simulación se cerró o el pasajero ya tiene un viaje en curso.
     */
    public boolean solicitar(Viaje viaje) {
        lock.lock();
        try {
            if (cerrado || viaje.getCliente().getCliente().enViaje()) {
                return false;
            }
            viaje.solicitar(LocalDateTime.now());
            pendientes.addLast(viaje);
            instanteSolicitud.put(viaje, System.currentTimeMillis());
            asignarPendientes();
            cambio.signalAll();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Lo llama el hilo de cada conductor. Espera hasta que le asignen un viaje.
     *
     * @return el viaje asignado, o null si la simulación se cerró y no le queda nada.
     */
    public Viaje esperarAsignacion(Usuario conductor) throws InterruptedException {
        lock.lock();
        try {
            Viaje viaje = asignados.remove(conductor);
            while (viaje == null && !cerrado) {
                cambio.await();
                viaje = asignados.remove(conductor);
            }
            return viaje;
        } finally {
            lock.unlock();
        }
    }

    /** El conductor llegó al pasajero y lo sube: el viaje pasa a INICIADO. */
    public void iniciarViaje(Viaje viaje) {
        lock.lock();
        try {
            viaje.iniciar(LocalDateTime.now());
        } finally {
            lock.unlock();
        }
    }

    /**
     * El conductor llegó al destino: el viaje se finaliza, el conductor queda
     * disponible y se intenta asignar lo que esté esperando.
     */
    public void finalizarViaje(Viaje viaje) {
        lock.lock();
        try {
            viaje.finalizar(LocalDateTime.now(), calificacionAleatoria(), calificacionAleatoria());

            viajesPorConductor.merge(viaje.getConductor(), 1, Integer::sum);
            estadisticas.registrarViajeFinalizado(kilometrosRecorridos(viaje), viaje.getCostoTotal());

            asignarPendientes();
            cambio.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Corta la simulación: no entran más pedidos, los pendientes se rechazan y los
     * conductores que están esperando terminan.
     */
    public void cerrar() {
        lock.lock();
        try {
            cerrado = true;
            for (Viaje viaje : pendientes) {
                viaje.rechazar(LocalDateTime.now());
                estadisticas.registrarRechazo();
            }
            pendientes.clear();
            instanteSolicitud.clear();
            cambio.signalAll();
        } finally {
            lock.unlock();
        }
    }

    // ----------------------------------------------------------------------
    // Métodos privados: se llaman siempre con el lock tomado
    // ----------------------------------------------------------------------

    private Usuario elegirPasajeroLibre(List<Usuario> pasajeros) {
        List<Usuario> libres = new ArrayList<>();
        for (Usuario pasajero : pasajeros) {
            if (!pasajero.getCliente().enViaje()) {
                libres.add(pasajero);
            }
        }
        return libres.isEmpty() ? null : libres.get(random.nextInt(libres.size()));
    }

    /** Recorre el buffer y asigna cada viaje que tenga un conductor disponible. */
    private void asignarPendientes() {
        Iterator<Viaje> it = pendientes.iterator();
        while (it.hasNext()) {
            Viaje viaje = it.next();
            if (intentarAsignar(viaje)) {
                it.remove();
                instanteSolicitud.remove(viaje);
                cambio.signalAll();
            }
        }
    }

    /**
     * Busca entre los conductores compatibles uno que pueda llegar al pasajero.
     * Si el recorrido no se puede calcular para un conductor, prueba con el siguiente.
     */
    private boolean intentarAsignar(Viaje viaje) {
        List<Usuario> candidatos = new ArrayList<>(
                movilidad.obtenerConductoresCompatibles(viaje.getServicio()));

        while (!candidatos.isEmpty()) {
            Usuario conductor = elegirConductor(candidatos, viaje.getOrigen());
            candidatos.remove(conductor);
            try {
                RutasViaje rutas = new PlanificacionViajeService(motorVial).planificar(
                        conductor.getConductor().getVehiculoActivo(), viaje.getOrigen(), viaje.getDestino());
                viaje.setRutas(rutas);
                viaje.aceptar(LocalDateTime.now(), conductor);
                asignados.put(conductor, viaje);
                return true;
            } catch (RutaNoDisponibleException | IllegalArgumentException | IllegalStateException e) {
                // Este conductor no puede atender el viaje: se prueba con el siguiente
            }
        }
        return false;
    }

    private Usuario elegirConductor(List<Usuario> candidatos, Ubicacion origen) {
        return switch (politica) {
            case SORTEO -> candidatos.get(random.nextInt(candidatos.size()));
            case CERCANIA -> masCercano(candidatos, origen);
            case ROTACION -> {
                int minimo = candidatos.stream().mapToInt(this::viajesDe).min().orElse(0);
                List<Usuario> menosViajes = candidatos.stream()
                        .filter(c -> viajesDe(c) == minimo)
                        .toList();
                yield masCercano(menosViajes, origen);
            }
        };
    }

    private Usuario masCercano(List<Usuario> candidatos, Ubicacion origen) {
        Usuario mejor = null;
        double menorDistancia = Double.POSITIVE_INFINITY;
        for (Usuario candidato : candidatos) {
            double distancia = candidato.getConductor().getVehiculoActivo().getUbicacion()
                    .calcularDistanciaA(origen);
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                mejor = candidato;
            }
        }
        return mejor;
    }

    private int viajesDe(Usuario conductor) {
        return viajesPorConductor.getOrDefault(conductor, 0);
    }

    /** Rechaza los pendientes que superaron el tiempo de espera; así el pasajero queda libre. */
    private void expirarViajesVencidos() {
        long ahora = System.currentTimeMillis();
        Iterator<Viaje> it = pendientes.iterator();
        while (it.hasNext()) {
            Viaje viaje = it.next();
            if (ahora - instanteSolicitud.get(viaje) >= esperaMaximaMs) {
                it.remove();
                instanteSolicitud.remove(viaje);
                viaje.rechazar(LocalDateTime.now());
                estadisticas.registrarRechazo();
                cambio.signalAll();
            }
        }
    }

    private double kilometrosRecorridos(Viaje viaje) {
        RutasViaje rutas = viaje.getRutas();
        return rutas.acercamiento().distanciaKm() + rutas.traslado().distanciaKm();
    }

    /** Calificación al azar: la simulación no tiene personas que califiquen. */
    private CalificacionViaje calificacionAleatoria() {
        CalificacionViaje[] valores = CalificacionViaje.values();
        return valores[random.nextInt(valores.length)];
    }
}
