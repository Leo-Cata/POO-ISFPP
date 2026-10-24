package md.datos.dto;

import java.util.EnumSet;
import java.util.Set;

import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;

/**
 * Objeto de transferencia de datos (DTO) que representa la información de un
 * vehículo. Encapsula los atributos esenciales de un vehículo para su
 * transporte entre capas de la aplicación, garantizando la inmutabilidad de las
 * colecciones internas mediante copias defensivas.
 */
public class VehiculoDTO {

	private String patente;
	private String modelo;
	private TipoVehiculo tipoVehiculo;
	private CategoriaVehiculo categoriaVehiculo;
	// Set (conjunto) garantiza la propiedad 'unique' por definicion
	private Set<TipoServicio> tipoServicios;
	private int capacidadPasajeros;

	/**
	 * Construye una nueva instancia de {@code VehiculoDTO} con todos los datos del
	 * vehículo. Si el conjunto de tipos de servicio recibido está vacío, inicializa
	 * internamente un {@code EnumSet} vacío; de lo contrario, realiza una copia
	 * defensiva del conjunto.
	 *
	 * @param patente            la patente o matrícula identificadora del vehículo.
	 * @param modelo             el modelo o denominación comercial del vehículo.
	 * @param capacidadPasajeros el número máximo de pasajeros que puede transportar
	 *                           el vehículo.
	 * @param tipoVehiculo       el tipo al que pertenece el vehículo (por ejemplo,
	 *                           automóvil, camión).
	 * @param categoriaVehiculo  la categoría de clasificación del vehículo (por
	 *                           ejemplo, particular, comercial).
	 * @param tipoServicios      el conjunto de tipos de servicio habilitados para
	 *                           el vehículo.
	 */
	public VehiculoDTO(String patente, String modelo, int capacidadPasajeros, TipoVehiculo tipoVehiculo,
			CategoriaVehiculo categoriaVehiculo, Set<TipoServicio> tipoServicios) {
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

	/**
	 * Retorna la patente del vehículo.
	 *
	 * @return cadena de texto con la patente identificadora del vehículo.
	 */
	public String getPatente() {
		return patente;
	}

	/**
	 * Retorna el modelo del vehículo.
	 *
	 * @return cadena de texto con el modelo o denominación comercial del vehículo.
	 */
	public String getModelo() {
		return modelo;
	}

	/**
	 * Retorna el tipo del vehículo.
	 *
	 * @return valor de la enumeración {@code TipoVehiculo} que clasifica al
	 *         vehículo.
	 */
	public TipoVehiculo getTipoVehiculo() {
		return tipoVehiculo;
	}

	/**
	 * Retorna la categoría del vehículo.
	 *
	 * @return valor de la enumeración {@code CategoriaVehiculo} que indica la
	 *         categoría del vehículo.
	 */
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

	/**
	 * Retorna la capacidad máxima de pasajeros del vehículo.
	 *
	 * @return número entero que representa la cantidad máxima de pasajeros que
	 *         puede transportar el vehículo.
	 */
	public int getCapacidadPasajeros() {
		return capacidadPasajeros;
	}
}
