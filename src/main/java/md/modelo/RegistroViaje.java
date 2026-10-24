package md.modelo;

import java.time.LocalDateTime;

public class RegistroViaje {

    private LocalDateTime fechaHora;
    private EstadoViaje estado;

    RegistroViaje(LocalDateTime fechaHora, EstadoViaje estado) {
        this.fechaHora = fechaHora;
        this.estado = estado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public EstadoViaje getEstado() {
        return estado;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fechaHora == null) ? 0 : fechaHora.hashCode());
        result = prime * result + ((estado == null) ? 0 : estado.hashCode());
        return result;
    }

    /**
     * dos viajes son iguales si tienen la misma fechaHora y estado
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RegistroViaje other = (RegistroViaje) obj;
        if (fechaHora == null) {
            if (other.fechaHora != null)
                return false;
        } else if (!fechaHora.equals(other.fechaHora))
            return false;
        if (estado != other.estado)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RegistroViaje [fechaHora=" + fechaHora + "]";
    }
}