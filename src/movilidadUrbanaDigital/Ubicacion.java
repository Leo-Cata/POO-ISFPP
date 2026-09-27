package movilidadUrbanaDigital;

import java.util.Objects;

public class Ubicacion {

	//atributos de ubicacion
	private double latitud;
	private double longitud;
	private String direccion;
	
	//constructor del objeto 
	public Ubicacion(double latitud, double longitud, String direccion) {
		super();
		this.latitud = latitud;
		this.longitud = longitud;
		this.direccion = direccion;
	}

	// Método que calcula la distancia real entre el origen y destino usando Haversine
	/**
	 * 
	 * @param destino
	 * @return la distancia entre la ubicacion de origen y el destino
	 */
		public double calcularDistanciaA(Ubicacion destino) {
			// Radio medio de la Tierra en kilómetros
			final double RADIO_TIERRA = 6371.0;

			// 1. Convertir las latitudes y longitudes de grados a radianes
			double lat1 = Math.toRadians(this.latitud);
			double lon1 = Math.toRadians(this.longitud);
			double lat2 = Math.toRadians(destino.getLatitud());
			double lon2 = Math.toRadians(destino.getLongitud());

			//Calcular la diferencia entre las coordenadas
			double dLat = lat2 - lat1;
			double dLon = lon2 - lon1;

			//Aplicar la Fórmula de Haversine
			double a = Math.pow(Math.sin(dLat / 2), 2)
					 + Math.cos(lat1) * Math.cos(lat2) 
					 * Math.pow(Math.sin(dLon / 2), 2);
			
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

			//Obtener la distancia exacta en kilómetros con decimales
			double distanciaKm = RADIO_TIERRA * c;

			//Redondear a entero porque el método devuelve un 'int'
			return distanciaKm;
		}
	
	//getters y setters
	public double getLatitud() {
		return latitud;
	}

	public void setLatitud(double latitud) {
		this.latitud = latitud;
	}

	public double getLongitud() {
		return longitud;
	}

	public void setLongitud(double longitud) {
		this.longitud = longitud;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	@Override
	public int hashCode() {
		return Objects.hash(latitud, longitud);
	}
	
	//equals, dos ubicaciones son iguales si coinciden en su longitud y latitud.
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ubicacion other = (Ubicacion) obj;
		return Double.doubleToLongBits(latitud) == Double.doubleToLongBits(other.latitud)
				&& Double.doubleToLongBits(longitud) == Double.doubleToLongBits(other.longitud);
	}

	//to string del objeto ubicacion
	@Override
	public String toString() {
		return "Ubicacion [latitud=" + latitud + ", longitud=" + longitud + ", direccion=" + direccion + "]";
	}
}
