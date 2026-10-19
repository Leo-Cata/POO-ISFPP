package md.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

class ViajeCicloDeVidaTest {

    private Usuario cliente;
    private Ubicacion ubicacion;

    @BeforeEach
    void setUp() {
        cliente = new Usuario("Paula Vazquez", "+549115539336", "paula@email.com");
        ubicacion = new Ubicacion(-42.76, -65.03);
    }

    // arma un conductor listo para trabajar: rol CONDUCTOR y estado DISPONIBLE
    private Usuario crearConductorDisponible(String patente, TipoVehiculo tipo,
            CategoriaVehiculo categoria, TipoServicio tipoServicio) {
        Usuario u = new Usuario("Juan Perez", "+549117363900", "juan@email.com");
        u.altaConductor("B1", new Vehiculo(patente, "Modelo", 4, tipo, categoria, tipoServicio));
        u.cambiarRolActivo(RolUsuario.CONDUCTOR);
        u.getConductor().setEstado(EstadoConductor.DISPONIBLE);
        return u;
    }

    private Viaje crearViajeSolicitado(Servicio servicio) {
        Viaje viaje = new Viaje(cliente, ubicacion, ubicacion, servicio);
        viaje.solicitar(LocalDateTime.now());
        return viaje;
    }

    @Test
    @DisplayName("1. Rechaza al conductor que no esta DISPONIBLE")
    void testRechazaConductorFueraDeServicio() {
        Usuario conductor = crearConductorDisponible("AA111AA", TipoVehiculo.AUTO,
                CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);
        conductor.getConductor().setEstado(EstadoConductor.FUERA_DE_SERVICIO);
        Viaje viaje = crearViajeSolicitado(new Servicio("Estandar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS));

        assertThrows(IllegalStateException.class, () -> viaje.aceptar(LocalDateTime.now(), conductor));
    }

    @Test
    @DisplayName("2. Rechaza un vehiculo de otro tipo (moto para un servicio de auto)")
    void testRechazaMotoParaServicioDeAuto() {
        Usuario conductor = crearConductorDisponible("123ABC", TipoVehiculo.MOTO,
                CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);
        Viaje viaje = crearViajeSolicitado(new Servicio("Estandar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS));

        assertThrows(IllegalStateException.class, () -> viaje.aceptar(LocalDateTime.now(), conductor));
    }

    @Test
    @DisplayName("3. Rechaza un vehiculo que no ofrece el tipo de servicio pedido")
    void testRechazaVehiculoQueNoOfreceElServicio() {
        Usuario conductor = crearConductorDisponible("AA222AA", TipoVehiculo.AUTO,
                CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);
        Viaje viaje = crearViajeSolicitado(new Servicio("Envios", 1500, 150, 75,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.ENVIOS));

        assertThrows(IllegalStateException.class, () -> viaje.aceptar(LocalDateTime.now(), conductor));
    }

    @Test
    @DisplayName("4. Acepta cuando tipo, servicio y categoria coinciden")
    void testAceptaCuandoTodoCoincide() {
        Usuario conductor = crearConductorDisponible("AA333AA", TipoVehiculo.AUTO,
                CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);
        Viaje viaje = crearViajeSolicitado(new Servicio("Estandar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS));

        viaje.aceptar(LocalDateTime.now(), conductor);

        assertEquals(EstadoViaje.ACEPTADO, viaje.estadoActual());
        assertEquals(EstadoConductor.VIAJE_A_ORIGEN, conductor.getConductor().getEstadoConductor());
    }

    @Test
    @DisplayName("5. Acepta un vehiculo de categoria mayor a la pedida")
    void testAceptaVehiculoDeCategoriaMayor() {
        Usuario conductor = crearConductorDisponible("AA444AA", TipoVehiculo.AUTO,
                CategoriaVehiculo.PREMIUM, TipoServicio.PASAJEROS);
        Viaje viaje = crearViajeSolicitado(new Servicio("Estandar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS));

        viaje.aceptar(LocalDateTime.now(), conductor);

        assertEquals(EstadoViaje.ACEPTADO, viaje.estadoActual());
        
    }
    
    @Test
    @DisplayName("6. Probar error al cancelar un viaje que nunca fue solicitado")
    void testCancelarErrorViajeNoSolicitado() {
        // el viaje esta creado, pero nunca se llamo a solicitar()
    	Servicio servicioEstandar = new Servicio("Estandar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);
    	Viaje viaje = new Viaje(cliente, ubicacion, ubicacion, servicioEstandar);
        assertThrows(IllegalStateException.class, () -> {
            viaje.cancelar(LocalDateTime.now(), cliente, "Me arrepenti");
        });
    }
    
    @Test
    @DisplayName("7. Rechaza un vehiculo con categoria menor a la requerida")
    void testRechazaVehiculoDeCategoriaMenor() {
        Usuario conductor = crearConductorDisponible("AA555AA", TipoVehiculo.AUTO,
                CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);
        // Servicio requiere PREMIUM, pero el auto es ESTANDAR
        Viaje viaje = crearViajeSolicitado(new Servicio("Premium", 4000, 400, 200,
                TipoVehiculo.AUTO, CategoriaVehiculo.PREMIUM, TipoServicio.PASAJEROS));

        assertThrows(IllegalStateException.class, () -> viaje.aceptar(LocalDateTime.now(), conductor));
    }

    @Test
    @DisplayName("8. Rechaza cuando el cliente intenta aceptar su propio viaje")
    void testRechazaClienteAceptandoSuPropioViaje() {
        // El mismo usuario que hace de cliente intenta actuar como conductor
        Viaje viaje = crearViajeSolicitado(new Servicio("Estandar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS));

        assertThrows(IllegalArgumentException.class, () -> viaje.aceptar(LocalDateTime.now(), cliente));
    }
    
    @Test
    @DisplayName("9. Un viaje recién instanciado no se agrega al historial hasta ser solicitado")
    void testViajeNoSolicitadoNoApareceEnHistorial() {
        Servicio servicio = new Servicio("Estandar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);
        
        // Instanciamos el viaje pero NO llamamos a solicitar()
        Viaje viaje = new Viaje(cliente, ubicacion, ubicacion, servicio);

        // Verificamos el tamaño del historial antes de solicitar
        assertEquals(0, cliente.getCliente().getViajes().size());
        
     // Verificamos el tamaño del historial luego de solicitar
        viaje.solicitar(LocalDateTime.now());
        assertEquals(1, cliente.getCliente().getViajes().size());
    }
    @Test
    @DisplayName("10. Inicia un viaje exitosamente luego de ser aceptado")
    void testIniciarViajeConExito() {
        Usuario conductor = crearConductorDisponible("AA666AA", TipoVehiculo.AUTO,
                CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);
        Viaje viaje = crearViajeSolicitado(new Servicio("Estandar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS));

        LocalDateTime ahora = LocalDateTime.now();
        viaje.aceptar(ahora, conductor);
        
        // Iniciamos el viaje
        viaje.iniciar(ahora.plusMinutes(10));

        assertEquals(EstadoViaje.INICIADO, viaje.estadoActual());
        assertEquals(EstadoConductor.VIAJE_A_DESTINO, conductor.getConductor().getEstadoConductor());
    }

    @Test
    @DisplayName("11. Rechaza iniciar un viaje que no fue aceptado previamente")
    void testErrorAlIniciarViajeNoAceptado() {
        Viaje viaje = crearViajeSolicitado(new Servicio("Estandar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS));

        // Intentamos iniciar directamente desde el estado SOLICITADO
        assertThrows(IllegalStateException.class, () -> viaje.iniciar(LocalDateTime.now()));
    }
    
    @Test
    @DisplayName("12. Finaliza un viaje exitosamente validando calificaciones y estado del conductor")
    void testFinalizarViajeConExito() {
        Usuario conductor = crearConductorDisponible("AA777AA", TipoVehiculo.AUTO,
                CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);
        Viaje viaje = crearViajeSolicitado(new Servicio("Estandar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS));

        LocalDateTime ahora = LocalDateTime.now();
        viaje.aceptar(ahora, conductor);
        viaje.iniciar(ahora.plusMinutes(5));
        
        // Finalizamos el viaje con calificaciones válidas
        viaje.finalizar(ahora.plusMinutes(20), md.modelo.CalificacionViaje.EXCELENTE, md.modelo.CalificacionViaje.MUY_BUENO);

        assertEquals(EstadoViaje.FINALIZADO, viaje.estadoActual());
        assertEquals(EstadoConductor.DISPONIBLE, conductor.getConductor().getEstadoConductor());
        assertEquals(md.modelo.CalificacionViaje.EXCELENTE, viaje.getCalificacionCliente());
    }

    @Test
    @DisplayName("13. Permite cancelar un viaje en estado solicitado por parte del cliente")
    void testCancelarViajePorCliente() {
        Viaje viaje = crearViajeSolicitado(new Servicio("Estandar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS));

        viaje.cancelar(LocalDateTime.now(), cliente, "Cambio de planes");

        assertEquals(EstadoViaje.CANCELADO, viaje.estadoActual());
        assertEquals(RolUsuario.CLIENTE, viaje.getRolCancela());
        assertEquals("Cambio de planes", viaje.getMotivoCancelacion());
    }

    @Test
    @DisplayName("14. Rechaza cancelar un viaje si lo intenta un usuario ajeno")
    void testErrorCancelarUsuarioNoAutorizado() {
        Viaje viaje = crearViajeSolicitado(new Servicio("Estandar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS));

        Usuario otroUsuario = new Usuario("Pedro", "+5491100000000", "pedro@email.com");

        assertThrows(IllegalArgumentException.class, () -> {
            viaje.cancelar(LocalDateTime.now(), otroUsuario, "Intento cancelar");
        });
    }
    
    @Test
    @DisplayName("15. Permite cancelar un viaje aceptado por parte del conductor")
    void testCancelarViajePorConductor() {
        Usuario conductor = crearConductorDisponible("AA888AA", TipoVehiculo.AUTO,
                CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);
        Viaje viaje = crearViajeSolicitado(new Servicio("Estandar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS));

        viaje.aceptar(LocalDateTime.now(), conductor);
        viaje.cancelar(LocalDateTime.now(), conductor, "Rotura de vehículo");

        assertEquals(EstadoViaje.CANCELADO, viaje.estadoActual());
        assertEquals(RolUsuario.CONDUCTOR, viaje.getRolCancela());
        assertEquals("Rotura de vehículo", viaje.getMotivoCancelacion());
        assertEquals(EstadoConductor.DISPONIBLE, conductor.getConductor().getEstadoConductor());
    }

    @Test
    @DisplayName("16. Rechaza un viaje en estado solicitado correctamente")
    void testRechazarViajeSolicitado() {
        Viaje viaje = crearViajeSolicitado(new Servicio("Estandar", 2000, 200, 100,
                TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS));

        viaje.rechazar(LocalDateTime.now());

        assertEquals(EstadoViaje.RECHAZADO, viaje.estadoActual());
    }
}