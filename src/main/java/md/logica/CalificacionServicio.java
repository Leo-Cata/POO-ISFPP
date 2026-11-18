package md.logica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private final Map<Cliente, List<CalificacionViaje>> calificacionesClientes = new HashMap<>();
    private final Map<Conductor, List<CalificacionViaje>> calificacionesConductores = new HashMap<>();
    private final Map<UUID, CalificacionViaje> calificacionesRegistradasCliente = new HashMap<>();
    private final Map<UUID, CalificacionViaje> calificacionesRegistradasConductor = new HashMap<>();

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

        if (calificacionesRegistradasCliente.containsKey(viaje.getId())
                || viaje.getCalificacionCliente() != CalificacionViaje.NO_CALIFICADO) {
            throw new IllegalStateException(
                    "El cliente ya calificó este viaje.");
        }

        Usuario conductor = viaje.getConductor();

        if (viaje.estadoActual() == EstadoViaje.FINALIZADO
                && (conductor == null || conductor.getConductor() == null)) {
            throw new IllegalStateException(
                    "No se encontró el perfil del conductor.");
        }

        calificacionesRegistradasCliente.put(viaje.getId(), calificacion);

        // Si el viaje ya finalizó, se incorpora directamente al historial.
        if (viaje.estadoActual() == EstadoViaje.FINALIZADO) {
            calificacionesConductores.computeIfAbsent(
                    conductor.getConductor(), k -> new ArrayList<>()).add(calificacion); // obtiene el valor asociado a
                                                                                         // una clave y, si no existe,
                                                                                         // crearlo y almacenarlo
                                                                                         // automáticamente.
        }
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

        if (calificacionesRegistradasConductor.containsKey(viaje.getId())
                || viaje.getCalificacionConductor() != CalificacionViaje.NO_CALIFICADO) {
            throw new IllegalStateException(
                    "El conductor ya calificó este viaje.");
        }

        Usuario cliente = viaje.getCliente();

        if (viaje.estadoActual() == EstadoViaje.FINALIZADO
                && (cliente == null || cliente.getCliente() == null)) {
            throw new IllegalStateException(
                    "No se encontró el perfil del cliente.");
        }

        calificacionesRegistradasConductor.put(viaje.getId(), calificacion);

        // Si el viaje ya finalizó, se incorpora directamente al historial.
        if (viaje.estadoActual() == EstadoViaje.FINALIZADO) {
            calificacionesClientes.computeIfAbsent(
                    cliente.getCliente(), k -> new ArrayList<>()).add(calificacion);
            // obtiene el valor asociado a una clave y, si no existe, crearlo y almacenarlo
            // automáticamente.
        }
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
        if (calificacionCliente != CalificacionViaje.NO_CALIFICADO) {
            calificacionesConductores.computeIfAbsent(
                    conductor.getConductor(), k -> new ArrayList<>()).add(calificacionCliente);
        }

        // El conductor califica al cliente.
        if (calificacionConductor != CalificacionViaje.NO_CALIFICADO) {
            calificacionesClientes.computeIfAbsent(
                    cliente.getCliente(), k -> new ArrayList<>()).add(calificacionConductor);
        }
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

        return calcularPromedio(
                calificacionesClientes.getOrDefault(cliente, new ArrayList<>()));
        // obtiene el valor asociado a una clave o devolver un valor alternativo si esa
        // clave no existe.
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

        return calcularPromedio(
                calificacionesConductores.getOrDefault(conductor, new ArrayList<>()));
        // obtiene el valor asociado a una clave o devolver un valor alternativo si esa
        // clave no existe.
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
    public CalificacionViaje obtenerCalificacionCliente(Viaje viaje) {

        if (viaje == null) {
            throw new IllegalArgumentException("El viaje no puede ser null.");
        }
        return calificacionesRegistradasCliente.getOrDefault(
                viaje.getId(), CalificacionViaje.NO_CALIFICADO);
    }

    public CalificacionViaje obtenerCalificacionConductor(Viaje viaje) {
        if (viaje == null) {
            throw new IllegalArgumentException("El viaje no puede ser null.");
        }
        return calificacionesRegistradasConductor.getOrDefault(
                viaje.getId(), CalificacionViaje.NO_CALIFICADO);
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

        EstadoViaje estado = viaje.estadoActual();

        if (estado != EstadoViaje.INICIADO
                && estado != EstadoViaje.FINALIZADO) {
            throw new IllegalStateException(
                    "Solamente se pueden calificar viajes iniciados o finalizados.");
        }
    }

    /**
     * Calcula el promedio de una colección de calificaciones.
     */
    private double calcularPromedio(
            List<CalificacionViaje> calificaciones) {

        if (calificaciones.isEmpty()) {
            return 0.0;
        }

        int sumaPuntajes = 0;
        int cantidadCalificaciones = 0;

        for (CalificacionViaje calificacion : calificaciones) {

            if (calificacion != CalificacionViaje.NO_CALIFICADO) {

                sumaPuntajes += calificacion.getValorNumerico();
                cantidadCalificaciones++;
            }
        }

        if (cantidadCalificaciones == 0) {
            return 0.0;
        }

        return (double) sumaPuntajes / cantidadCalificaciones;
    }

}
