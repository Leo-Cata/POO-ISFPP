package md.datos.dto;

import java.util.EnumSet;
import java.util.Set;

import md.modelo.*;

public class VehiculoDTO {

	private String patente;
	private String modelo;
	private TipoVehiculo tipoVehiculo;
	private CategoriaVehiculo categoriaVehiculo;
	// Set (conjunto) garantiza la propiedad 'unique' por definicion
	private Set<TipoServicio> tipoServicios;
	private int capacidadPasajeros;

	public VehiculoDTO(String patente, String modelo, TipoVehiculo tipoVehiculo, CategoriaVehiculo categoriaVehiculo,
			Set<TipoServicio> tipoServicios, int capacidadPasajeros) {
		this.patente = patente;
		this.modelo = modelo;
		this.tipoVehiculo = tipoVehiculo;
		this.categoriaVehiculo = categoriaVehiculo;
		
		if (tipoServicios.isEmpty()) {
		    this.tipoServicios = EnumSet.noneOf(TipoServicio.class);
		} else {
		    this.tipoServicios = EnumSet.copyOf(tipoServicios);
		}
		
		this.capacidadPasajeros = capacidadPasajeros;
	}

	public String getPatente() {
		return patente;
	}

	public String getModelo() {
		return modelo;
	}

	public TipoVehiculo getTipoVehiculo() {
		return tipoVehiculo;
	}

	public CategoriaVehiculo getCategoriaVehiculo() {
		return categoriaVehiculo;
	}

	public Set<TipoServicio> getTipoServicios() {
		if (tipoServicios.isEmpty()) {
		    return EnumSet.noneOf(TipoServicio.class);
		} else {
		    return EnumSet.copyOf(tipoServicios);
		}
	}

	public int getCapacidadPasajeros() {
		return capacidadPasajeros;
	}
}
