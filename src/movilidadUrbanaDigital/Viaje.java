package movilidadUrbanaDigital;

import java.time.LocalDate;

public class Viaje {
    private int tiempoEstimado;
    private double factorDemora;
    private LocalDate fechaHora; // deberia ser LocalDateTime ya que menciona hora pero en el diagrama apareceasi
    private double costoViaje;
    private Conductor conductor;
    private Cliente cliente;
    private Calificacion calificacionCliente, calificacionConductor;
    private Ubicacion origen, destino;
    private Estado estado;

    // construtor
    public Viaje(int tiempoEstimado, double factorDemora, LocalDate fechaHora, double costoViaje, Conductor conductor,
            Cliente cliente, Calificacion calificacionCliente, Calificacion calificacionConductor, Ubicacion origen,
            Ubicacion destino, Estado estado) {
        this.tiempoEstimado = tiempoEstimado;
        this.factorDemora = factorDemora;
        this.fechaHora = fechaHora;
        this.costoViaje = costoViaje;
        this.conductor = conductor;
        this.cliente = cliente;
        this.calificacionCliente = calificacionCliente;
        this.calificacionConductor = calificacionConductor;
        this.origen = origen;
        this.destino = destino;
        this.estado = estado;
    }

    // getters y setters
    public int getTiempoEstimado() {
        return tiempoEstimado;
    }

    public void setTiempoEstimado(int tiempoEstimado) {
        this.tiempoEstimado = tiempoEstimado;
    }

    public double getFactorDemora() {
        return factorDemora;
    }

    public void setFactorDemora(double factorDemora) {
        this.factorDemora = factorDemora;
    }

    public LocalDate getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDate fechaHora) {
        this.fechaHora = fechaHora;
    }

    public double getCostoViaje() {
        return costoViaje;
    }

    public void setCostoViaje(double costoViaje) {
        this.costoViaje = costoViaje;
    }

    public Conductor getConductor() {
        return conductor;
    }

    public void setConductor(Conductor conductor) {
        this.conductor = conductor;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Calificacion getCalificacionCliente() {
        return calificacionCliente;
    }

    public void setCalificacionCliente(Calificacion calificacionCliente) {
        this.calificacionCliente = calificacionCliente;
    }

    public Calificacion getCalificacionConductor() {
        return calificacionConductor;
    }

    public void setCalificacionConductor(Calificacion calificacionConductor) {
        this.calificacionConductor = calificacionConductor;
    }

    public Ubicacion getOrigen() {
        return origen;
    }

    public void setOrigen(Ubicacion origen) {
        this.origen = origen;
    }

    public Ubicacion getDestino() {
        return destino;
    }

    public void setDestino(Ubicacion destino) {
        this.destino = destino;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    // dos viajes son iguales si tienen la misma fechaHora, el mismo conductor y el
    // mismo cleinte
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fechaHora == null) ? 0 : fechaHora.hashCode());
        result = prime * result + ((conductor == null) ? 0 : conductor.hashCode());
        result = prime * result + ((cliente == null) ? 0 : cliente.hashCode());
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
        Viaje other = (Viaje) obj;
        if (fechaHora == null) {
            if (other.fechaHora != null)
                return false;
        } else if (!fechaHora.equals(other.fechaHora))
            return false;
        if (conductor == null) {
            if (other.conductor != null)
                return false;
        } else if (!conductor.equals(other.conductor))
            return false;
        if (cliente == null) {
            if (other.cliente != null)
                return false;
        } else if (!cliente.equals(other.cliente))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Viaje [tiempoEstimado=" + tiempoEstimado + ", factorDemora=" + factorDemora + ", fechaHora=" + fechaHora
                + ", costoViaje=" + costoViaje + ", conductor=" + conductor + ", cliente=" + cliente
                + ", calificacionCliente=" + calificacionCliente + ", calificacionConductor=" + calificacionConductor
                + ", origen=" + origen + ", destino=" + destino + ", estado=" + estado + "]";
    }
}
