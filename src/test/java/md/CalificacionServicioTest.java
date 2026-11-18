
package md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import md.logica.CalificacionServicio;
import md.modelo.CalificacionViaje;
import md.modelo.CategoriaVehiculo;
import md.modelo.EstadoConductor;
import md.modelo.EstadoViaje;
import md.modelo.RolUsuario;
import md.modelo.Servicio;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Viaje;

class CalificacionServicioTest {

    private CalificacionServicio calificacionServicio;

    private Usuario cliente;
    private Usuario conductor;

    private Viaje viaje;

    @BeforeEach
    void setUp() {

        calificacionServicio = new CalificacionServicio();

        cliente = new Usuario("Ana Martinez", "2800000000", "ana@email.com");

        Vehiculo vehiculo = new Vehiculo("AA111AA", "Ford Focus", 4, TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR,
                TipoServicio.PASAJEROS);

        conductor = new Usuario("Juan Perez", "2800000001", "juan@email.com");

        conductor.altaConductor("LIC-123", vehiculo);
        conductor.cambiarRolActivo(RolUsuario.CONDUCTOR);
        conductor.getConductor().setEstado(EstadoConductor.DISPONIBLE);

        Servicio servicio = new Servicio("Estandar", 2000, 200, 100, TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR,
                TipoServicio.PASAJEROS);

        Ubicacion origen = new Ubicacion(-42.76, -65.03);
        Ubicacion destino = new Ubicacion(-42.77, -65.04);

        viaje = new Viaje(cliente, origen, destino, servicio);

        viaje.solicitar(LocalDateTime.now());
        viaje.aceptar(LocalDateTime.now(), conductor);
        viaje.iniciar(LocalDateTime.now());
    }

    @Test
    @DisplayName("1. El cliente puede calificar al conductor de su viaje")
    void testCalificarComoCliente() {

        calificacionServicio.calificarComoCliente(cliente, viaje, CalificacionViaje.MUY_BUENO);

        assertEquals(CalificacionViaje.MUY_BUENO,
                calificacionServicio.obtenerCalificacionCliente(viaje));
    }

    @Test
    @DisplayName("2. El conductor puede calificar al cliente de su viaje")
    void testCalificarComoConductor() {

        calificacionServicio.calificarComoConductor(conductor, viaje, CalificacionViaje.EXCELENTE);

        assertEquals(CalificacionViaje.EXCELENTE,
                calificacionServicio.obtenerCalificacionConductor(viaje));
    }

    @Test
    @DisplayName("3. Un cliente ajeno al viaje no puede calificarlo")
    void testClienteAjenoNoPuedeCalificar() {

        Usuario otroCliente = new Usuario("Pedro Lopez", "2800000002", "pedro@email.com");

        assertThrows(IllegalArgumentException.class, () -> calificacionServicio.calificarComoCliente(
                otroCliente, viaje, CalificacionViaje.BUENO));
    }

    @Test
    @DisplayName("4. Un conductor ajeno al viaje no puede calificarlo")
    void testConductorAjenoNoPuedeCalificar() {

        Usuario otroConductor = new Usuario("Carlos Gomez", "2800000003", "carlos@email.com");

        assertThrows(IllegalArgumentException.class, () -> calificacionServicio.calificarComoConductor(
                otroConductor, viaje, CalificacionViaje.BUENO));
    }

    @Test
    @DisplayName("5. No se permite calificar dos veces el mismo viaje")
    void testNoPermiteCalificarDosVeces() {

        calificacionServicio.calificarComoCliente(cliente, viaje, CalificacionViaje.BUENO);

        assertThrows(IllegalStateException.class, () -> calificacionServicio.calificarComoCliente(
                cliente, viaje, CalificacionViaje.EXCELENTE));

        calificacionServicio.calificarComoConductor(conductor, viaje, CalificacionViaje.MUY_BUENO);

        assertThrows(IllegalStateException.class, () -> calificacionServicio.calificarComoConductor(
                conductor, viaje, CalificacionViaje.EXCELENTE));
    }

