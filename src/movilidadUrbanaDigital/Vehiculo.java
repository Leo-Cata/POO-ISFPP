package movilidadUrbanaDigital;

public abstract class Vehiculo {
    private String patente;
    private String modelo;
    private boolean aceptaPasajeros;
    private boolean aceptaCarga;
    private Servicio servicio;
    private Ubicacion ubicacion;

    // constuctor del objeto vehiculo
    public Vehiculo(String patente, String modelo, boolean aceptaPasajeros, boolean aceptaCarga, Servicio servicio,
            Ubicacion ubicacion) {
        this.patente = patente;
        this.modelo = modelo;
        this.aceptaPasajeros = aceptaPasajeros;
        this.aceptaCarga = aceptaCarga;
        this.servicio = servicio;
        this.ubicacion = ubicacion;
    }
    
    //metodo abstracto cada subclase calcula su costo
    public abstract double calcularCostoViaje(int distancia);
    
    // getters y setters del objeto vehiculo
    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public boolean isAceptaPasajeros() {
        return aceptaPasajeros;
    }

    public void setAceptaPasajeros(boolean aceptaPasajeros) {
        this.aceptaPasajeros = aceptaPasajeros;
    }

    public boolean isAceptaCarga() {
        return aceptaCarga;
    }

    public void setAceptaCarga(boolean aceptaCarga) {
        this.aceptaCarga = aceptaCarga;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    // hash y equals del objeto vehiculo
    // dos vehiculos son iguales si tienen la misma patente
    // no se si se deberia chequear por el tipo de servicio siguiendo la idea de que
    // los servicios serian carga y pasajeros
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((patente == null) ? 0 : patente.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vehiculo other = (Vehiculo) obj;
        if (patente == null) {
            if (other.patente != null)
                return false;
        } else if (!patente.equals(other.patente))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Vehiculo [patente=" + patente + ", modelo=" + modelo + ", aceptaPasajeros=" + aceptaPasajeros
                + ", aceptaCarga=" + aceptaCarga + ", servicio=" + servicio + ", ubicacion=" + ubicacion + "]";
    }
}
