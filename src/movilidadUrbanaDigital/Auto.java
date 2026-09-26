package movilidadUrbanaDigital;

public class Auto extends Vehiculo {

	private int capacidadPasajeros;
	private static double precioPorKm = 500;
	private static double tarifaBase = 2000;

	public Auto(String patente, String modelo, boolean aceptaPasajeros, boolean aceptaCarga, Servicio servicio,
			Ubicacion ubicacion, int capacidadPasajeros) {
		super(patente, modelo, aceptaPasajeros, aceptaCarga, servicio, ubicacion);
		this.capacidadPasajeros = capacidadPasajeros;
	}

	// implementar el calculo
	@Override
	public double calcularCostoViaje(double distancia) {
		if (distancia < 0) {
			throw new IllegalArgumentException("la distancia no puede ser menor a 0");
		}
		return tarifaBase + (precioPorKm * distancia);
	}

	public int getCapacidadPasajeros() {
		return capacidadPasajeros;
	}

	public void setCapacidadPasajeros(int capacidadPasajeros) {
		this.capacidadPasajeros = capacidadPasajeros;
	}

	@Override
	public String toString() {
		return "Auto [capacidadPasajeros=" + capacidadPasajeros + "]";
	}
}
