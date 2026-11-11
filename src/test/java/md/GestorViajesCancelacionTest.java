
package md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import md.config.CargarParametros;
import md.logica.GestorViajes;
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

class GestorViajesCancelacionTest {

    private static final double DELTA = 0.0001;

    private GestorViajes gestor;
    private Usuario cliente;
    private Usuario conductor;
    private Servicio servicio;
    private Ubicacion origen;
    private Ubicacion destino;

    @BeforeEach
    void setUp() {
        gestor = new GestorViajes();

        cliente = new Usuario("Ana Martinez","+5491111111111","ana@email.com");

        conductor = new Usuario("Juan Perez","+5491122222222","juan@email.com");

        Vehiculo vehiculo = new Vehiculo("AA111AA","Toyota Corolla",4,TipoVehiculo.AUTO,CategoriaVehiculo.ESTANDAR,
                TipoServicio.PASAJEROS);

        conductor.altaConductor("LIC-123", vehiculo);
        conductor.cambiarRolActivo(RolUsuario.CONDUCTOR);

        servicio = new Servicio("Estandar",2000,200,100,TipoVehiculo.AUTO,                CategoriaVehiculo.ESTANDAR,
                TipoServicio.PASAJEROS);

        origen = new Ubicacion(-42.76, -65.03);
        destino = new Ubicacion(-42.78, -65.05);
    }

    private Viaje crearViajeAceptado() {
        LocalDateTime ahora = LocalDateTime.now().minusSeconds(2);

        Viaje viaje = new Viaje(cliente, origen, destino, servicio);

        viaje.setCostoTotal(10000);
        viaje.setDuracionEstimadaMinutos(60);

        viaje.solicitar(ahora);
        viaje.aceptar(ahora.plusSeconds(1), conductor);

        return viaje;
    }

    @Test
    @DisplayName("1. Cancela un viaje solicitado sin aplicar un costo")
    void testCancelarViajeSolicitado() {
        Viaje viaje = new Viaje(cliente, origen, destino, servicio);
        viaje.solicitar(LocalDateTime.now());

        boolean resultado = gestor.procesarCancelacionViaje(cliente,"Cambio de planes");

        assertTrue(resultado);
        assertEquals(EstadoViaje.CANCELADO, viaje.estadoActual());
        assertEquals(0.0, viaje.getCostoCancelacion(), DELTA);
        assertEquals(RolUsuario.CLIENTE, viaje.getRolCancela());
        assertEquals("Cambio de planes", viaje.getMotivoCancelacion());
    }

    @Test
    @DisplayName("2. Devuelve false cuando el usuario no tiene viajes activos")
    void testCancelarSinViajeActivo() {
        boolean resultado = gestor.procesarCancelacionViaje(cliente,"Cambio de planes");

        assertFalse(resultado);
    }

    @Test
    @DisplayName("3. Rechaza cancelar si el usuario es null")
    void testCancelarConUsuarioNull() {
        assertThrows(IllegalArgumentException.class,() -> gestor.procesarCancelacionViaje(null, "Motivo"));
    }

    @Test
    @DisplayName("4. Rechaza cancelar si el motivo está vacío")
    void testCancelarConMotivoVacio() {
        assertThrows(IllegalArgumentException.class,() -> gestor.procesarCancelacionViaje(cliente, " "));
    }

    @Test
    @DisplayName("5. Calcula el costo al cancelar un viaje aceptado como cliente")
    void testCancelarViajeAceptadoPorCliente() {
        Viaje viaje = crearViajeAceptado();

        double porcentaje = CargarParametros.getCostoCancelacionViajeAceptado();
        double costoEsperado = viaje.getCostoTotal() * porcentaje;

        boolean resultado = gestor.procesarCancelacionViaje(cliente,"Ya no necesito el viaje");

        assertTrue(resultado);
        assertEquals(EstadoViaje.CANCELADO, viaje.estadoActual());
        assertEquals(costoEsperado, viaje.getCostoCancelacion(), DELTA);
        assertEquals(RolUsuario.CLIENTE, viaje.getRolCancela());
    }

    @Test
    @DisplayName("6. No aplica costo cuando cancela el conductor")
    void testCancelarViajeAceptadoPorConductor() {
        Viaje viaje = crearViajeAceptado();

        boolean resultado = gestor.procesarCancelacionViaje(conductor,"Problema con el vehículo");

        assertTrue(resultado);
        assertEquals(EstadoViaje.CANCELADO, viaje.estadoActual());
        assertEquals(0.0, viaje.getCostoCancelacion(), DELTA);
        assertEquals(RolUsuario.CONDUCTOR, viaje.getRolCancela());
        assertEquals(EstadoConductor.DISPONIBLE,conductor.getConductor().getEstadoConductor()
        );
    }
}
