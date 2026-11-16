package md.logica;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import md.rutas.api.PlanificadorRutas;
import md.modelo.CalificacionViaje;
import md.modelo.EstadoViaje;
import md.modelo.RolUsuario;
import md.modelo.Servicio;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Viaje;
import md.rutas.api.RutaNoDisponibleException;
import md.rutas.api.RutasViaje;
import net.datastructures.ProbeHashMap;

/**
 * Coordina los casos de uso relacionados con los viajes.
 *
 * No interactúa con el usuario ni presenta información por pantalla. Esa
 * responsabilidad corresponde a las interfaces de usuario.
 */
public class GestorViajes {
	private final Map<UUID, Cotizacion> cotizacionesPorViaje = new HashMap<>();
	private final Map<UUID, Double> costosCancelacionPorViaje = new HashMap<>();
	private final Map<Usuario, Double> deudasPendientes = new HashMap<>();

	private final CalificacionServicio calificacionServicio = new CalificacionServicio();

	private final ProbeHashMap<String, Usuario> usuariosRegistrados;
	private final PlanificadorRutas motorVial;
	private final double velocidadMediaKmH;
	private final double costoCancelacionViajeAceptado;

	/**
	 * Construye el gestor con los usuarios registrados y el planificador de rutas.
	 *
	 * @param usuariosRegistrados usuarios disponibles en la aplicación.
	 * @param motorVial           planificador utilizado para calcular los
	 *                            recorridos.
	 * @throws IllegalArgumentException si alguno de los parámetros es null.
	 */
	/**
	 * Construye el gestor con los usuarios registrados, el planificador de rutas
	 * y los parámetros necesarios para calcular cotizaciones y cancelaciones.
	 *
	 * @param usuariosRegistrados           usuarios disponibles en la aplicación.
	 * @param motorVial                     planificador utilizado para calcular los
	 *                                      recorridos.
	 * @param velocidadMediaKmH             velocidad media utilizada para estimar
	 *                                      la duración.
	 * @param costoCancelacionViajeAceptado porcentaje aplicado al cancelar un viaje
	 *                                      aceptado.
	 * @throws IllegalArgumentException si algún parámetro no es válido.
	 */
	public GestorViajes(
			ProbeHashMap<String, Usuario> usuariosRegistrados,
			PlanificadorRutas motorVial,
			double velocidadMediaKmH,
			double costoCancelacionViajeAceptado) {

		if (usuariosRegistrados == null || motorVial == null) {
			throw new IllegalArgumentException("Los usuarios y el motor vial son obligatorios.");
		}

		if (!Double.isFinite(velocidadMediaKmH) || velocidadMediaKmH <= 0) {
			throw new IllegalArgumentException("La velocidad media debe ser positiva.");
		}

		if (!Double.isFinite(costoCancelacionViajeAceptado)
				|| costoCancelacionViajeAceptado < 0
				|| costoCancelacionViajeAceptado > 1) {
			throw new IllegalArgumentException("El costo de cancelación debe estar entre 0 y 1.");
		}

		this.usuariosRegistrados = usuariosRegistrados;
		this.motorVial = motorVial;
		this.velocidadMediaKmH = velocidadMediaKmH;
		this.costoCancelacionViajeAceptado = costoCancelacionViajeAceptado;
	}

	/**
	 * Resultado de la cotización comercial.
	 *
	 * La distancia y la duración son estimaciones geográficas, no métricas
	 * obtenidas del motor vial.
	 */
	public record Cotizacion(double distanciaKm, double duracionMinutos, double costoTotal) {
	}

	/**
	 * Resultado de una solicitud procesada correctamente.
	 */
	public record ResultadoSolicitud(Viaje viaje, RutasViaje rutas, Cotizacion cotizacion, Usuario conductor) {
	}

	/**
	 * Calcula la cotización comercial utilizando la distancia geográfica y la
	 * velocidad media configurada.
	 */
	public Cotizacion calcularCotizacion(Servicio servicio, Ubicacion origen, Ubicacion destino) {

		if (servicio == null || origen == null || destino == null) {
			throw new IllegalArgumentException("El servicio, el origen y el destino son obligatorios.");
		}

		double velocidadMedia = velocidadMediaKmH;

		double distanciaKm = origen.calcularDistancia(destino);
		double duracionMinutos = (distanciaKm / velocidadMedia) * 60.0;

		double costoTotal = servicio.calcularCosto(distanciaKm, duracionMinutos);

		return new Cotizacion(distanciaKm, duracionMinutos, costoTotal);
	}

