package movilidadUrbanaDigital;

public class Conductor extends Usuario {
    private boolean disponible;
    private static int limiteCalificaciones = 200;
    private Vehiculo vehiculo;

    // constructor de la clase conductor
    public Conductor(String nombre, String documento, String telefono, String email, Vehiculo vehiculo) {
        super(nombre, documento, telefono, email);
        this.vehiculo = vehiculo;
    }

    // setters y getters de conductor
    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public static int getLimiteCalificaciones() {
        return limiteCalificaciones;
    }

    public static void setLimiteCalificaciones(int limiteCalificaciones) {
        Conductor.limiteCalificaciones = limiteCalificaciones;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    // hash y equals de conductor
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (disponible ? 1231 : 1237);
        result = prime * result + ((vehiculo == null) ? 0 : vehiculo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Conductor other = (Conductor) obj;
        if (disponible != other.disponible)
            return false;
        if (vehiculo == null) {
            if (other.vehiculo != null)
                return false;
        } else if (!vehiculo.equals(other.vehiculo))
            return false;
        return true;
    }

    // toString de conductor
    @Override
    public String toString() {
        return super.toString() + " Conductor [disponible=" + disponible + ", vehiculo=" + vehiculo + "]";
    }
}
