package md.modelo;

import java.util.Objects;

public class Ubicacion {
	private double latitud;
	private double longitud;

	public Ubicacion(double latitud, double longitud) {
		this.latitud = latitud;
		this.longitud = longitud;
	}

	// Método que calcula la distancia real entre el origen y destino usando
	// Haversine
	/**
	 * 
	 * @param ubicacion
	 * @return la distancia entre la ubicaciones
	 */
	public double calcularDistanciaA(Ubicacion ubicacion) {

		if (ubicacion == null) {
			throw new IllegalArgumentException("La ubicacion no puede ser null");
		}

		// Radio medio de la Tierra en kilómetros
		final double RADIO_TIERRA = 6371.0;

		// 1. Convertir las latitudes y longitudes de grados a radianes
		double lat1 = Math.toRadians(this.latitud);
		double lon1 = Math.toRadians(this.longitud);
		double lat2 = Math.toRadians(ubicacion.getLatitud());
		double lon2 = Math.toRadians(ubicacion.getLongitud());

		// Calcular la diferencia entre las coordenadas
		double dLat = lat2 - lat1;
		double dLon = lon2 - lon1;

		// Aplicar la Fórmula de Haversine
		double a = Math.pow(Math.sin(dLat / 2), 2)
				+ Math.cos(lat1) * Math.cos(lat2)
						* Math.pow(Math.sin(dLon / 2), 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		// Obtener la distancia exacta en kilómetros con decimales
		double distanciaKm = RADIO_TIERRA * c;

		return distanciaKm;
	}

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

	@Override
	public int hashCode() {
		return Objects.hash(latitud, longitud);
	}

	// equals, dos ubicaciones son iguales si coinciden en su longitud y latitud.
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

	@Override
	public String toString() {
		return "Ubicacion [latitud=" + latitud + ", longitud=" + longitud + "]";
	}

}