	/**
	 * Registra una solicitud de viaje sin asignarle todavía un conductor.
	 *
	 * El viaje queda en estado SOLICITADO.
	 */
	public Viaje registrarSolicitudViaje(
			Usuario clienteUsuario,
			Servicio servicio,
			Ubicacion origen,
			Ubicacion destino) {

		if (clienteUsuario == null || servicio == null || origen == null || destino == null) {
			throw new IllegalArgumentException("Los datos de la solicitud son obligatorios.");
		}

		if (clienteUsuario.getRolActivo() != RolUsuario.CLIENTE
				|| clienteUsuario.getCliente() == null) {
			throw new IllegalStateException("Se requiere un usuario con perfil CLIENTE activo.");
		}

		Cotizacion cotizacion = calcularCotizacion(servicio, origen, destino);

		BloqueoViajes.LOCK.lock();
		try {
			if (deudasPendientes.containsKey(clienteUsuario)) {
				throw new IllegalStateException("El cliente tiene una deuda pendiente.");
			}

			if (clienteUsuario.getCliente().enViaje()) {
				throw new IllegalStateException("El cliente ya tiene un viaje activo.");
			}

			Viaje nuevoViaje = new Viaje(clienteUsuario, origen, destino, servicio);
			nuevoViaje.solicitar(LocalDateTime.now());

			cotizacionesPorViaje.put(nuevoViaje.getId(), cotizacion);

			return nuevoViaje;

		} finally {
			BloqueoViajes.LOCK.unlock();
		}
	}

	/**
	 * Busca un conductor compatible, planifica las rutas y acepta
	 * una solicitud que se encuentre en estado SOLICITADO.
	 *
	 * @return resultado de la aceptación, o null si no hay conductor disponible.
	 */
	public ResultadoSolicitud procesarAceptacionViaje(Viaje viaje)
			throws RutaNoDisponibleException {

		if (viaje == null) {
			throw new IllegalArgumentException("El viaje es obligatorio.");
		}

		BloqueoViajes.LOCK.lock();
		try {
			if (viaje.estadoActual() != EstadoViaje.SOLICITADO) {
				return null;
			}

			Cotizacion cotizacion = cotizacionesPorViaje.get(viaje.getId());

			if (cotizacion == null) {
				throw new IllegalStateException("No se encontró la cotización del viaje.");
			}

			Movilidad movilidad = new Movilidad(usuariosRegistrados);

			Usuario conductorSeleccionado = movilidad.seleccionarConductor(viaje.getServicio(), viaje.getOrigen());

			if (conductorSeleccionado == null) {
				viaje.rechazar(LocalDateTime.now());
				return null;
			}

			try {
				RutasViaje rutas = new PlanificacionViajeService(motorVial)
						.planificar(
								conductorSeleccionado.getConductor().getVehiculoActivo(),
								viaje.getOrigen(),
								viaje.getDestino());

				viaje.aceptar(LocalDateTime.now(), conductorSeleccionado);

				return new ResultadoSolicitud(
						viaje, rutas, cotizacion, conductorSeleccionado);

			} catch (RutaNoDisponibleException | RuntimeException e) {

				if (viaje.estadoActual() == EstadoViaje.SOLICITADO) {
					viaje.rechazar(LocalDateTime.now());
				}

				throw e;
			}

		} finally {
			BloqueoViajes.LOCK.unlock();
		}
	}

