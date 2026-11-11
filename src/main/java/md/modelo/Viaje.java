package md.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import md.rutas.api.RutasViaje;

public class Viaje {
	private UUID id;
	private double costoTotal;
	private double duracionEstimadaMinutos;
	private double distanciaTotal;
	private double costoCancelacion;
	private String motivoCancelacion;
	private RolUsuario rolCancela;
	private Usuario cliente, conductor;
	private Vehiculo vehiculo;
	private Ubicacion origen, destino;
	private ArrayList<RegistroViaje> registroViaje;
	private Servicio servicio;
	private CalificacionViaje calificacionConductor, calificacionCliente;
	// recorridos calculados al asignar el conductor: acercamiento y traslado
	private RutasViaje rutas;

	public Viaje(Usuario cliente, Ubicacion origen, Ubicacion destino, Servicio servicio) {
		this.id = UUID.randomUUID();
		this.cliente = cliente;
		this.origen = origen;
		this.destino = destino;
		this.servicio = servicio;

		this.registroViaje = new ArrayList<RegistroViaje>(); // se inicializa con 0 elementos
		this.conductor = null; // se agrega al aceptar el viaje
		this.vehiculo = null; // se agrega al aceptar el viaje
		this.motivoCancelacion = null; // se agrega al cancelar un viaje
		this.calificacionCliente = null; // se agrega al finalizar el viaje
		this.calificacionConductor = null; // se agrega al finalizar el viaje

	}

	/**
	 * se solicita un nuevo viaje
	 * 
	 * @param fechaHora LocalDateTime de la solicitud
	 * @throws IllegalArgumentException si la fechaHora es invalida
	 * @throws IllegalStateException    si ya se encuentran valores en la lista, es
	 *                                  decir ya se ha solicitado y hay un viaje en
	 *                                  alguno de los estados
	 */
	public void solicitar(LocalDateTime fechaHora) {
		if (fechaHora == null) {
			throw new IllegalArgumentException("La fecha y hora son invalidas");
		}

		// para solicitar un vieja la lista debe estar vacia
		if (!registroViaje.isEmpty()) {
			throw new IllegalStateException("Ya se ha solicitado un vieja");
		}

		if (this.cliente.getCliente().enViaje()) {
			throw new IllegalStateException("El cliente ya tiene un viaje activo.");
		}

		agregarRegistroViaje(new RegistroViaje(fechaHora, EstadoViaje.SOLICITADO));
		cliente.getCliente().agregarViajes(this); // agrega el viaje actual
	}

	/**
	 * se aceptara el viaje, asignando asi un conductor
	 * 
	 * @param fechaHora LocalDateTime de cuando se acepto
	 * @param conductor Usuario de tipo conductor que sera asignadp
	 * @throws IllegalArgumentException si la fechaHora o conductor son invalidos
	 * @throws IllegalStateException    si el ultimo estado no es de tipo SOLICITADO
	 */
	public void aceptar(LocalDateTime fechaHora, Usuario conductor) {
		if (fechaHora == null) {
			throw new IllegalArgumentException("La fecha y hora son invalidas");
		}
		// verificar que el objeto usuario no sea null
		if (conductor == null) {
			throw new IllegalArgumentException("El conductor es invalido");
		}
		if (conductor.equals(this.cliente)) {
			throw new IllegalArgumentException("Un usuario no puede aceptar su propio viaje.");
		}
		// verificar que el usuario tenga el rol de conductor
		if (conductor.getRolActivo() != RolUsuario.CONDUCTOR) {
			throw new IllegalArgumentException("El usuario ingresado no tiene perfil de conductor");
		}
		if (estadoActual() != EstadoViaje.SOLICITADO) {
			throw new IllegalStateException("El viaje no se encuentra solicitado");
		}
		if (conductor.getConductor().getEstadoConductor() != EstadoConductor.DISPONIBLE) {
			throw new IllegalStateException("El conductor no se encuentra en estado disponible");
		}

		Vehiculo vehiculoActivo = conductor.getConductor().getVehiculoActivo();
		if (vehiculoActivo == null) {
			throw new IllegalStateException("El conductor no tiene un vehículo activo asignado.");
		}

		boolean tipoCoincide = vehiculoActivo.getTipoVehiculo().equals(servicio.getTipoVehiculo());
		boolean servicioCoincide = vehiculoActivo.getTipoServicios().contains(servicio.getTipoServicio());
		int pedida = servicio.getCategoriaVehiculo().getValor();
		int maxima = vehiculoActivo.getCategoriaVehiculo().getValor();
		int minima = conductor.getConductor().getCategoriaVehiculoActivo().getValor();
		boolean categoriaValida = pedida <= maxima && pedida >= minima;

		if (!tipoCoincide || !servicioCoincide || !categoriaValida) {
			throw new IllegalStateException("El vehículo no cumple con los requisitos del servicio solicitado.");
		}

		this.conductor = conductor;
		this.vehiculo = vehiculoActivo;
		agregarRegistroViaje(new RegistroViaje(fechaHora, EstadoViaje.ACEPTADO));
		conductor.getConductor().setEstado(EstadoConductor.VIAJE_A_ORIGEN);
		conductor.getConductor().agregarViaje(this);

	}

