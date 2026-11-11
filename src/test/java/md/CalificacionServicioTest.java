
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

        cliente = new Usuario("Ana Martinez","2800000000","ana@email.com");

        Vehiculo vehiculo = new Vehiculo("AA111AA","Ford Focus",4,TipoVehiculo.AUTO,CategoriaVehiculo.ESTANDAR,TipoServicio.PASAJEROS);

        conductor = new Usuario("Juan Perez","2800000001","juan@email.com");

        conductor.altaConductor("LIC-123", vehiculo);
        conductor.cambiarRolActivo(RolUsuario.CONDUCTOR);

        Servicio servicio = new Servicio("Estandar",2000,200,100,TipoVehiculo.AUTO,CategoriaVehiculo.ESTANDAR,
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

        assertEquals(CalificacionViaje.MUY_BUENO,viaje.getCalificacionCliente());
    }

    @Test
    @DisplayName("2. El conductor puede calificar al cliente de su viaje")
    void testCalificarComoConductor() {

        calificacionServicio.calificarComoConductor(conductor, viaje, CalificacionViaje.EXCELENTE);

        assertEquals(CalificacionViaje.EXCELENTE,viaje.getCalificacionConductor());
    }

    @Test
    @DisplayName("3. Un cliente ajeno al viaje no puede calificarlo")
    void testClienteAjenoNoPuedeCalificar() {

        Usuario otroCliente = new Usuario("Pedro Lopez","2800000002","pedro@email.com");

        assertThrows(IllegalArgumentException.class,() -> calificacionServicio.calificarComoCliente(
                        otroCliente, viaje, CalificacionViaje.BUENO));
    }

    @Test
    @DisplayName("4. Un conductor ajeno al viaje no puede calificarlo")
    void testConductorAjenoNoPuedeCalificar() {

        Usuario otroConductor = new Usuario("Carlos Gomez","2800000003","carlos@email.com");

        assertThrows(IllegalArgumentException.class,() -> calificacionServicio.calificarComoConductor(
                        otroConductor, viaje, CalificacionViaje.BUENO));
    }

    @Test
    @DisplayName("5. No se permite calificar dos veces el mismo viaje")
    void testNoPermiteCalificarDosVeces() {

        calificacionServicio.calificarComoCliente(cliente, viaje, CalificacionViaje.BUENO);

        assertThrows(IllegalStateException.class,() -> calificacionServicio.calificarComoCliente(
                        cliente, viaje, CalificacionViaje.EXCELENTE));

        calificacionServicio.calificarComoConductor(conductor, viaje, CalificacionViaje.MUY_BUENO);

        assertThrows(IllegalStateException.class,() -> calificacionServicio.calificarComoConductor(
                        conductor, viaje, CalificacionViaje.EXCELENTE));
    }

    @Test
    @DisplayName("6. No se permiten calificaciones nulas ni NO_CALIFICADO")
    void testRechazaCalificacionesInvalidas() {

        assertThrows(IllegalArgumentException.class,() -> calificacionServicio.calificarComoCliente(
                        cliente, viaje, null));

        assertThrows(IllegalArgumentException.class,() -> calificacionServicio.calificarComoCliente(
                        cliente, viaje, CalificacionViaje.NO_CALIFICADO));
    }

    @Test
    @DisplayName("7. Solo se pueden calificar viajes iniciados")
    void testNoPermiteCalificarViajeNoIniciado() {

        Usuario otroCliente = new Usuario("Maria Lopez","2800000004","maria@email.com");

        Servicio servicio = new Servicio("Estandar",2000,200,100,TipoVehiculo.AUTO,CategoriaVehiculo.ESTANDAR,
                TipoServicio.PASAJEROS);

        Viaje viajeSolicitado = new Viaje(otroCliente,new Ubicacion(-42.76, -65.03),
                new Ubicacion(-42.77, -65.04),servicio);

        viajeSolicitado.solicitar(LocalDateTime.now());

        assertEquals(EstadoViaje.SOLICITADO, viajeSolicitado.estadoActual());

        assertThrows(IllegalStateException.class,() -> calificacionServicio.calificarComoCliente(
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

        viaje.finalizar(LocalDateTime.now(),viaje.getCalificacionCliente(),viaje.getCalificacionConductor());

        calificacionServicio.registrarCalificacionesEnHistorial(viaje);

        assertEquals(CalificacionViaje.BUENO,cliente.getCliente().getCalificaciones().get(0));

        assertEquals(CalificacionViaje.MUY_BUENO,conductor.getConductor().getCalificaciones().get(0));
    }

    @Test
    @DisplayName("10. Las calificaciones pendientes se completan con NO_CALIFICADO")
    void testCompletarCalificacionesPendientes() {

        calificacionServicio.calificarComoCliente(cliente, viaje, CalificacionViaje.MUY_BUENO);

        calificacionServicio.completarCalificacionesPendientes(viaje);

        assertEquals(CalificacionViaje.NO_CALIFICADO,viaje.getCalificacionConductor());

        viaje.finalizar(LocalDateTime.now(),viaje.getCalificacionCliente(),viaje.getCalificacionConductor());

        calificacionServicio.registrarCalificacionesEnHistorial(viaje);

        assertEquals(0.0,calificacionServicio.calcularPromedioCliente(cliente.getCliente()),0.0001);
    }

    @Test
    @DisplayName("11. El promedio se calcula sin considerar NO_CALIFICADO")
    void testCalcularPromedios() {

        cliente.getCliente().agregarCalificacion(CalificacionViaje.BUENO);
        cliente.getCliente().agregarCalificacion(CalificacionViaje.EXCELENTE);

        conductor.getConductor().agregarCalificacion(CalificacionViaje.REGULAR);
        conductor.getConductor().agregarCalificacion(CalificacionViaje.MUY_BUENO);
        conductor.getConductor().agregarCalificacion(CalificacionViaje.NO_CALIFICADO);

        assertEquals(4.0,calificacionServicio.calcularPromedioCliente(cliente.getCliente()),0.0001);

        assertEquals(3.0,calificacionServicio.calcularPromedioConductor(conductor.getConductor()),0.0001);
    }

    @Test
    @DisplayName("12. El promedio es cero cuando no existen calificaciones")
    void testPromedioSinCalificaciones() {

        Usuario usuarioNuevo = new Usuario("Usuario Nuevo","2800000005","nuevo@email.com");

        assertEquals(0.0,calificacionServicio.calcularPromedioCliente(usuarioNuevo.getCliente()),0.0001);
    }
}
