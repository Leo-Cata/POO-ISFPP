package movilidadUrbanaDigital;

public class Moto extends Vehiculo{
	

	public Moto(String patente, String modelo, boolean aceptaPasajeros, boolean aceptaCarga, Servicio servicio,
			Ubicacion ubicacion) {
		super(patente, modelo, aceptaPasajeros, aceptaCarga, servicio, ubicacion);
	}

	//implementar el calculo
	@Override
	public double calcularCostoViaje(int distancia) {
		// TODO Auto-generated method stub
		return 0;
	}

}