	/**
	 * setea el viaje como INICIADO (se sube el pasajero/entrega el paquete)
	 * 
	 * @param fechaHora LocalDateTime en que se inicio
	 * @throws IllegalArgumentException si la fechaHora es invalida
	 * @throws IllegalStateException    si el estado anterior no es ACEPTADO
	 */

	public void iniciar(LocalDateTime fechaHora) {
		if (fechaHora == null) {
			throw new IllegalArgumentException("La hora y fecha son invalidas");
		}
		if (estadoActual() != EstadoViaje.ACEPTADO) {
			throw new IllegalStateException("El viaje no se encuentra aceptado");
		}

		agregarRegistroViaje(new RegistroViaje(fechaHora, EstadoViaje.INICIADO));

		conductor.getConductor().setEstado(EstadoConductor.VIAJE_A_DESTINO);
	}

	/**
	 * setea el estado de viaje como FINALIZADO, agregando las calificaciones
	 * 
	 * @param fechaHora             LocalDateTime de la hora de finalizacion
	 * @param calificacionConductor enum int calificacion
	 * @param calificacionCliente   enum int calificacion
	 * @throws IllegalStateException    si el estado anterior no es INICIADO
	 * @throws IllegalArgumentException si la fechaHora es null
	 */
	public void finalizar(LocalDateTime fechaHora, CalificacionViaje calificacionCliente,
			CalificacionViaje calificacionConductor) {
		if (fechaHora == null) {
			throw new IllegalArgumentException("La hora y fecha son invalidas");
		}
		if (estadoActual() != EstadoViaje.INICIADO) {
			throw new IllegalStateException("El viaje no se encuentra iniciado");
		}
		if (calificacionCliente == null || calificacionConductor == null) {
			throw new IllegalArgumentException("Calificaciones invalidas");
		}

		this.calificacionCliente = calificacionCliente;
		this.calificacionConductor = calificacionConductor;

		agregarRegistroViaje(new RegistroViaje(fechaHora, EstadoViaje.FINALIZADO));

		conductor.getConductor().setEstado(EstadoConductor.DISPONIBLE);

	}

