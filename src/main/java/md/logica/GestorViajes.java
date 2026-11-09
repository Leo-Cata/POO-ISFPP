package md.logica;

import java.time.LocalDateTime;

import md.aplicacion.ContextoAplicacion;
import md.config.CargarParametros;
import md.modelo.CalificacionViaje;
import md.modelo.EstadoViaje;
import md.modelo.RolUsuario;
import md.modelo.Servicio;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Viaje;
import md.rutas.api.RutaNoDisponibleException;
import md.rutas.api.RutasViaje;

/**
 * Coordina los casos de uso relacionados con los viajes.
 *
 * No interactúa con el usuario ni presenta información por pantalla.
 * Esa responsabilidad corresponde a las interfaces de usuario.
 */
public class GestorViajes {

    /**
     * Resultado de la cotización comercial.
     *
     * La distancia y la duración son estimaciones geográficas,
     * no métricas obtenidas del motor vial.
     */
    public record Cotizacion(double distanciaKm,double duracionMinutos,double costoTotal) {
    }

    /**
     * Resultado de una solicitud procesada correctamente.
     */
    public record ResultadoSolicitud(Viaje viaje,RutasViaje rutas,Cotizacion cotizacion,Usuario conductor) {
    }

    /**
     * Calcula la cotización comercial utilizando la distancia geográfica
     * y la velocidad media configurada.
     */
    public Cotizacion calcularCotizacion(Servicio servicio,Ubicacion origen,Ubicacion destino) {

        if (servicio == null || origen == null || destino == null) {
            throw new IllegalArgumentException("El servicio, el origen y el destino son obligatorios.");
        }

        double velocidadMedia = CargarParametros.getVelocidadMediaKmH();

        if (!Double.isFinite(velocidadMedia) || velocidadMedia <= 0) {
            throw new IllegalStateException("La velocidad media configurada no es válida.");
        }

        double distanciaKm = origen.calcularDistanciaA(destino);
        double duracionMinutos = (distanciaKm / velocidadMedia) * 60.0;

        double costoTotal = servicio.calcularCosto(distanciaKm,duracionMinutos);

        return new Cotizacion(distanciaKm,duracionMinutos,costoTotal);
    }

    /**
     * Procesa una solicitud de viaje.
     *
     * Selecciona el conductor compatible más cercano, calcula los
     * recorridos viales y registra el viaje como solicitado y aceptado.
     *
     * Las rutas se calculan antes de crear el viaje para evitar registrar
     * una solicitud cuando no se pudo obtener un recorrido.
     *
     * @return el resultado de la solicitud o null si no hay conductores
     *         compatibles disponibles.
     */
    public ResultadoSolicitud procesarSolicitudViaje(Usuario clienteUsuario,Servicio servicio,Ubicacion origen,Ubicacion destino) throws RutaNoDisponibleException {

        if (clienteUsuario == null || servicio == null || origen == null || destino == null) {
            throw new IllegalArgumentException("Los datos de la solicitud son obligatorios.");
        }
        if (clienteUsuario.getRolActivo() != RolUsuario.CLIENTE) {
            throw new IllegalStateException("El usuario debe tener activo el rol CLIENTE para solicitar un viaje.");
        }
        if (clienteUsuario.getCliente() == null) {
            throw new IllegalArgumentException("El usuario no tiene perfil de cliente.");
        }

        if (clienteUsuario.getCliente().enViaje()) {
            throw new IllegalStateException("El cliente ya tiene un viaje activo.");
        }

        // Calcular la cotización definitiva.
        Cotizacion cotizacion = calcularCotizacion(servicio,origen,destino);

        //Obtener los datos de la aplicación.
        var datos = ContextoAplicacion.getDatosIniciales();

        if (datos == null) {
            throw new IllegalStateException("Los datos iniciales no están disponibles.");
        }

        //Seleccionar el conductor compatible más cercano.
        Movilidad movilidad = new Movilidad(datos.getUsuarios());

        Usuario conductorSeleccionado =	movilidad.seleccionarConductor(servicio, origen);

        if (conductorSeleccionado == null) {
            return null;
        }

        //Calcular los dos recorridos viales.
        RutasViaje rutas = new PlanificacionViajeService(ContextoAplicacion.getMotorVial()).planificar(
                        conductorSeleccionado.getConductor().getVehiculoActivo(),origen,destino);

        //Crear y registrar el viaje.
        Viaje nuevoViaje = new Viaje(clienteUsuario,origen,destino,servicio);

        nuevoViaje.solicitar(LocalDateTime.now());

        try {
            nuevoViaje.aceptar(LocalDateTime.now(),conductorSeleccionado);

        } catch (RuntimeException e) {

            // Si no pudo aceptarse, no dejamos la solicitud pendiente.
            if (nuevoViaje.estadoActual() == EstadoViaje.SOLICITADO) {
                nuevoViaje.rechazar(LocalDateTime.now());
            }

            throw e;
        }

        return new ResultadoSolicitud(nuevoViaje,rutas,cotizacion,conductorSeleccionado);
    }

