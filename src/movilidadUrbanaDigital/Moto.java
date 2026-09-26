package movilidadUrbanaDigital;

public class Moto extends Vehiculo {

	public Moto(String patente, String modelo, boolean aceptaPasajeros, boolean aceptaCarga, Servicio servicio,
			Ubicacion ubicacion) {
		super(patente, modelo, aceptaPasajeros, aceptaCarga, servicio, ubicacion);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see movilidadUrbanaDigital.Vehiculo#calcularCostoViaje(double)
	 */
	@Override
	public double calcularCostoViaje(double distancia) {
		if (distancia < 0) {
			throw new IllegalArgumentException("La distancio no puede ser menor a 0");
		}

		return getServicio().getPrecioPorKm() * distancia;
	}

}