	/**
	 * setea un viaje como cancelado, solo si los parametros no son null, y si los
	 * estados anteriores no son FINALIZADO y CANCELADO
	 * 
	 * @param fechaHora LocalDateTime de cuando se cancela
	 * @param usuario   Usuario que cancela el viaje
	 * @param motivo    String descripcion de por que se cancelo el viaje
	 * @throws IllegalArgumentException si la fehcaHora es null
	 * @throws IllegalStateException    si el usuario es invalido, o si el estado
	 *                                  actual es FINALIZADO o CANCELADO
	 */
	public void cancelar(LocalDateTime fechaHora, Usuario usuario, String motivo) {
		if (fechaHora == null) {
			throw new IllegalArgumentException("La hora y fecha son invalidas");
		}
		if (usuario == null) {
			throw new IllegalArgumentException("El usuario es invalido");
		}

		if (motivo == null) {
			throw new IllegalArgumentException("El motivo no puede ser null");
		}

		if (estadoActual() == null) {
			throw new IllegalStateException("El viaje todavia no fue solicitado");
		}

		if (estadoActual() == EstadoViaje.FINALIZADO || estadoActual() == EstadoViaje.CANCELADO
				|| estadoActual() == EstadoViaje.RECHAZADO) {
			throw new IllegalStateException("El viaje se encuentra finalizado, cancelado o rechazado");
		}

		boolean cancelaCliente = usuario.equals(this.cliente);
		boolean cancelaConductor = this.conductor != null && usuario.equals(this.conductor);
		if (!cancelaCliente && !cancelaConductor) {
			throw new IllegalArgumentException("Solo el cliente o el conductor del viaje pueden cancelarlo.");
		}

		this.motivoCancelacion = motivo;
		this.rolCancela = cancelaCliente ? RolUsuario.CLIENTE : RolUsuario.CONDUCTOR;
		agregarRegistroViaje(new RegistroViaje(fechaHora, EstadoViaje.CANCELADO));

		if (this.conductor != null && this.conductor.getConductor() != null) {
			conductor.getConductor().setEstado(EstadoConductor.DISPONIBLE);
		}

	}

	/**
	 * rechaza un viaje que se encuentra solicidado
	 * 
	 * @param fechaHora LocalDatetime de cuando se rechazo
	 * @throws IllegalArgumentException si la fechaHora es null
	 * @throws IllegalStateException    si el estado actual no es SOLICITADO
	 */
	public void rechazar(LocalDateTime fechaHora) {
		if (fechaHora == null) {
			throw new IllegalArgumentException("La fecha y hora son invalidos");
		}
		if (estadoActual() != EstadoViaje.SOLICITADO) {
			throw new IllegalStateException("El viaje no se encuentra SOLICITADO");
		}

		agregarRegistroViaje(new RegistroViaje(fechaHora, EstadoViaje.RECHAZADO));
	}

