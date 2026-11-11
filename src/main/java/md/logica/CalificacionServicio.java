package md.logica;

import java.util.ArrayList;

import md.modelo.CalificacionViaje;
import md.modelo.Cliente;
import md.modelo.Conductor;
import md.modelo.EstadoViaje;
import md.modelo.Usuario;
import md.modelo.Viaje;

/**
 * Gestiona las calificaciones de los viajes y calcula los promedios de
 * calificación de clientes y conductores.
 */
public class CalificacionServicio {

    /**
     * Registra la calificación que el cliente otorga al conductor.
     *
     * @param cliente      usuario que realiza la calificación.
     * @param viaje        viaje que se desea calificar.
     * @param calificacion calificación otorgada al conductor.
     */
    public void calificarComoCliente(
            Usuario cliente, Viaje viaje, CalificacionViaje calificacion) {

        validarDatosCalificacion(cliente, viaje, calificacion);

        if (!cliente.equals(viaje.getCliente())) {
            throw new IllegalArgumentException(
                    "El usuario no es el cliente de este viaje.");
        }

        if (viaje.getCalificacionCliente() != null) {
            throw new IllegalStateException(
                    "El cliente ya calificó este viaje.");
        }

        viaje.setCalificacionCliente(calificacion);
    }

    /**
     * Registra la calificación que el conductor otorga al cliente.
     *
     * @param conductor    usuario que realiza la calificación.
     * @param viaje        viaje que se desea calificar.
     * @param calificacion calificación otorgada al cliente.
     */
    public void calificarComoConductor(
            Usuario conductor, Viaje viaje, CalificacionViaje calificacion) {

        validarDatosCalificacion(conductor, viaje, calificacion);

        if (!conductor.equals(viaje.getConductor())) {
            throw new IllegalArgumentException(
                    "El usuario no es el conductor de este viaje.");
        }

        if (viaje.getCalificacionConductor() != null) {
            throw new IllegalStateException(
                    "El conductor ya calificó este viaje.");
        }

        viaje.setCalificacionConductor(calificacion);
    }

    /**
     * Incorpora las calificaciones de un viaje finalizado a los
     * historiales de los participantes que las recibieron.
     *
     * Este método debe ser invocado por GestorViajes después
     * de que el viaje haya sido finalizado correctamente.
     *
     * @param viaje viaje cuyas calificaciones se incorporan.
     */
    public void registrarCalificacionesEnHistorial(Viaje viaje) {

        if (viaje == null) {
            throw new IllegalArgumentException(
                    "El viaje no puede ser null.");
        }

        if (viaje.estadoActual() != EstadoViaje.FINALIZADO) {
            throw new IllegalStateException(
                    "El viaje debe estar finalizado para registrar las calificaciones.");
        }

        CalificacionViaje calificacionCliente = viaje.getCalificacionCliente();

        CalificacionViaje calificacionConductor = viaje.getCalificacionConductor();

        if (calificacionCliente == null || calificacionConductor == null) {
            throw new IllegalStateException(
                    "El viaje no tiene registradas las calificaciones.");
        }

        Usuario cliente = viaje.getCliente();
        Usuario conductor = viaje.getConductor();

        if (cliente == null || cliente.getCliente() == null
                || conductor == null || conductor.getConductor() == null) {
            throw new IllegalStateException(
                    "No se encontraron los perfiles de los participantes.");
        }

        // El cliente califica al conductor.
        conductor.getConductor().agregarCalificacion(calificacionCliente);

        // El conductor califica al cliente.
        cliente.getCliente().agregarCalificacion(calificacionConductor);
    }

    /**
     * Calcula el promedio de las calificaciones recibidas por un cliente.
     *
     * @param cliente cliente cuyo promedio se desea calcular.
     * @return promedio de calificaciones o 0.0 si no tiene calificaciones.
     */
    public double calcularPromedioCliente(Cliente cliente) {

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "El cliente no puede ser null.");
        }

        return calcularPromedio(cliente.getCalificaciones());
    }

    /**
     * Calcula el promedio de las calificaciones recibidas por un conductor.
     *
     * @param conductor conductor cuyo promedio se desea calcular.
     * @return promedio de calificaciones o 0.0 si no tiene calificaciones.
     */
    public double calcularPromedioConductor(Conductor conductor) {

        if (conductor == null) {
            throw new IllegalArgumentException(
                    "El conductor no puede ser null.");
        }

        return calcularPromedio(conductor.getCalificaciones());
    }

    /**
     * Completa con {@link CalificacionViaje#NO_CALIFICADO} las calificaciones del
     * viaje que todavía no fueron cargadas (la del cliente y la del conductor).
     * No modifica las calificaciones que ya tienen un valor.
     *
     * @param viaje viaje en curso cuyas calificaciones pendientes se completan.
     * @throws IllegalArgumentException si el viaje es {@code null}.
     * @throws IllegalStateException    si el viaje no está en estado
     *                                  {@code INICIADO}.
     */
    public void completarCalificacionesPendientes(Viaje viaje) {

        if (viaje == null) {
            throw new IllegalArgumentException("El viaje es obligatorio.");
        }

        if (viaje.estadoActual() != EstadoViaje.INICIADO) {
            throw new IllegalStateException(
                    "El viaje debe estar iniciado para completar las calificaciones.");
        }

        if (viaje.getCalificacionCliente() == null) {
            viaje.setCalificacionCliente(CalificacionViaje.NO_CALIFICADO);
        }

        if (viaje.getCalificacionConductor() == null) {
            viaje.setCalificacionConductor(CalificacionViaje.NO_CALIFICADO);
        }
    }

    /**
     * Valida los datos necesarios para registrar una calificación.
     */
    private void validarDatosCalificacion(
            Usuario usuario, Viaje viaje, CalificacionViaje calificacion) {

        if (usuario == null || viaje == null) {
            throw new IllegalArgumentException(
                    "El usuario y el viaje son obligatorios.");
        }

        if (calificacion == null
                || calificacion == CalificacionViaje.NO_CALIFICADO) {
            throw new IllegalArgumentException(
                    "La calificación no es válida.");
        }

        if (viaje.estadoActual() != EstadoViaje.INICIADO) {
            throw new IllegalStateException(
                    "Solamente se pueden calificar viajes iniciados.");
        }
    }

    /**
     * Calcula el promedio de una colección de calificaciones.
     */
    private double calcularPromedio(
            ArrayList<CalificacionViaje> calificaciones) {

        if (calificaciones.isEmpty()) {
            return 0.0;
        }

        int sumaPuntajes = 0;
        int cantidadCalificaciones = 0;

        for (CalificacionViaje calificacion : calificaciones) {

            if (calificacion != CalificacionViaje.NO_CALIFICADO) {

                sumaPuntajes += calificacion.getPuntaje();
                cantidadCalificaciones++;
            }
        }

        if (cantidadCalificaciones == 0) {
            return 0.0;
        }

        return (double) sumaPuntajes / cantidadCalificaciones;
    }

}
