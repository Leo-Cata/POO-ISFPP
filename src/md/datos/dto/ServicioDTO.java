package md.datos.dto;

import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;

/**
 * Objeto de transferencia de datos (DTO) que encapsula la información de un
 * servicio de transporte, incluyendo su tarifa, tipo de vehículo y categoría
 * asociada.
 */
public class ServicioDTO {
	private String nombre;
	private double tarifaBase;
	private double precioPorKm;
	private double precioPorMinuto;
	private TipoServicio tipoServicio;
	private TipoVehiculo tipoVehiculo;
	private CategoriaVehiculo categoriaVehiculo;

	/**
	 * Construye una instancia de {@code ServicioDTO} con todos los datos necesarios
	 * para representar un servicio de transporte.
	 *
	 * @param nombre            nombre identificador del servicio.
	 * @param tarifaBase        tarifa fija base que se aplica al inicio del
	 *                          servicio.
	 * @param precioPorKm       costo adicional cobrado por cada kilómetro
	 *                          recorrido.
	 * @param precioPorMinuto   costo adicional cobrado por cada minuto de duración
	 *                          del servicio.
	 * @param tipoVehiculo      tipo de vehículo asociado al servicio.
	 * @param categoriaVehiculo categoría del vehículo asociado al servicio.
	 * @param tipoServicio      modalidad o tipo de servicio ofrecido.
	 */
	public ServicioDTO(String nombre, double tarifaBase, double precioPorKm, double precioPorMinuto,
			TipoVehiculo tipoVehiculo, CategoriaVehiculo categoriaVehiculo, TipoServicio tipoServicio) {
		this.nombre = nombre;
		this.tarifaBase = tarifaBase;
		this.precioPorKm = precioPorKm;
		this.precioPorMinuto = precioPorMinuto;
		this.tipoServicio = tipoServicio;
		this.tipoVehiculo = tipoVehiculo;
		this.categoriaVehiculo = categoriaVehiculo;
	}

	/**
	 * Retorna el nombre identificador del servicio.
	 *
	 * @return nombre del servicio.
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * Retorna la tarifa base fija aplicada al inicio del servicio.
	 *
	 * @return tarifa base del servicio.
	 */
	public double getTarifaBase() {
		return tarifaBase;
	}

	/**
	 * Retorna el precio cobrado por cada kilómetro recorrido durante el servicio.
	 *
	 * @return precio por kilómetro del servicio.
	 */
	public double getPrecioPorKm() {
		return precioPorKm;
	}

	/**
	 * Retorna el precio cobrado por cada minuto de duración del servicio.
	 *
	 * @return precio por minuto del servicio.
	 */
	public double getPrecioPorMinuto() {
		return precioPorMinuto;
	}

	/**
	 * Retorna la modalidad o tipo de servicio ofrecido.
	 *
	 * @return tipo de servicio asociado.
	 */
	public TipoServicio getTipoServicio() {
		return tipoServicio;
	}

	/**
	 * Retorna el tipo de vehículo requerido para el servicio.
	 *
	 * @return tipo de vehículo asociado al servicio.
	 */
	public TipoVehiculo getTipoVehiculo() {
		return tipoVehiculo;
	}

	/**
	 * Retorna la categoría del vehículo asociado al servicio.
	 *
	 * @return instancia de {@link CategoriaVehiculo} que representa la categoría
	 *         del vehículo.
	 */
	public CategoriaVehiculo getCategoriaVehiculo() {
		return categoriaVehiculo;
	}

}