	/**
	 * metodo para consultar el estado accual
	 * 
	 * @return EstadoViaje enum del ultimo estado, o null si no hay estados en la
	 *         lista
	 */
	public EstadoViaje estadoActual() {
		if (registroViaje == null || registroViaje.isEmpty()) {
			return null;
		}
		return registroViaje.getLast().getEstado();

	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	/**
	 * Obtiene el costo total cotizado originalmente para el viaje.
	 *
	 * @return costo total del viaje.
	 */
	public double getCostoTotal() {
		return costoTotal;
	}

	/**
	 * Establece el costo total correspondiente a la cotización inicial del viaje.
	 *
	 * @param costoTotal importe total cotizado.
	 * @throws IllegalArgumentException si el costo es negativo.
	 */
	public void setCostoTotal(double costoTotal) {
		if (costoTotal < 0) {
			throw new IllegalArgumentException("El costo no puede ser negativo.");
		}

		this.costoTotal = costoTotal;
	}

	/**
	 * Obtiene la duración estimada del viaje, calculada al momento de solicitarlo.
	 *
	 * @return duración estimada del viaje expresada en minutos.
	 */
	public double getDuracionEstimadaMinutos() {
		return duracionEstimadaMinutos;
	}

	/**
	 * Establece la duración estimada del viaje según la cotización inicial.
	 *
	 * @param duracionEstimadaMinutos duración estimada expresada en minutos.
	 * @throws IllegalArgumentException si la duración no es válida.
	 */
	public void setDuracionEstimadaMinutos(double duracionEstimadaMinutos) {
		if (duracionEstimadaMinutos < 0) {
			throw new IllegalArgumentException("La duracion no puede ser negativa.");
		}
		this.duracionEstimadaMinutos = duracionEstimadaMinutos;
	}

	/**
	 * Obtiene la distancia total del viaje.
	 *
	 * @return distancia total del viaje expresada en km.
	 */
	public double getDistanciaTotal() {
		return distanciaTotal;
	}

	/**
	 * Establece la distancia total del viaje según la cotización inicial.
	 *
	 * @param distanciaTotal distancia total expresada en km.
	 * @throws IllegalArgumentException si la distancia no es válida.
	 */
	public void setDistanciaTotal(double distanciaTotal) {
		if (distanciaTotal < 0) {
			throw new IllegalArgumentException("La distancia total del recorrido no puede ser negativa.");
		}

		this.distanciaTotal = distanciaTotal;
	}

	/**
	 * Obtiene el importe calculado por la cancelación del viaje.
	 *
	 * @return costo de cancelación del viaje.
	 */
	public double getCostoCancelacion() {
		return costoCancelacion;
	}

	/**
	 * Establece el importe correspondiente a la cancelación del viaje.
	 *
	 * @param costoCancelacion importe calculado por la cancelación.
	 * @throws IllegalArgumentException si el costo es negativo.
	 */
	public void setCostoCancelacion(double costoCancelacion) {
		if (costoCancelacion < 0) {
			throw new IllegalArgumentException("El costo de cancelacion no puede ser negativo.");
		}
		this.costoCancelacion = costoCancelacion;
	}

	public String getMotivoCancelacion() {
		return motivoCancelacion;
	}

	public void setMotivoCancelacion(String motivoCancelacion) {
		this.motivoCancelacion = motivoCancelacion;
	}

	public Usuario getCliente() {
		return cliente;
	}

	public void setCliente(Usuario cliente) {
		this.cliente = cliente;
	}

	public Usuario getConductor() {
		return conductor;
	}

	public void setConductor(Usuario conductor) {
		this.conductor = conductor;
	}

	public RolUsuario getRolCancela() {
		return rolCancela;
	}

	public void setRolCancela(RolUsuario rolCancela) {
		this.rolCancela = rolCancela;
	}

	public CalificacionViaje getCalificacionConductor() {
		return calificacionConductor;
	}

	public void setCalificacionConductor(CalificacionViaje calificacionConductor) {
		this.calificacionConductor = calificacionConductor;
	}

	public CalificacionViaje getCalificacionCliente() {
		return calificacionCliente;
	}

	public void setCalificacionCliente(CalificacionViaje calificacionCliente) {
		this.calificacionCliente = calificacionCliente;
	}

	public Vehiculo getVehiculo() {
		return vehiculo;
	}

	public void setVehiculo(Vehiculo vehiculo) {
		this.vehiculo = vehiculo;
	}

	public Ubicacion getOrigen() {
		return origen;
	}

	public void setOrigen(Ubicacion origen) {
		this.origen = origen;
	}

	public Ubicacion getDestino() {
		return destino;
	}

	public void setDestino(Ubicacion destino) {
		this.destino = destino;
	}

	public ArrayList<RegistroViaje> getRegistroViaje() {

		ArrayList<RegistroViaje> copiaRegistroViaje = new ArrayList<RegistroViaje>();

		copiaRegistroViaje.addAll(registroViaje);

		return copiaRegistroViaje;
	}

	public void agregarRegistroViaje(RegistroViaje nuevoRegistroViaje) {
		this.registroViaje.addLast(nuevoRegistroViaje);
	}
	
	public RutasViaje getRutas() {
		return rutas;
	}

	public void setRutas(RutasViaje rutas) {
		this.rutas = rutas;
	}

	public Servicio getServicio() {
		return servicio;
	}

	public void setServicio(Servicio servicio) {
		this.servicio = servicio;
	}

	// dos viajes son iguales si tienen el mismo UUID
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Viaje other = (Viaje) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Viaje [id=" + id + ", motivoCancelacion=" + motivoCancelacion + ", rolCancela=" + rolCancela
				+ ", cliente=" + cliente + ", conductor=" + conductor + ", vehiculo=" + vehiculo + ", origen=" + origen
				+ ", destino=" + destino + ", registroViaje=" + registroViaje + ", servicio=" + servicio
				+ ", calificacionConductor=" + calificacionConductor + ", calificacionCliente=" + calificacionCliente
				+ "]";
	}

}
