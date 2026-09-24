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
