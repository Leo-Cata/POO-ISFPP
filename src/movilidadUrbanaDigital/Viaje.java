package movilidadUrbanaDigital;

import java.time.LocalDateTime;
import java.util.UUID;

import net.datastructures.LinkedPositionalList;

public class Viaje {
	private UUID id;
	private String motivoCancelacion;
	private RolUsuario rolCancela;
	private Usuario cliente, conductor;
	private Vehiculo vehiculo;
	private Ubicacion origen, destino;
	private LinkedPositionalList<RegistroViaje> registroViaje; // segun gemeni se puede usar un ArrayList directamennte
	private Servicio servicio;
	private CalificacionViaje calificacionCondutor, calificacionCliente;

	public Viaje(UUID id, Usuario cliente, Ubicacion origen, Ubicacion destino, Servicio servicio) {
		this.id = id; // ia recomienda usar UUID.randomUUID();
		this.cliente = cliente;
		this.origen = origen;
		this.destino = destino;
		this.servicio = servicio;

		this.registroViaje = new LinkedPositionalList<RegistroViaje>(); // se inicializa con 0 elementos
		this.conductor = null; // se agrega al aceptar el viaje
		this.vehiculo = null; // se agrega al aceptar el viaje
		this.motivoCancelacion = null; // se agrega al cancelar un viaje
		this.calificacionCliente = null; // se agrega al finalizar el viaje
		this.calificacionCondutor = null; // se agrega al finalizar el viaje
	}

	/**
	 * se solicita un nuevo viaje
	 * 
	 * @param fechaHora LocalDateTime de la solicitud
	 * @throws IllegalArgumentException si la fechaHora es invalida
	 * @throws IllegalStateException    si ya se encuentran valores en la lista, es
	 *                                  decir ya se ha solicitado y hay un vieja en
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

		this.registroViaje.addLast(new RegistroViaje(fechaHora, EstadoViaje.SOLICITADO));
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
		if (conductor == null) {
			throw new IllegalArgumentException("El conductor es invalido");
		}
		if (registroViaje.last().getElement().getEstado() != EstadoViaje.SOLICITADO) {
			throw new IllegalStateException("El viaje no se encuentra solicitado");
		}
		this.conductor = conductor;
		this.registroViaje.addLast(new RegistroViaje(fechaHora, EstadoViaje.ACEPTADO));
	}

	/**
	 * setea el viaje como INICIADO (se sube el pasajero/entrega el paquete)
	 * 
	 * @param fechaHora LocalDateTime en que se inicio
	 * @throws IllegalArgumentException si la hora es invalida
	 * @throws IllegalStateArgument     si el estado anterior no es ACEPTADO
	 */

	public void iniciar(LocalDateTime fechaHora) {
		if (fechaHora == null) {
			throw new IllegalArgumentException("La hora y fecha son invalidas");
		}
		if (registroViaje.last().getElement().getEstado() != EstadoViaje.ACEPTADO) {
			throw new IllegalStateException("El viaje no se encuentra aceptado");
		}

		this.registroViaje.addLast(new RegistroViaje(fechaHora, EstadoViaje.INICIADO));
	}

	public void finalizar(LocalDateTime fechaHora, CalificacionViaje calificacionConductor,
			CalificacionViaje calificacionCliente) {
	}

	public void cancelar(LocalDateTime fehcaHora, Usuario usuario, String motivo) {

	}

	public void rechazar(LocalDateTime fechaHora) {

	}

	public EstadoViaje estadoActual() {
		return null;

	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public CalificacionViaje getCalificacionCondutor() {
		return calificacionCondutor;
	}

	public void setCalificacionCondutor(CalificacionViaje calificacionCondutor) {
		this.calificacionCondutor = calificacionCondutor;
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

	public LinkedPositionalList<RegistroViaje> getRegistroViaje() {
		return registroViaje;
	}

	public void agregarRegistroViaje(RegistroViaje nuevoRegistroViaje) {
		this.registroViaje.addLast(nuevoRegistroViaje);
	}

	public Servicio getServicio() {
		return servicio;
	}

	public void setServicio(Servicio servicio) {
		this.servicio = servicio;
	}
}
