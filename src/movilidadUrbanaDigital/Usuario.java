package movilidadUrbanaDigital;

import java.util.ArrayList;
import java.util.List;

public abstract class Usuario {
    // variables del objeto usuario
    private String nombre;
    private String documento;
    private String telefono;
    private String email;
    private int totalCalificaciones; // {readOnly}

    // capaz hay que quitarlo porque no figura en el diagrama, pero de alguna manera
    // hay que tener en cuenta las calificaciones
    private List<Calificacion> calificaciones;

    // constructor del objeto usuario
    public Usuario(String nombre, String documento, String telefono, String email) {
        this.nombre = nombre;
        this.documento = documento;
        this.telefono = telefono;
        this.email = email;
        this.totalCalificaciones = 0;
        this.calificaciones = new ArrayList<Calificacion>();
    }

    // getters y setters del objeto usuario
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getTotalCalificaciones() {
        return totalCalificaciones;
    }

    // LEO
    // segun la ia, para trabajos anteriores me decia que
    // tener setters para variables de tipo lista y similares puede ser mala
    // practica ya que al setter algo en la lista, estarias sobreescribiendo toda la
    // lista y no solamente agregando un valor
    public List<Calificacion> getCalificaciones() {
        return calificaciones;
    }

    // hash y equals del objeto usuario
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((documento == null) ? 0 : documento.hashCode());
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
        Usuario other = (Usuario) obj;
        if (documento == null) {
            if (other.documento != null)
                return false;
        } else if (!documento.equals(other.documento))
            return false;
        return true;
    }

    // metodos

    /**
     * Metodo para agregar una calificacion al viaje
     * para el conductor y el cliente
     * 
     * @param calificacion
     * @throws IllegalArgumentException si la calificacion es invalda
     */
    public void calificarViaje(Calificacion calificacion) {
        if (calificacion == null) {
            throw new IllegalArgumentException("Calificacion invalida.");
        }
        this.calificaciones.add(calificacion);
        this.totalCalificaciones++;
    }

    /**
     * metodo para calcular el promedio de todas las calificaciones en la lista de
     * las mismas
     * 
     * @return double promedio de calificaciones
     */
    public double promedioCalificacion() {
        double promedio = 0.0;
        // caso base, la lista es nulla o esta vacia (retorna 0)
        if (calificaciones == null || calificaciones.isEmpty()) {
            return promedio;
        }

        // foreach para loopear por todas las clasificaciones
        // obteniendo el resultado en int
        for (Calificacion calificacion : calificaciones) {
            promedio += calificacion.getPuntaje();
        }
        // luego se divide la suma total por el tamaño de la lista
        promedio = promedio / calificaciones.size();

        return promedio;
    }

    // toString del objeto usuario
    @Override
    public String toString() {
        return "Usuario [nombre=" + nombre + ", documento=" + documento + ", telefono=" + telefono + ", email=" + email
                + ", totalCalificaciones=" + totalCalificaciones + "]";
    }
}
