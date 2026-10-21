package md.datos.dto;

import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;

public class ServicioDTO {
	private String nombre;
	private double tarifaBase;
	private double precioPorKm;
	private double precioPorMinuto;
	private TipoServicio tipoServicio;
	private TipoVehiculo tipoVehiculo;
	private CategoriaVehiculo categoriaVehiculo;

	public ServicioDTO(String nombre, double tarifaBase, double precioPorKm, double precioPorMinuto,
			TipoServicio tipoServicio, TipoVehiculo tipoVehiculo, CategoriaVehiculo categoriaVehiculo) {
		this.nombre = nombre;
		this.tarifaBase = tarifaBase;
		this.precioPorKm = precioPorKm;
		this.precioPorMinuto = precioPorMinuto;
		this.tipoServicio = tipoServicio;
		this.tipoVehiculo = tipoVehiculo;
		this.categoriaVehiculo = categoriaVehiculo;
	}

	public String getNombre() {
		return nombre;
	}

	public double getTarifaBase() {
		return tarifaBase;
	}

	public double getPrecioPorKm() {
		return precioPorKm;
	}

	public double getPrecioPorMinuto() {
		return precioPorMinuto;
	}

	public TipoServicio getTipoServicio() {
		return tipoServicio;
	}

	public TipoVehiculo getTipoVehiculo() {
		return tipoVehiculo;
	}

	public CategoriaVehiculo getCategoriaVehiculo() {
		return categoriaVehiculo;
	}

}
