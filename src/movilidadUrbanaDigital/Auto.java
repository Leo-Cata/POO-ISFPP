package movilidadUrbanaDigital;

public class Auto extends Vehiculo{
	
	private int capacidadPasajeros;

	public Auto(String patente, String modelo, boolean aceptaPasajeros, boolean aceptaCarga, Servicio servicio,
			Ubicacion ubicacion, int capacidadPasajeros) {
		super(patente, modelo, aceptaPasajeros, aceptaCarga, servicio, ubicacion);
		this.capacidadPasajeros = capacidadPasajeros;
	}

	//implementar el calculo
	@Override
	public double calcularCostoViaje(int distancia) {
		// TODO Auto-generated method stub
		return 0;
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