	/**
	 * Cancela el viaje activo.
	 *
	 * @return true si se canceló un viaje; false si no había uno activo.
	 */
	public boolean procesarCancelacionViaje(Usuario usuario, String motivo) {

		if (usuario == null) {
			throw new IllegalArgumentException("El usuario es obligatorio.");
		}

		if (motivo == null || motivo.isBlank()) {
			throw new IllegalArgumentException("El motivo de cancelación es obligatorio.");
		}

		BloqueoViajes.LOCK.lock();
		try {
			Iterable<Viaje> viajesUsuario = obtenerViajesUsuario(usuario);

			for (Viaje viaje : viajesUsuario) {

				EstadoViaje estado = viaje.estadoActual();

				if (estado == EstadoViaje.SOLICITADO || estado == EstadoViaje.ACEPTADO
						|| estado == EstadoViaje.INICIADO) {

					EstadoViaje estadoAnterior = viaje.estadoActual();

					double costo = calcularCostoCancelacion(viaje, usuario, estadoAnterior);
					costosCancelacionPorViaje.put(viaje.getId(), costo);

					viaje.cancelar(LocalDateTime.now(), usuario, motivo);

					// La deuda se registra únicamente cuando cancela el cliente.
					if (usuario.getRolActivo() == RolUsuario.CLIENTE) {
						deudasPendientes.put(usuario, costo);
					}

					return true;
				}
			}

			return false;
		} finally {
			BloqueoViajes.LOCK.unlock();
		}
	}

	/**
	 * Inicia el viaje aceptado del conductor.
	 *
	 * @return true si se inició el viaje; false si no había uno aceptado.
	 */
	public boolean procesarInicioViaje(Usuario conductorUsuario) {

		if (conductorUsuario == null || conductorUsuario.getRolActivo() != RolUsuario.CONDUCTOR
				|| conductorUsuario.getConductor() == null) {
			throw new IllegalArgumentException("Se requiere un usuario con el rol CONDUCTOR activo.");
		}

		BloqueoViajes.LOCK.lock();
		try {
			Viaje viajeActivo = obtenerViajeActivoConductor(conductorUsuario);

			if (viajeActivo == null || viajeActivo.estadoActual() != EstadoViaje.ACEPTADO) {
				return false;
			}

			viajeActivo.iniciar(LocalDateTime.now());

			return true;
		} finally {
			BloqueoViajes.LOCK.unlock();
		}
	}

	/**
	 * Finaliza el viaje iniciado del conductor.
	 *
	 * @return true si se finalizó el viaje; false si no había uno iniciado.
	 */
	public boolean procesarFinalizacionViaje(Usuario conductorUsuario) {

		if (conductorUsuario == null || conductorUsuario.getRolActivo() != RolUsuario.CONDUCTOR
				|| conductorUsuario.getConductor() == null) {
			throw new IllegalArgumentException("Se requiere un usuario con el rol CONDUCTOR activo.");
		}

		BloqueoViajes.LOCK.lock();
		try {
			Viaje viajeActivo = obtenerViajeActivoConductor(conductorUsuario);

			if (viajeActivo == null || viajeActivo.estadoActual() != EstadoViaje.INICIADO) {
				return false;
			}

			// Obtener las calificaciones pendientes.
			CalificacionViaje calificacionCliente = calificacionServicio.obtenerCalificacionCliente(viajeActivo);

			CalificacionViaje calificacionConductor = calificacionServicio.obtenerCalificacionConductor(viajeActivo);

			// Finalizar el viaje y registrar las calificaciones.
			viajeActivo.finalizar(LocalDateTime.now(), calificacionCliente, calificacionConductor);

			calificacionServicio.registrarCalificacionesEnHistorial(viajeActivo);

			return true;
		} finally {
			BloqueoViajes.LOCK.unlock();
		}
	}

	/**
	 * Consulta el viaje activo del usuario cuando actúa como cliente.
	 * Se considera activo un viaje en estado {@code ACEPTADO} o {@code INICIADO}.
	 * La búsqueda se hace bajo el bloqueo compartido de viajes para ser segura
	 * entre hilos.
	 *
	 * @param usuario usuario cuyo viaje activo se desea consultar.
	 * @return el viaje activo, o {@code null} si el usuario no tiene ninguno.
	 * @throws IllegalArgumentException si el usuario es {@code null}.
	 */
	public Viaje consultarViajeActivo(Usuario usuario) {

		if (usuario == null) {
			throw new IllegalArgumentException("El usuario es obligatorio.");
		}

		BloqueoViajes.LOCK.lock();
		try {
			for (Viaje viaje : obtenerViajesUsuario(usuario)) {

				EstadoViaje estado = viaje.estadoActual();

				if (estado == EstadoViaje.SOLICITADO
						|| estado == EstadoViaje.ACEPTADO
						|| estado == EstadoViaje.INICIADO) {
					return viaje;
				}
			}

			return null;
		} finally {
			BloqueoViajes.LOCK.unlock();
		}
	}

