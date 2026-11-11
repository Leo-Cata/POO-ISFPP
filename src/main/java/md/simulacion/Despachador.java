package md.simulacion;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
import md.modelo.EstadoConductor;
import md.modelo.EstadoViaje;
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
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ReentrantLock lock = BloqueoViajes.LOCK;
    private final Condition cambio = BloqueoViajes.CAMBIO;

    private final ProbeHashMap<String, Usuario> usuarios;
    private final Movilidad movilidad;
    private final PlanificadorRutas motorVial;
    private final PoliticaAsignacion politica;
    private final long esperaMaximaMs;
    private final Estadisticas estadisticas;
    private final Random random = new Random();

    /** Solicitudes que todavía no tienen un conductor asignado. */
    private final LinkedList<Viaje> pendientes = new LinkedList<>();

    /** Momento en que se publicó cada solicitud pendiente. */
    private final Map<Viaje, Long> instanteSolicitud = new HashMap<>();

    /** Viajes aceptados que el hilo del conductor todavía no retiró. */
    private final Map<Usuario, Viaje> asignados = new HashMap<>();

    /** Viajes que los hilos de conductores están atendiendo actualmente. */
    private final Map<Usuario, Viaje> enAtencion = new HashMap<>();

    /** Cantidad de viajes finalizados por conductor, para la política ROTACION. */
    private final Map<Usuario, Integer> viajesPorConductor = new HashMap<>();

    private boolean cerrado;
    private int totalCancelaciones;

    /**
     * @param usuarios        todos los usuarios de la simulación (conductores y pasajeros).
     * @param motorVial       motor de rutas para calcular los recorridos de cada asignación.
     * @param politica        cómo elegir entre varios conductores compatibles.
     * @param esperaMaximaMs  tiempo real que un viaje puede esperar conductor antes de rechazarse.
     * @param estadisticas    dónde se acumulan los resultados.
     */
    public Despachador(ProbeHashMap<String, Usuario> usuarios,PlanificadorRutas motorVial,PoliticaAsignacion politica,
            long esperaMaximaMs,Estadisticas estadisticas) {
    	if (usuarios == null || motorVial == null || politica == null || estadisticas == null) {
    		throw new IllegalArgumentException("Los parámetros del despachador no pueden ser null.");
    	}
    	if (esperaMaximaMs <= 0) {
    		throw new IllegalArgumentException("El tiempo máximo de espera debe ser mayor que cero.");
    	}

    	this.usuarios = usuarios;
    	this.movilidad = new Movilidad(usuarios);
    	this.motorVial = motorVial;
    	this.politica = politica;
    	this.esperaMaximaMs = esperaMaximaMs;
    	this.estadisticas = estadisticas;
    }
    
    /** Muestra el estado inicial y los conductores que están libres. */
    public void mostrarEstadoInicial() {
        lock.lock();
        try {
            evento("INICIO", "Simulación preparada. Política de asignación: " + politica + ".");
            imprimirEstadoBloqueado();
        } finally {
            lock.unlock();
        }
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
     * Registra un viaje solicitado y trata de asignarlo a un conductor.
     * Si no encuentra uno compatible, lo deja en el buffer hasta que se libere
     * un conductor o venza el tiempo máximo de espera.
     */
    public boolean solicitar(Viaje viaje) {
        if (viaje == null || viaje.getCliente() == null || viaje.getCliente().getCliente() == null) {
            throw new IllegalArgumentException("El viaje y su cliente son obligatorios.");
        }

        lock.lock();
        try {
            if (cerrado) {
                return false;
            }
            if (viaje.getCliente().getCliente().enViaje()) {
                evento("SOLICITUD", nombre(viaje.getCliente()) + " no puede solicitar otro viaje: ya tiene uno activo.");
                return false;
            }

            viaje.solicitar(LocalDateTime.now());
            pendientes.addLast(viaje);
            instanteSolicitud.put(viaje, System.currentTimeMillis());

            evento("SOLICITUD", nombre(viaje.getCliente()) + " solicita " + servicio(viaje)
                    + " desde " + ubicacion(viaje.getOrigen()) + " hasta " + ubicacion(viaje.getDestino())
                    + ". Costo estimado: $" + formato(viaje.getCostoTotal()) + ".");

            asignarPendientes();

            if (viaje.estadoActual() == EstadoViaje.SOLICITADO) {
                evento("EN ESPERA", "No hay un conductor compatible disponible para "
                        + nombre(viaje.getCliente()) + ". La solicitud queda en espera.");
            }

            imprimirEstadoBloqueado();
            cambio.signalAll();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Lo utiliza el hilo de cada conductor. Espera hasta que le asignen un viaje.
     * Devuelve null cuando se cerró la simulación y ya no tiene una asignación.
     */
    public Viaje esperarAsignacion(Usuario conductor) throws InterruptedException {
        if (conductor == null) {
            throw new IllegalArgumentException("El conductor no puede ser null.");
        }

        lock.lockInterruptibly();
        try {
            while (true) {
                Viaje viaje = asignados.remove(conductor);
                if (viaje != null) {
                    // Puede haberse cancelado antes de que el hilo alcanzara a retirarlo.
                    if (viaje.estadoActual() == EstadoViaje.ACEPTADO) {
                        enAtencion.put(conductor, viaje);
                        evento("CONDUCTOR", nombre(conductor) + " comienza a atender el viaje de "
                                + nombre(viaje.getCliente()) + ". Se dirige al punto de origen.");
                        return viaje;
                    }
                    continue;
                }

                if (cerrado) {
                    return null;
                }

                cambio.await();
            }
        } finally {
            lock.unlock();
        }
    }
    
    /** Comprueba de forma segura si un viaje sigue activo y no fue cancelado. */
    public boolean sigueActivo(Viaje viaje) {
        if (viaje == null) {
            return false;
        }
        lock.lock();
        try {
            EstadoViaje estado = viaje.estadoActual();
            return estado == EstadoViaje.ACEPTADO || estado == EstadoViaje.INICIADO;
        } finally {
            lock.unlock();
        }
    }

    /** El conductor llegó al pasajero: el viaje pasa de ACEPTADO a INICIADO. */
    public boolean iniciarViaje(Viaje viaje) {
        if (viaje == null) {
            return false;
        }
        lock.lock();
        try {
            if (viaje.estadoActual() != EstadoViaje.ACEPTADO) {
                return false;
            }

            viaje.iniciar(LocalDateTime.now());
            evento("INICIADO", nombre(viaje.getConductor()) + " llegó al punto de origen de "
                    + nombre(viaje.getCliente()) + ". Comienza el traslado al destino.");
            imprimirEstadoBloqueado();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Finaliza el viaje, deja libre al conductor y vuelve a revisar las solicitudes
     * que estaban esperando.
     */
    public boolean finalizarViaje(Viaje viaje) {
        if (viaje == null) {
            return false;
        }
        lock.lock();
        try {
            if (viaje.estadoActual() != EstadoViaje.INICIADO) {
                return false;
            }

            viaje.finalizar(LocalDateTime.now(), calificacionAleatoria(), calificacionAleatoria());
            Usuario conductor = viaje.getConductor();
            if (conductor != null) {
                viajesPorConductor.merge(conductor, 1, Integer::sum);
                enAtencion.remove(conductor, viaje);
            }

            double km = kilometrosRecorridos(viaje);
            estadisticas.registrarViajeFinalizado(km, viaje.getCostoTotal());

            evento("FINALIZADO", "Viaje de " + nombre(viaje.getCliente()) + " completado por "
                    + nombre(conductor) + ". Distancia vial: " + formato(km) + " km; costo: $"
                    + formato(viaje.getCostoTotal()) + ".");

            asignarPendientes();
            cambio.signalAll();
            imprimirEstadoBloqueado();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Cancela un viaje pendiente, asignado o que ya está siendo recorrido.
     * La cancelación se realiza con el mismo bloqueo que usan los hilos, por lo
     * que el conductor no puede continuar actualizando el vehículo después de
     * observar que el viaje fue cancelado.
     *
     * @param viaje viaje que se cancela.
     * @param quienCancela debe ser el cliente o el conductor asignado.
     * @param motivo explicación de la cancelación.
     * @return true si se canceló; false si ya no era posible cancelarlo.
     */
    public boolean cancelarViaje(Viaje viaje, Usuario quienCancela, String motivo) {
        if (viaje == null || quienCancela == null || motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El viaje, quien cancela y el motivo son obligatorios.");
        }

        lock.lock();
        try {
            EstadoViaje estadoAnterior = viaje.estadoActual();
            if (estadoAnterior != EstadoViaje.SOLICITADO
                    && estadoAnterior != EstadoViaje.ACEPTADO
                    && estadoAnterior != EstadoViaje.INICIADO) {
                return false;
            }

            // Viaje.cancelar valida que sea el cliente o el conductor del viaje.
            viaje.cancelar(LocalDateTime.now(), quienCancela, motivo);

            pendientes.remove(viaje);
            instanteSolicitud.remove(viaje);

            Usuario conductor = viaje.getConductor();
            if (conductor != null) {
                asignados.remove(conductor, viaje);
                enAtencion.remove(conductor, viaje);
            }

            totalCancelaciones++;
            String actor = quienCancela.equals(viaje.getCliente()) ? "cliente" : "conductor";
            evento("CANCELADO", nombre(quienCancela) + " canceló el viaje de "
                    + nombre(viaje.getCliente()) + ". Quién canceló: " + actor
                    + ". Estado anterior: " + estadoAnterior + ". Motivo: " + motivo + ".");

            asignarPendientes();
            cambio.signalAll();
            imprimirEstadoBloqueado();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** El hilo de conductor llama este método al salir de la atención del viaje. */
    public void liberarAtencion(Usuario conductor, Viaje viaje) {
        if (conductor == null || viaje == null) {
            return;
        }
        lock.lock();
        try {
            enAtencion.remove(conductor, viaje);
            cambio.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /** Cantidad de cancelaciones registradas durante esta ejecución. */
    public int getCantidadCancelaciones() {
        lock.lock();
        try {
            return totalCancelaciones;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Cierra la simulación: no acepta nuevas solicitudes, rechaza las que siguen
     * pendientes y despierta a los hilos que estaban esperando.
     * Los viajes ya aceptados pueden terminar su recorrido.
     */
    public void cerrar() {
        lock.lock();
        try {
            if (cerrado) {
                return;
            }
            cerrado = true;

            for (Viaje viaje : pendientes) {
                // Si otro componente canceló directamente el viaje, no intentamos
                // rechazarlo una segunda vez.
                if (viaje.estadoActual() == EstadoViaje.SOLICITADO) {
                    viaje.rechazar(LocalDateTime.now());
                    estadisticas.registrarRechazo();
                    evento("RECHAZADO", "La solicitud de " + nombre(viaje.getCliente())
                            + " quedó sin asignación al finalizar el tiempo de simulación.");
                }
            }
            pendientes.clear();
            instanteSolicitud.clear();

            evento("CIERRE", "No se recibirán más solicitudes. Los viajes aceptados terminarán.");
            cambio.signalAll();
            imprimirEstadoBloqueado();
        } finally {
            lock.unlock();
        }
    }

    // Operaciones privadas. Se invocan con BloqueoViajes.LOCK adquirido.

    private Usuario elegirPasajeroLibre(List<Usuario> pasajeros) {
        List<Usuario> libres = new ArrayList<>();
        for (Usuario pasajero : pasajeros) {
            if (pasajero != null && pasajero.getCliente() != null && !pasajero.getCliente().enViaje()) {
                libres.add(pasajero);
            }
        }
        return libres.isEmpty() ? null : libres.get(random.nextInt(libres.size()));
    }

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

    private boolean intentarAsignar(Viaje viaje) {
        List<Usuario> candidatos = new ArrayList<>(movilidad.obtenerConductoresCompatibles(viaje.getServicio()));

        while (!candidatos.isEmpty()) {
            Usuario conductor = elegirConductor(candidatos, viaje.getOrigen());
            if (conductor == null) {
                break;
            }
            candidatos.remove(conductor);
            try {
                RutasViaje rutas = new PlanificacionViajeService(motorVial).planificar(
                        conductor.getConductor().getVehiculoActivo(), viaje.getOrigen(), viaje.getDestino());

                viaje.setRutas(rutas);
                viaje.aceptar(LocalDateTime.now(), conductor);
                asignados.put(conductor, viaje);

                evento("ACEPTADO", nombre(conductor) + " acepta el viaje solicitado por "
                        + nombre(viaje.getCliente()) + ". Vehículo: "
                        + conductor.getConductor().getVehiculoActivo().getPatente() + ".");
                return true;
            } catch (RutaNoDisponibleException | IllegalArgumentException | IllegalStateException e) {
                evento("NO ASIGNADO", nombre(conductor) + " no pudo tomar el viaje de "
                        + nombre(viaje.getCliente()) + ": " + e.getMessage());
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
            Ubicacion posicion = candidato.getConductor().getVehiculoActivo().getUbicacion();
            if (posicion == null) {
                continue;
            }
            double distancia = posicion.calcularDistanciaA(origen);
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

    private void expirarViajesVencidos() {
        long ahora = System.currentTimeMillis();
        Iterator<Viaje> it = pendientes.iterator();
        boolean huboVencimientos = false;
        while (it.hasNext()) {
            Viaje viaje = it.next();
            Long inicio = instanteSolicitud.get(viaje);
            if (viaje.estadoActual() != EstadoViaje.SOLICITADO) {
                // Limpia viajes que hayan sido cancelados desde otro componente.
                it.remove();
                instanteSolicitud.remove(viaje);
                huboVencimientos = true;
            } else if (inicio != null && ahora - inicio >= esperaMaximaMs) {
                it.remove();
                instanteSolicitud.remove(viaje);
                viaje.rechazar(LocalDateTime.now());
                estadisticas.registrarRechazo();
                evento("RECHAZADO", "La solicitud de " + nombre(viaje.getCliente())
                        + " venció: no se encontró un conductor compatible a tiempo.");
                huboVencimientos = true;
            }
        }
        if (huboVencimientos) {
            cambio.signalAll();
            imprimirEstadoBloqueado();
        }
    }

    private double kilometrosRecorridos(Viaje viaje) {
        RutasViaje rutas = viaje.getRutas();
        if (rutas == null) {
            return 0.0;
        }
        return rutas.acercamiento().distanciaKm() + rutas.traslado().distanciaKm();
    }

    private CalificacionViaje calificacionAleatoria() {
        CalificacionViaje[] valores = CalificacionViaje.values();
        return valores[random.nextInt(valores.length)];
    }

    private void imprimirEstadoBloqueado() {
        List<String> libres = new ArrayList<>();
        List<String> ocupados = new ArrayList<>();
        List<String> fueraServicio = new ArrayList<>();

        for (Usuario usuario : usuarios.values()) {
            if (usuario == null || usuario.getConductor() == null) {
                continue;
            }
            EstadoConductor estado = usuario.getConductor().getEstadoConductor();
            if (estado == EstadoConductor.DISPONIBLE && usuario.getRolActivo() == md.modelo.RolUsuario.CONDUCTOR) {
                libres.add(nombre(usuario));
            } else if (estado == EstadoConductor.VIAJE_A_ORIGEN || estado == EstadoConductor.VIAJE_A_DESTINO) {
                ocupados.add(nombre(usuario));
            } else {
                fueraServicio.add(nombre(usuario));
            }
        }

        evento("ESTADO", "Conductores libres (" + libres.size() + "): " + lista(libres));
        evento("ESTADO", "Conductores ocupados (" + ocupados.size() + "): " + lista(ocupados));
        if (!fueraServicio.isEmpty()) {
            evento("ESTADO", "Fuera de servicio (" + fueraServicio.size() + "): " + lista(fueraServicio));
        }

        if (pendientes.isEmpty()) {
            evento("ESTADO", "Solicitudes en espera: ninguna.");
        } else {
            List<String> solicitudes = new ArrayList<>();
            for (Viaje viaje : pendientes) {
                solicitudes.add(nombre(viaje.getCliente()) + " - " + servicio(viaje));
            }
            evento("ESTADO", "Solicitudes esperando conductor (" + pendientes.size() + "): "
                    + lista(solicitudes));
        }
    }

    private void evento(String tipo, String mensaje) {
        synchronized (System.out) {
            System.out.printf("[%s] [%-11s] %s%n", LocalTime.now().format(FORMATO_HORA), tipo, mensaje);
        }
    }

    private static String nombre(Usuario usuario) {
        return usuario == null ? "desconocido" : usuario.getNombre();
    }

    private static String servicio(Viaje viaje) {
        return viaje.getServicio() == null ? "servicio sin nombre" : viaje.getServicio().getNombre();
    }

    private static String ubicacion(Ubicacion ubicacion) {
        if (ubicacion == null) {
            return "ubicación desconocida";
        }
        return "(" + formato(ubicacion.getLatitud()) + ", " + formato(ubicacion.getLongitud()) + ")";
    }

    private static String formato(double valor) {
        return String.format(java.util.Locale.ROOT, "%.2f", valor);
    }

    private static String lista(List<String> elementos) {
        return elementos.isEmpty() ? "ninguno" : String.join(", ", elementos);
    }
}
