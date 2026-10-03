package movilidadUrbanaDigital;

import java.time.LocalDateTime;
import java.util.UUID;

import net.datastructures.LinkedPositionalList;

public class Viaje {

	private UUID id;
	private String motivoCancelacion;
	private Usuario cliente;
	private Usuario conductor;
	private RolUsuario rolCancela;
	private CalificacionViaje calificacionCondcutor;
	private CalificacionViaje calificacionCliente;
	private Vehiculo vehiculo;
	private Ubicacion origen;
	private Ubicacion destino;
	private LinkedPositionalList<RegistroViaje> registroViaje;
	private Servicio servicio;

	public Viaje(UUID id, String motivoCancelacion, Usuario cliente, Usuario conductor, RolUsuario rolCancela,
			CalificacionViaje calificacionCondcutor, CalificacionViaje calificacionCliente, Vehiculo vehiculo,
			Ubicacion origen, Ubicacion destino, Servicio servicio) {
		super();
		this.id = id;
		this.motivoCancelacion = motivoCancelacion;
		this.cliente = cliente;
		this.conductor = conductor;
		this.rolCancela = rolCancela;
		this.calificacionCondcutor = calificacionCondcutor;
		this.calificacionCliente = calificacionCliente;
		this.vehiculo = vehiculo;
		this.origen = origen;
		this.destino = destino;
		this.servicio = servicio;
		this.registroViaje = new LinkedPositionalList<RegistroViaje>();
	}

	public void solicitar(LocalDateTime fechaHora) {
	}

	public void aceptar(LocalDateTime fechaHora, Conductor conductor) {

	}

	public void iniciar(LocalDateTime fechaHora) {

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

	public CalificacionViaje getCalificacionCondcutor() {
		return calificacionCondcutor;
	}

	public void setCalificacionCondcutor(CalificacionViaje calificacionCondcutor) {
		this.calificacionCondcutor = calificacionCondcutor;
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