	/**
	 * Consulta el viaje activo asociado al conductor.
	 *
	 * @param conductorUsuario usuario cuyo perfil de conductor se desea consultar.
	 * @return viaje aceptado o iniciado del conductor, o null si no tiene uno
	 *         activo.
	 */
	public Viaje consultarViajeActivoConductor(Usuario conductorUsuario) {
		BloqueoViajes.LOCK.lock();
		try {
			return obtenerViajeActivoConductor(conductorUsuario);
		} finally {
			BloqueoViajes.LOCK.unlock();
		}
	}

	/**
	 * Busca el viaje aceptado o iniciado del conductor.
	 * Se llama siempre con el lock tomado.
	 */
	private Viaje obtenerViajeActivoConductor(Usuario conductorUsuario) {

		if (conductorUsuario == null || conductorUsuario.getConductor() == null) {
			return null;
		}

		for (Viaje viaje : conductorUsuario.getConductor().getViajes()) {

			EstadoViaje estado = viaje.estadoActual();

			if (estado == EstadoViaje.ACEPTADO || estado == EstadoViaje.INICIADO) {
				return viaje;
			}
		}

		return null;
	}

	public double obtenerCostoCancelacion(Viaje viaje) {
		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser null.");
		}

		BloqueoViajes.LOCK.lock();
		try {
			return costosCancelacionPorViaje.getOrDefault(viaje.getId(), 0.0);
		} finally {
			BloqueoViajes.LOCK.unlock();
		}
	}

	public boolean tieneDeudaPendiente(Usuario cliente) {
		if (cliente == null) {
			throw new IllegalArgumentException("El cliente es obligatorio.");
		}

		BloqueoViajes.LOCK.lock();
		try {
			return deudasPendientes.containsKey(cliente);
		} finally {
			BloqueoViajes.LOCK.unlock();
		}
	}

	public double obtenerDeudaPendiente(Usuario cliente) {
		if (cliente == null) {
			throw new IllegalArgumentException("El cliente es obligatorio.");
		}

		BloqueoViajes.LOCK.lock();
		try {
			if (!deudasPendientes.containsKey(cliente)) {
				throw new IllegalStateException("El cliente no tiene una deuda pendiente.");
			}

			return deudasPendientes.get(cliente);
		} finally {
			BloqueoViajes.LOCK.unlock();
		}
	}

	public void registrarPagoCancelacion(Usuario cliente) {
		if (cliente == null) {
			throw new IllegalArgumentException("El cliente es obligatorio.");
		}

		BloqueoViajes.LOCK.lock();
		try {
			if (!deudasPendientes.containsKey(cliente)) {
				throw new IllegalStateException("El cliente no tiene una deuda pendiente.");
			}

			deudasPendientes.remove(cliente);
		} finally {
			BloqueoViajes.LOCK.unlock();
		}
	}

	/**
	 * Calcula el costo correspondiente a la cancelación de un viaje, según su
	 * estado anterior y el usuario que solicita la cancelación.
	 *
	 * Para viajes solicitados no se aplica ningún cargo. Para viajes aceptados se
	 * aplica el porcentaje configurado si cancela el cliente. Para viajes iniciados
	 * se calcula un importe proporcional al tiempo transcurrido si cancela el
	 * cliente.
	 *
	 * @param viaje          viaje que se desea cancelar.
	 * @param usuario        usuario que solicita la cancelación.
	 * @param estadoAnterior estado del viaje antes de su cancelación.
	 * @return importe correspondiente al costo de cancelación.
	 */
	private double calcularCostoCancelacion(Viaje viaje, Usuario usuario, EstadoViaje estadoAnterior) {

		if (estadoAnterior == EstadoViaje.SOLICITADO) {
			return 0.0;
		}

		Cotizacion cotizacion = cotizacionesPorViaje.get(viaje.getId());

		if (cotizacion == null) {
			throw new IllegalStateException("No se encontró la cotización del viaje.");
		}

		double costoCancelacionCliente = costoCancelacionViajeAceptado;

		if (estadoAnterior == EstadoViaje.ACEPTADO) {
			if (usuario.getRolActivo() == RolUsuario.CLIENTE) {
				return cotizacion.costoTotal() * costoCancelacionCliente;
			}

			return 0.0;
		}

		if (estadoAnterior == EstadoViaje.INICIADO) {
			if (usuario.getRolActivo() == RolUsuario.CONDUCTOR) {
				return 0.0;
			}

			LocalDateTime fechaInicio = obtenerFechaInicio(viaje);
			LocalDateTime fechaCancelacion = LocalDateTime.now();

			double minutosTranscurridos = java.time.Duration.between(fechaInicio, fechaCancelacion).toSeconds() / 60.0;

			double duracionEstimada = cotizacion.duracionMinutos();

			if (duracionEstimada <= 0) {
				return cotizacion.costoTotal();
			}

			// Calcular costo proporcional a la distancia recorrida.
			double proporcion = minutosTranscurridos / duracionEstimada;

			proporcion = Math.max(0.0, Math.min(proporcion, 1.0));

			return cotizacion.costoTotal() * proporcion;
		}

		return 0.0;
	}

	/**
	 * Busca y obtiene la fecha y hora en que se inició el viaje.
	 *
	 * @param viaje viaje del que se desea obtener la fecha de inicio.
	 * @return fecha y hora del registro correspondiente al estado INICIADO.
	 * @throws IllegalStateException si el viaje no contiene un registro de inicio.
	 */
	private LocalDateTime obtenerFechaInicio(Viaje viaje) {

		for (var registro : viaje.getRegistroViaje()) {
			if (registro.getEstadoViaje() == EstadoViaje.INICIADO) {
				return registro.getFechaHora();
			}
		}

		throw new IllegalStateException("No se encontró el registro de inicio del viaje.");
	}

	/**
	 * Obtiene los viajes asociados al perfil correspondiente al rol activo del
	 * usuario.
	 *
	 * @param usuario usuario del que se desean obtener los viajes.
	 * @return colección de viajes del cliente o conductor.
	 * @throws IllegalArgumentException si el usuario no tiene el perfil
	 *                                  correspondiente.
	 * @throws IllegalStateException    si el usuario no tiene un rol activo válido.
	 */
	private Iterable<Viaje> obtenerViajesUsuario(Usuario usuario) {

		if (usuario.getRolActivo() == RolUsuario.CLIENTE) {

			if (usuario.getCliente() == null) {
				throw new IllegalArgumentException("El usuario no tiene un perfil de cliente.");
			}

			return usuario.getCliente().getViajes();
		}

		if (usuario.getRolActivo() == RolUsuario.CONDUCTOR) {

			if (usuario.getConductor() == null) {
				throw new IllegalArgumentException("El usuario no tiene un perfil de conductor.");
			}

			return usuario.getConductor().getViajes();
		}

		throw new IllegalStateException("El usuario no tiene un rol activo válido.");
	}

	public Cotizacion obtenerCotizacion(Viaje viaje) {

		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser null.");
		}

		BloqueoViajes.LOCK.lock();
		try {
			return cotizacionesPorViaje.get(viaje.getId());
		} finally {
			BloqueoViajes.LOCK.unlock();
		}
	}

	/**
	 * Registra la calificación que el cliente otorga al conductor.
	 */
	public void calificarComoCliente(Usuario cliente, Viaje viaje, CalificacionViaje calificacion) {
		BloqueoViajes.LOCK.lock();
		try {
			calificacionServicio.calificarComoCliente(cliente, viaje, calificacion);
		} finally {
			BloqueoViajes.LOCK.unlock();
		}
	}

	/**
	 * Registra la calificación que el conductor otorga al cliente.
	 */
	public void calificarComoConductor(Usuario conductor, Viaje viaje, CalificacionViaje calificacion) {
		BloqueoViajes.LOCK.lock();
		try {
			calificacionServicio.calificarComoConductor(conductor, viaje, calificacion);
		} finally {
			BloqueoViajes.LOCK.unlock();
		}
	}
}