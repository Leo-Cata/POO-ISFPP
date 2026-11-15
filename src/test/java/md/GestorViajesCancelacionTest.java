package md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

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
import md.rutas.api.PlanificadorRutas;
import md.rutas.api.PuntoRuta;
import md.rutas.api.RutaCalculada;
import md.rutas.api.RutaNoDisponibleException;
import net.datastructures.ProbeHashMap;

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
        cliente = new Usuario("Ana Martinez", "+5491111111111", "ana@email.com");
        conductor = new Usuario("Juan Perez", "+5491122222222", "juan@email.com");
        origen = new Ubicacion(-42.76, -65.03);
        destino = new Ubicacion(-42.77, -65.04);
        servicio = new Servicio("Estándar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);

        Vehiculo vehiculo = new Vehiculo("AA111AA", "Toyota Corolla", 4,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);
        vehiculo.setUbicacion(origen);
        conductor.altaConductor("LIC-123", vehiculo);
        conductor.cambiarRolActivo(RolUsuario.CONDUCTOR);
        conductor.getConductor().setEstado(EstadoConductor.DISPONIBLE);

        ProbeHashMap<String, Usuario> usuarios = new ProbeHashMap<>();
        usuarios.put(conductor.getEmail(), conductor);
        gestor = new GestorViajes(usuarios, new PlanificadorFalso(),
                CargarParametros.getVelocidadMediaKmH(),
                CargarParametros.getCostoCancelacionViajeAceptado());
    }

    private Viaje crearViajeAceptado() throws RutaNoDisponibleException {
        GestorViajes.ResultadoSolicitud resultado = gestor.procesarSolicitudViaje(
                cliente, servicio, origen, destino);
        assertNotNull(resultado, "El conductor de prueba debe poder aceptar el viaje.");
        return resultado.viaje();
    }

    @Test
    @DisplayName("1. Cancela un viaje solicitado sin aplicar un costo")
    void testCancelarViajeSolicitado() {
        Viaje viaje = new Viaje(cliente, origen, destino, servicio);
        viaje.solicitar(java.time.LocalDateTime.now());

        boolean resultado = gestor.procesarCancelacionViaje(cliente, "Cambio de planes");

        assertTrue(resultado);
        assertEquals(EstadoViaje.CANCELADO, viaje.estadoActual());
        assertEquals(0.0, gestor.obtenerCostoCancelacion(viaje), DELTA);
        assertEquals(RolUsuario.CLIENTE, viaje.getRolCancela());
        assertEquals("Cambio de planes", viaje.getMotivoCancelacion());
    }

    @Test
    @DisplayName("2. Devuelve false cuando el usuario no tiene viajes activos")
    void testCancelarSinViajeActivo() {
        boolean resultado = gestor.procesarCancelacionViaje(cliente, "Cambio de planes");
        assertFalse(resultado);
    }

    @Test
    @DisplayName("3. Rechaza cancelar si el usuario es null")
    void testCancelarConUsuarioNull() {
        assertThrows(IllegalArgumentException.class,
                () -> gestor.procesarCancelacionViaje(null, "Motivo"));
    }

    @Test
    @DisplayName("4. Rechaza cancelar si el motivo está vacío")
    void testCancelarConMotivoVacio() {
        assertThrows(IllegalArgumentException.class,
                () -> gestor.procesarCancelacionViaje(cliente, " "));
    }

    @Test
    @DisplayName("5. Calcula el costo al cancelar un viaje aceptado como cliente")
    void testCancelarViajeAceptadoPorCliente() throws RutaNoDisponibleException {
        Viaje viaje = crearViajeAceptado();
        double costoEsperado = gestor.obtenerCotizacion(viaje).costoTotal()
                * CargarParametros.getCostoCancelacionViajeAceptado();

        boolean resultado = gestor.procesarCancelacionViaje(cliente, "Ya no necesito el viaje");

        assertTrue(resultado);
        assertEquals(EstadoViaje.CANCELADO, viaje.estadoActual());
        assertEquals(costoEsperado, gestor.obtenerCostoCancelacion(viaje), DELTA);
        assertEquals(RolUsuario.CLIENTE, viaje.getRolCancela());
    }

    @Test
    @DisplayName("6. No aplica costo cuando cancela el conductor")
    void testCancelarViajeAceptadoPorConductor() throws RutaNoDisponibleException {
        Viaje viaje = crearViajeAceptado();

        boolean resultado = gestor.procesarCancelacionViaje(conductor, "Problema con el vehículo");

        assertTrue(resultado);
        assertEquals(EstadoViaje.CANCELADO, viaje.estadoActual());
        assertEquals(0.0, gestor.obtenerCostoCancelacion(viaje), DELTA);
        assertEquals(RolUsuario.CONDUCTOR, viaje.getRolCancela());
        assertEquals(EstadoConductor.DISPONIBLE, conductor.getConductor().getEstadoConductor());
    }

    private static final class PlanificadorFalso implements PlanificadorRutas {
        @Override
        public RutaCalculada calcular(Ubicacion origen, Ubicacion destino, TipoVehiculo tipoVehiculo)
                throws RutaNoDisponibleException {
            return new RutaCalculada(1000.0, 60_000L, List.of(
                    new PuntoRuta(origen.getLatitud(), origen.getLongitud()),
                    new PuntoRuta(destino.getLatitud(), destino.getLongitud())));
        }

        @Override
        public Optional<Ubicacion> ajustarACalle(Ubicacion candidata, double maxDistanciaMetros) {
            return Optional.of(candidata);
        }

        @Override
        public void close() {
        }
    }
}
