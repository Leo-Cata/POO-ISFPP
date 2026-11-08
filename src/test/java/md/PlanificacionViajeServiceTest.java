package md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import md.logica.PlanificacionViajeService;
import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.modelo.Vehiculo;
import md.rutas.api.PlanificadorRutas;
import md.rutas.api.PuntoRuta;
import md.rutas.api.RutaCalculada;
import md.rutas.api.RutaNoDisponibleException;
import md.rutas.api.RutasViaje;

class PlanificacionViajeServiceTest {

    private static final Ubicacion POSICION_VEHICULO = new Ubicacion(-42.780, -65.025);
    private static final Ubicacion ORIGEN = new Ubicacion(-42.770, -65.040);
    private static final Ubicacion DESTINO = new Ubicacion(-42.775, -65.030);

    // Motor de mentira: anota cada consulta y devuelve las rutas preparadas, en orden
    private static class PlanificadorFalso implements PlanificadorRutas {
        final List<Ubicacion> origenes = new ArrayList<>();
        final List<Ubicacion> destinos = new ArrayList<>();
        final List<TipoVehiculo> tipos = new ArrayList<>();
        final List<RutaCalculada> respuestas = new ArrayList<>();
        int fallaEnLaLlamada = 0; // 0 = nunca falla; 1 = falla la primera consulta; 2 = la segunda

        @Override
        public RutaCalculada calcular(Ubicacion origen, Ubicacion destino, TipoVehiculo tipo)
                throws RutaNoDisponibleException {
            origenes.add(origen);
            destinos.add(destino);
            tipos.add(tipo);
            int numeroDeLlamada = origenes.size();
            if (numeroDeLlamada == fallaEnLaLlamada) {
                throw new RutaNoDisponibleException("No hay camino");
            }
            return respuestas.get(numeroDeLlamada - 1);
        }

        @Override
        public Optional<Ubicacion> ajustarACalle(Ubicacion candidata, double maxDistanciaMetros) {
            return Optional.of(candidata);
        }

        @Override
        public void close() {
        }
    }

    private RutaCalculada rutaAcercamiento;
    private RutaCalculada rutaTraslado;
    private PlanificadorFalso planificador;
    private PlanificacionViajeService servicio;

    @BeforeEach
    void setUp() {
        rutaAcercamiento = new RutaCalculada(500, 60000,
                List.of(new PuntoRuta(-42.780, -65.025), new PuntoRuta(-42.770, -65.040)));
        rutaTraslado = new RutaCalculada(900, 120000,
                List.of(new PuntoRuta(-42.770, -65.040), new PuntoRuta(-42.775, -65.030)));
        planificador = new PlanificadorFalso();
        planificador.respuestas.add(rutaAcercamiento);
        planificador.respuestas.add(rutaTraslado);
        servicio = new PlanificacionViajeService(planificador);
    }

    private Vehiculo vehiculo(TipoVehiculo tipo, Ubicacion ubicacion) {
        Vehiculo v = new Vehiculo("AA111AA", "Modelo", 4, tipo, CategoriaVehiculo.ESTANDAR, TipoServicio.PASAJEROS);
        if (ubicacion != null) {
            v.setUbicacion(ubicacion);
        }
        return v;
    }

    @Test
    @DisplayName("1. No acepta un planificador nulo")
    void testRechazaPlanificadorNulo() {
        assertThrows(IllegalArgumentException.class, () -> new PlanificacionViajeService(null));
    }

    @Test
    @DisplayName("2. Pide primero el acercamiento (vehiculo a origen) y despues el traslado (origen a destino)")
    void testPideLosDosTramosEnOrden() throws RutaNoDisponibleException {
        RutasViaje rutas = servicio.planificar(vehiculo(TipoVehiculo.AUTO, POSICION_VEHICULO), ORIGEN, DESTINO);

        assertEquals(2, planificador.origenes.size());
        assertSame(POSICION_VEHICULO, planificador.origenes.get(0));
        assertSame(ORIGEN, planificador.destinos.get(0));
        assertSame(ORIGEN, planificador.origenes.get(1));
        assertSame(DESTINO, planificador.destinos.get(1));
        assertSame(rutaAcercamiento, rutas.acercamiento());
        assertSame(rutaTraslado, rutas.traslado());
    }

    @Test
    @DisplayName("3. Usa el tipo del vehiculo en los dos tramos (auto)")
    void testUsaElTipoDelVehiculoAuto() throws RutaNoDisponibleException {
        servicio.planificar(vehiculo(TipoVehiculo.AUTO, POSICION_VEHICULO), ORIGEN, DESTINO);

        assertEquals(List.of(TipoVehiculo.AUTO, TipoVehiculo.AUTO), planificador.tipos);
    }

    @Test
    @DisplayName("4. Usa el tipo del vehiculo en los dos tramos (moto)")
    void testUsaElTipoDelVehiculoMoto() throws RutaNoDisponibleException {
        servicio.planificar(vehiculo(TipoVehiculo.MOTO, POSICION_VEHICULO), ORIGEN, DESTINO);

        assertEquals(List.of(TipoVehiculo.MOTO, TipoVehiculo.MOTO), planificador.tipos);
    }

    @Test
    @DisplayName("5. Si falla el acercamiento propaga el error y no pide el traslado")
    void testFallaElAcercamiento() {
        planificador.fallaEnLaLlamada = 1;

        assertThrows(RutaNoDisponibleException.class,
                () -> servicio.planificar(vehiculo(TipoVehiculo.AUTO, POSICION_VEHICULO), ORIGEN, DESTINO));
        assertEquals(1, planificador.origenes.size());
    }

    @Test
    @DisplayName("6. Si falla el traslado tambien propaga el error")
    void testFallaElTraslado() {
        planificador.fallaEnLaLlamada = 2;

        assertThrows(RutaNoDisponibleException.class,
                () -> servicio.planificar(vehiculo(TipoVehiculo.AUTO, POSICION_VEHICULO), ORIGEN, DESTINO));
        assertEquals(2, planificador.origenes.size());
    }

    @Test
    @DisplayName("7. Rechaza argumentos nulos sin consultar al motor")
    void testRechazaArgumentosNulos() {
        Vehiculo v = vehiculo(TipoVehiculo.AUTO, POSICION_VEHICULO);

        assertThrows(IllegalArgumentException.class, () -> servicio.planificar(null, ORIGEN, DESTINO));
        assertThrows(IllegalArgumentException.class, () -> servicio.planificar(v, null, DESTINO));
        assertThrows(IllegalArgumentException.class, () -> servicio.planificar(v, ORIGEN, null));
        assertEquals(0, planificador.origenes.size());
    }

    @Test
    @DisplayName("8. Un vehiculo sin posicion es un error de estado y no se consulta al motor")
    void testVehiculoSinUbicacion() {
        assertThrows(IllegalStateException.class,
                () -> servicio.planificar(vehiculo(TipoVehiculo.AUTO, null), ORIGEN, DESTINO));
        assertEquals(0, planificador.origenes.size());
    }
}
