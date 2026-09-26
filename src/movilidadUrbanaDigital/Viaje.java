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
    private Vehiculo vehiculo;

    /**
     * * Constructor de la clase Viaje.
     * Al instanciarse, el viaje nace siempre en estado SOLICITADO y calcula
     * su costo final de manera inmutable basándose en las condiciones del momento.
     * 
     * @param fechaHora
     * @param cliente
     * @param origen
     * @param destino
     * @param vehiculo
     * @param factorDemora
     * @param tiempoEstimado
     */
    public Viaje(LocalDate fechaHora, Cliente cliente, Ubicacion origen, Ubicacion destino, Vehiculo vehiculo,
            double factorDemora, int tiempoEstimado) {
        this.fechaHora = fechaHora;
        this.cliente = cliente;
        this.origen = origen;
        this.destino = destino;
        this.vehiculo = vehiculo;
        this.factorDemora = factorDemora;
        this.tiempoEstimado = tiempoEstimado;
        this.estado = Estado.SOLICITADO; // el estado del viaje inicia siempre en solicitado
        this.costoViaje = this.calcularCostoViaje(); // se fija el costo del viaje al solicitarlo
    }

    /**
     * Calcula la tarifa final del viaje delegando la distancia a la Ubicación
     * y el subtotal al Vehículo, para luego aplicar los recargos por tiempo y
     * demanda.
     * 
     * 
     * @return El costo total calculado para el viaje
     */
    public double calcularCostoViaje() {

        int distancia = origen.calcularDistanciaA(destino);

        double tarifaDelVehiculo = vehiculo.calcularCostoViaje(distancia);

        Servicio servicio = vehiculo.getServicio();

        double costoPorTiempo = tiempoEstimado * servicio.getPrecioPorMinuto();

        double tarifaFinal = (tarifaDelVehiculo + costoPorTiempo) * factorDemora;

        return tarifaFinal;
    }

    // recibir el conductor que acepta el viaje (modificacion)
    /**
     * 
     * @param conductor
     * @throws IllegalStateException Si el viaje no está en estado SOLICITADO.
     */
    public void aceptarViaje(Conductor conductor) {
        // verificar si el estado es correcto
        if (estado != Estado.SOLICITADO)
            throw new IllegalStateException("Solo se puede aceptar un viaje en esatdo solicitado : " + estado);

        // asiganmos conductor
        this.conductor = conductor;
        // luego cambiamos el estado del viaje
        estado = Estado.ACEPTADO;
    }

    /**
     * Marca el inicio físico del recorrido (cuando el cliente o carga sube al
     * vehículo).
     * 
     * @throws IllegalStateException Si el viaje no fue aceptado previamente.
     */
    public void iniciarViaje() {

        if (estado != Estado.ACEPTADO)
            throw new IllegalStateException("El conductor debe aceptar el viaje antes de iniciarlo.");

        estado = Estado.EN_CURSO;
    }

    /**
     * Cancela el viaje aplicando las reglas de penalidad de negocio:
     * - Si está SOLICITADO: El costo se reduce a 0.
     * - Si está ACEPTADO o EN_CURSO: Se cobra una penalidad del 50% del valor
     * original.
     * 
     * @throws IllegalStateException Si el viaje ya se encuentra FINALIZADO.
     */
    public void cancelarViaje() {

        if (estado == Estado.FINALIZADO)
            throw new IllegalStateException("No es posible cancelar un viaje finalizado.");
        if (estado == Estado.CANCELADO)
            throw new IllegalStateException("El viaje ya se encuentra cancelado.");

        if (estado == Estado.SOLICITADO)
            this.costoViaje = 0;

        if (estado == Estado.ACEPTADO || estado == Estado.EN_CURSO)
            this.costoViaje = this.costoViaje * 0.50;

        this.estado = Estado.CANCELADO;
    }

    /**
     * Finaliza el recorrido y guarda las calificaciones cruzadas de manera
     * simultánea.
     * 
     * @param calificacionConductor La nota que se le otorga al conductor.
     * @param calificacionCliente   La nota que se le otorga al cliente.
     * @throws IllegalStateException Si el viaje no se encuentra EN_CURSO.
     */
    public void finalizarViaje(Calificacion calificacionConductor, Calificacion calificacionCliente) {
        if (estado != Estado.EN_CURSO)
            throw new IllegalStateException("Solo se puede finalizar un viaje que actualmente esta en curso.");

        this.calificacionCliente = calificacionCliente;
        this.calificacionConductor = calificacionConductor;

        estado = Estado.FINALIZADO;
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

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    // Métodos Object (hashCode, equals, toString)

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