    @Test
    @DisplayName("6. No se permiten calificaciones nulas ni NO_CALIFICADO")
    void testRechazaCalificacionesInvalidas() {

        assertThrows(IllegalArgumentException.class, () -> calificacionServicio.calificarComoCliente(
                cliente, viaje, null));

        assertThrows(IllegalArgumentException.class, () -> calificacionServicio.calificarComoCliente(
                cliente, viaje, CalificacionViaje.NO_CALIFICADO));
    }

    @Test
    @DisplayName("7. No se permiten calificaciones en viajes no iniciados ni finalizados")
    void testNoPermiteCalificarViajeNoIniciado() {

        Usuario otroCliente = new Usuario("Maria Lopez", "2800000004", "maria@email.com");

        Servicio servicio = new Servicio("Estandar", 2000, 200, 100, TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR,
                TipoServicio.PASAJEROS);

        Viaje viajeSolicitado = new Viaje(otroCliente, new Ubicacion(-42.76, -65.03),
                new Ubicacion(-42.77, -65.04), servicio);

        viajeSolicitado.solicitar(LocalDateTime.now());

        assertEquals(EstadoViaje.SOLICITADO, viajeSolicitado.estadoActual());

        assertThrows(IllegalStateException.class, () -> calificacionServicio.calificarComoCliente(
                otroCliente, viajeSolicitado, CalificacionViaje.BUENO));
    }

    @Test
    @DisplayName("8. No se pueden registrar calificaciones en el historial antes de finalizar")
    void testRegistrarHistorialAntesDeFinalizar() {

        calificacionServicio.calificarComoCliente(cliente, viaje, CalificacionViaje.MUY_BUENO);

        calificacionServicio.calificarComoConductor(conductor, viaje, CalificacionViaje.BUENO);

        assertThrows(IllegalStateException.class,
                () -> calificacionServicio.registrarCalificacionesEnHistorial(viaje));
    }

    @Test
    @DisplayName("9. Al finalizar se incorporan las calificaciones a los historiales")
    void testRegistrarCalificacionesEnHistorial() {

        calificacionServicio.calificarComoCliente(cliente, viaje, CalificacionViaje.MUY_BUENO);
        calificacionServicio.calificarComoConductor(conductor, viaje, CalificacionViaje.BUENO);

        viaje.finalizar(LocalDateTime.now(),
                calificacionServicio.obtenerCalificacionCliente(viaje),
                calificacionServicio.obtenerCalificacionConductor(viaje));

        calificacionServicio.registrarCalificacionesEnHistorial(viaje);

        // No se consulta la lista interna del modelo: se verifica el historial
        // mediante los promedios expuestos por CalificacionServicio.
        assertEquals(3.0, calificacionServicio.calcularPromedioCliente(cliente.getCliente()), 0.0001);
        assertEquals(4.0, calificacionServicio.calcularPromedioConductor(conductor.getConductor()), 0.0001);
    }

    @Test
    @DisplayName("10. Una calificacion pendiente queda como NO_CALIFICADO al finalizar")
    void testCompletarCalificacionesPendientes() {

        calificacionServicio.calificarComoCliente(cliente, viaje, CalificacionViaje.MUY_BUENO);

        // Si el conductor no califica, el servicio devuelve NO_CALIFICADO por defecto.
        assertEquals(CalificacionViaje.NO_CALIFICADO,
                calificacionServicio.obtenerCalificacionConductor(viaje));

        viaje.finalizar(LocalDateTime.now(),
                calificacionServicio.obtenerCalificacionCliente(viaje),
                calificacionServicio.obtenerCalificacionConductor(viaje));

        calificacionServicio.registrarCalificacionesEnHistorial(viaje);

        // La calificacion del cliente la recibe el conductor; el cliente no recibió
        // ninguna.
        assertEquals(0.0, calificacionServicio.calcularPromedioCliente(cliente.getCliente()), 0.0001);
        assertEquals(4.0, calificacionServicio.calcularPromedioConductor(conductor.getConductor()), 0.0001);
    }