    /**
     * Cancela el viaje activo del cliente.
     *
     * @return true si se canceló un viaje; false si no había uno activo.
     */
    public boolean procesarCancelacionViaje(Usuario clienteUsuario,String motivo) {

        if (clienteUsuario == null || clienteUsuario.getCliente() == null) {
            throw new IllegalArgumentException("El usuario debe tener un perfil de cliente.");
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de cancelación es obligatorio.");
        }

        for (Viaje viaje : clienteUsuario.getCliente().getViajes().values()) {

            EstadoViaje estado = viaje.estadoActual();

            if (estado == EstadoViaje.SOLICITADO || estado == EstadoViaje.ACEPTADO || estado == EstadoViaje.INICIADO) {

                viaje.cancelar(LocalDateTime.now(),clienteUsuario,motivo);

                return true;
            }
        }

        return false;
    }

    /**
     * Inicia el viaje aceptado del conductor.
     *
     * @return true si se inició el viaje; false si no había uno aceptado.
     */
    public boolean procesarInicioViaje(Usuario conductorUsuario) {

        Viaje viajeActivo = obtenerViajeActivoConductor(conductorUsuario);

        if (viajeActivo == null
                || viajeActivo.estadoActual() != EstadoViaje.ACEPTADO) {
            return false;
        }

        viajeActivo.iniciar(LocalDateTime.now());

        return true;
    }

    /**
     * Finaliza el viaje iniciado del conductor.
     *
     * @return true si se finalizó el viaje; false si no había uno iniciado.
     */
    public boolean procesarFinalizacionViaje(
            Usuario conductorUsuario,
            CalificacionViaje calCliente,
            CalificacionViaje calConductor) {

        if (calCliente == null || calConductor == null) {
            throw new IllegalArgumentException("Las calificaciones son obligatorias.");
        }

        Viaje viajeActivo = obtenerViajeActivoConductor(conductorUsuario);

        if (viajeActivo == null
                || viajeActivo.estadoActual() != EstadoViaje.INICIADO) {
            return false;
        }

        viajeActivo.finalizar(LocalDateTime.now(),calCliente,calConductor);

        return true;
    }

    /**
     * Busca el viaje aceptado o iniciado del conductor.
     */
    private Viaje obtenerViajeActivoConductor(Usuario conductorUsuario) {

        if (conductorUsuario == null
                || conductorUsuario.getConductor() == null) {
            return null;
        }

        for (Viaje viaje : conductorUsuario.getConductor().getViajes().values()) {

            EstadoViaje estado = viaje.estadoActual();

            if (estado == EstadoViaje.ACEPTADO || estado == EstadoViaje.INICIADO) {
                return viaje;
            }
        }

        return null;
    }
}