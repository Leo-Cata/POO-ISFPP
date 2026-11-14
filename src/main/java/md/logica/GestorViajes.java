package md.logica;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import md.aplicacion.ContextoAplicacion;
import md.config.CargarParametros;
import md.modelo.EstadoViaje;
import md.modelo.RolUsuario;
import md.modelo.Servicio;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Viaje;
import md.rutas.api.RutaNoDisponibleException;
import md.rutas.api.RutasViaje;

/**
 * Coordina los casos de uso relacionados con los viajes.
 *
 * No interactúa con el usuario ni presenta información por pantalla. Esa
 * responsabilidad corresponde a las interfaces de usuario.
 */
public class GestorViajes {
	private final Map<UUID, Cotizacion> cotizacionesPorViaje = new HashMap<>();
	private final Map<UUID, Double> costosCancelacionPorViaje = new HashMap<>();

	private final CalificacionServicio calificacionServicio = new CalificacionServicio();

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

		double velocidadMedia = CargarParametros.getVelocidadMediaKmH();

		if (!Double.isFinite(velocidadMedia) || velocidadMedia <= 0) {
			throw new IllegalStateException("La velocidad media configurada no es válida.");
		}

		double distanciaKm = origen.calcularDistancia(destino);
		double duracionMinutos = (distanciaKm / velocidadMedia) * 60.0;

		double costoTotal = servicio.calcularCosto(distanciaKm, duracionMinutos);

		return new Cotizacion(distanciaKm, duracionMinutos, costoTotal);
	}

	/**
	 * Procesa una solicitud de viaje.
	 *
	 * Selecciona el conductor compatible más cercano, calcula los recorridos viales
	 * y registra el viaje como solicitado y aceptado.
	 *
	 * Las rutas se calculan antes de crear el viaje para evitar registrar una
	 * solicitud cuando no se pudo obtener un recorrido.
	 *
	 * @return el resultado de la solicitud o null si no hay conductores compatibles
	 *         disponibles.
	 */
	public ResultadoSolicitud procesarSolicitudViaje(Usuario clienteUsuario, Servicio servicio, Ubicacion origen,
			Ubicacion destino) throws RutaNoDisponibleException {

		if (clienteUsuario == null || servicio == null || origen == null || destino == null) {
			throw new IllegalArgumentException("Los datos de la solicitud son obligatorios.");
		}
		if (clienteUsuario.getRolActivo() != RolUsuario.CLIENTE) {
			throw new IllegalStateException("El usuario debe tener activo el rol CLIENTE para solicitar un viaje.");
		}
		if (clienteUsuario.getCliente() == null) {
			throw new IllegalArgumentException("El usuario no tiene perfil de cliente.");
		}

		if (clienteUsuario.getCliente().enViaje()) {
			throw new IllegalStateException("El cliente ya tiene un viaje activo.");
		}

		// Calcular la cotización definitiva (no toca estado compartido).
		Cotizacion cotizacion = calcularCotizacion(servicio, origen, destino);

		// Obtener los datos de la aplicación.
		var datos = ContextoAplicacion.getDatosIniciales();

		if (datos == null) {
			throw new IllegalStateException("Los datos iniciales no están disponibles.");
		}

		// Desde acá hasta el final se decide la asignación y se cambian estados.
		// Todo bajo el mismo lock que usa la simulación.
		BloqueoViajes.LOCK.lock();
		try {
			// Se vuelve a chequear dentro del lock: otro hilo pudo crear un viaje mientras
			// tanto.
			if (clienteUsuario.getCliente().enViaje()) {
				throw new IllegalStateException("El cliente ya tiene un viaje activo.");
			}

			// Seleccionar el conductor compatible más cercano.
			Movilidad movilidad = new Movilidad(datos.getUsuarios());

			Usuario conductorSeleccionado = movilidad.seleccionarConductor(servicio, origen);

			if (conductorSeleccionado == null) {
				return null;
			}

			// Calcular los dos recorridos viales.
			RutasViaje rutas = new PlanificacionViajeService(ContextoAplicacion.getMotorVial())
					.planificar(conductorSeleccionado.getConductor().getVehiculoActivo(), origen, destino);

			// Crear y registrar el viaje.
			Viaje nuevoViaje = new Viaje(clienteUsuario, origen, destino, servicio);

			nuevoViaje.solicitar(LocalDateTime.now());

			try {

				nuevoViaje.aceptar(LocalDateTime.now(), conductorSeleccionado);

				cotizacionesPorViaje.put(nuevoViaje.getId(), cotizacion);
			} catch (RuntimeException e) {

				// Si no pudo aceptarse, no dejamos la solicitud pendiente.
				if (nuevoViaje.estadoActual() == EstadoViaje.SOLICITADO) {
					nuevoViaje.rechazar(LocalDateTime.now());
				}

				throw e;
			}

			return new ResultadoSolicitud(nuevoViaje, rutas, cotizacion, conductorSeleccionado);
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

			calificacionServicio.completarCalificacionesPendientes(viajeActivo);

			// Finalizar el viaje y registrar las calificaciones.
			viajeActivo.finalizar(LocalDateTime.now(), viajeActivo.getCalificacionCliente(),
					viajeActivo.getCalificacionConductor());

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

				if (estado == EstadoViaje.ACEPTADO || estado == EstadoViaje.INICIADO) {
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

		double costoCancelacionCliente = CargarParametros.getCostoCancelacionViajeAceptado();

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

}