    @Test
    @DisplayName("11. El promedio se calcula sin considerar NO_CALIFICADO")
    void testCalcularPromedios() {

        // Primer viaje: el cliente recibe BUENO y el conductor recibe REGULAR.
        calificacionServicio.calificarComoCliente(cliente, viaje, CalificacionViaje.REGULAR);
        calificacionServicio.calificarComoConductor(conductor, viaje, CalificacionViaje.BUENO);
        finalizarYRegistrar(viaje);

        // Segundo viaje: se agregan EXCELENTE al cliente y MUY_BUENO al conductor.
        Viaje segundoViaje = crearViajeIniciado(cliente, conductor);
        calificacionServicio.calificarComoCliente(cliente, segundoViaje, CalificacionViaje.MUY_BUENO);
        calificacionServicio.calificarComoConductor(conductor, segundoViaje, CalificacionViaje.EXCELENTE);
        finalizarYRegistrar(segundoViaje);

        // Tercer viaje sin calificaciones: NO_CALIFICADO no debe alterar los promedios.
        Viaje tercerViaje = crearViajeIniciado(cliente, conductor);
        finalizarYRegistrar(tercerViaje);

        assertEquals(4.0, calificacionServicio.calcularPromedioCliente(cliente.getCliente()), 0.0001);
        assertEquals(3.0, calificacionServicio.calcularPromedioConductor(conductor.getConductor()), 0.0001);
    }

    /**
     * Finaliza el viaje con las calificaciones registradas (o NO_CALIFICADO por
     * defecto)
     * y las incorpora a los historiales.
     */
    private void finalizarYRegistrar(Viaje viaje) {
        viaje.finalizar(LocalDateTime.now(),
                calificacionServicio.obtenerCalificacionCliente(viaje),
                calificacionServicio.obtenerCalificacionConductor(viaje));
        calificacionServicio.registrarCalificacionesEnHistorial(viaje);
    }

    /** Crea un nuevo viaje iniciado con los mismos participantes de la prueba. */
    private Viaje crearViajeIniciado(Usuario cliente, Usuario conductor) {
        Servicio servicio = new Servicio("Estandar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);
        Viaje nuevoViaje = new Viaje(cliente,
                new Ubicacion(-42.76, -65.03),
                new Ubicacion(-42.77, -65.04), servicio);
        nuevoViaje.solicitar(LocalDateTime.now());
        nuevoViaje.aceptar(LocalDateTime.now(), conductor);
        nuevoViaje.iniciar(LocalDateTime.now());
        return nuevoViaje;
    }

    @Test
    @DisplayName("12. El promedio es cero cuando no existen calificaciones")
    void testPromedioSinCalificaciones() {

        Usuario usuarioNuevo = new Usuario("Usuario Nuevo", "2800000005", "nuevo@email.com");

        assertEquals(0.0, calificacionServicio.calcularPromedioCliente(usuarioNuevo.getCliente()), 0.0001);
    }

    @Test
    @DisplayName("13. El cliente puede calificar un viaje finalizado")
    void testCalificarViajeFinalizado() {

        viaje.finalizar(
                LocalDateTime.now(),
                CalificacionViaje.NO_CALIFICADO,
                CalificacionViaje.NO_CALIFICADO);

        calificacionServicio.calificarComoCliente(
                cliente, viaje, CalificacionViaje.MUY_BUENO);

        assertEquals(
                4.0,
                calificacionServicio.calcularPromedioConductor(
                        conductor.getConductor()),
                0.0001);

        assertEquals(
                CalificacionViaje.MUY_BUENO,
                calificacionServicio.obtenerCalificacionCliente(viaje));

        assertThrows(
                IllegalStateException.class,
                () -> calificacionServicio.calificarComoCliente(
                        cliente, viaje, CalificacionViaje.EXCELENTE));
    }
}
