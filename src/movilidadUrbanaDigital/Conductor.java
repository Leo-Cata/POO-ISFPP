package movilidadUrbanaDigital;

import java.util.UUID;

import net.datastructures.Entry;
import net.datastructures.ProbeHashMap;

public class Conductor {

	private String licenciaConducir;
	private CategoriaVehiculo categoriaVehiculoActivo;
	private ProbeHashMap<String, Vehiculo> vehiculos;// alamcenar los autos en un mapa y como clave la patente
	private ProbeHashMap<UUID, Viaje> viajes; // almacenar los vaijes en un mapa y como clave el UUID
	private EstadoConductor estado;
	private Vehiculo vehiculoActivo;

	public Conductor(String licenciaConducir, CategoriaVehiculo categoriaVehiculoActivo, Vehiculo vehiculo,
			EstadoConductor estado) {
		super();
		this.licenciaConducir = licenciaConducir;
		this.categoriaVehiculoActivo = categoriaVehiculoActivo;
		this.estado = estado;
		this.viajes = new ProbeHashMap<>();
		this.vehiculos = new ProbeHashMap<>();
		this.vehiculoActivo = vehiculo;
		agregarVehiculo(vehiculo);
	}

	/**
	 * agrega un vehiculo al usuario de tipo conductor
	 * 
	 * @param vehiculo el cual sera agregado, obteniendo la patente del mismo que se
	 *                 usara como llave del mapa
	 */
	public void agregarVehiculo(Vehiculo vehiculo) {
		if (vehiculo == null)
			throw new IllegalArgumentException("El vehiculo no puede ser null");
		
		if(vehiculos.get(vehiculo.getPatente())!= null)
			throw new IllegalArgumentException("El vehiculo ya esta registrado.");
		
		this.vehiculos.put(vehiculo.getPatente(), vehiculo);
	}

	/**
	 * agrega un viaje al mapa de viajes
	 * 
	 * @param viaje del cual se obtiene us UUID como llave
	 */
	public void agregarViaje(Viaje viaje) {

		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser null");
		}

		if(viajes.get(viaje.getId()) != null)
			throw new IllegalArgumentException("El viaje ya esta registrado.");
		
		this.viajes.put(viaje.getId(), viaje);
	}

	/**
	 * chequea el estado actual del conductor
	 * 
	 * @return true el conductor se encuentra conduciendo al origen/destino
	 */
	public boolean enViaje() {
		return this.estado == EstadoConductor.VIAJE_A_ORIGEN || this.estado == EstadoConductor.VIAJE_A_DESTINO;
	}

	// setters y getters de conductor

	public Vehiculo getVehiculoActivo() {
		return vehiculoActivo;
	}

	public void setVehiculoActivo(Vehiculo vehiculoActivo) {
		this.vehiculoActivo = vehiculoActivo;
	}

	public String getLicenciaConducir() {
		return licenciaConducir;
	}

	public void setLicenciaConducir(String licenciaConducir) {
		this.licenciaConducir = licenciaConducir;
	}

	public CategoriaVehiculo getCategoriaVehiculoActivo() {
		return categoriaVehiculoActivo;
	}

	public void setCategoriaVehiculoActivo(CategoriaVehiculo categoriaVehiculoActivo) {
		this.categoriaVehiculoActivo = categoriaVehiculoActivo;
	}

	public EstadoConductor getEstado() {
		return estado;
	}

	public void setEstado(EstadoConductor estado) {
		this.estado = estado;
	}

	public ProbeHashMap<String, Vehiculo> getVehiculos() {
		
		ProbeHashMap<String, Vehiculo> vehiculosCopia = new ProbeHashMap<String, Vehiculo>();
		
		for(Entry<String, Vehiculo> vehic : vehiculos.entrySet())
			vehiculosCopia.put(vehic.getKey(), vehic.getValue());
		//se devuelve una copia de vehiculos
		return vehiculosCopia ;
	}

	public ProbeHashMap<UUID, Viaje> getViajes() {
		
		ProbeHashMap<UUID, Viaje> viajesCopia = new ProbeHashMap<UUID, Viaje>();
		
		for(Entry<UUID, Viaje> viaj : viajes.entrySet())
			viajesCopia.put(viaj.getKey(), viaj.getValue());
		//se devuelve una copia de viajes
		return viajesCopia;
	}

	@Override
	public String toString() {
		return "Conductor [licenciaConducir=" + licenciaConducir + ", categoriaVehiculoActivo="
				+ categoriaVehiculoActivo + ", vehiculos=" + vehiculos + ", viajes=" + viajes + ", estado=" + estado
				+ "]";
	}

}
