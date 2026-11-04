package md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.rutas.api.PlanificadorRutas;
import md.rutas.api.PuntoRuta;
import md.rutas.api.RutaCalculada;
import md.rutas.api.RutaNoDisponibleException;
import md.rutas.api.RutasViaje;

class RutaCalculadaTest {

    private List<PuntoRuta> dosPuntos() {
        return new ArrayList<>(List.of(new PuntoRuta(-42.77, -65.04), new PuntoRuta(-42.78, -65.03)));
    }

    @Test
    @DisplayName("1. Una ruta valida expone sus datos y las conversiones a km y minutos")
    void testRutaValida() {
        RutaCalculada ruta = new RutaCalculada(1500, 120000, dosPuntos());

        assertEquals(1500, ruta.distanciaMetros(), 0.0001);
        assertEquals(1.5, ruta.distanciaKm(), 0.0001);
        assertEquals(2.0, ruta.duracionMinutos(), 0.0001);
        assertEquals(2, ruta.geometria().size());
    }

    @Test
    @DisplayName("2. Rechaza distancias negativas o no numericas")
    void testRechazaDistanciaInvalida() {
        assertThrows(IllegalArgumentException.class, () -> new RutaCalculada(-1, 1000, dosPuntos()));
        assertThrows(IllegalArgumentException.class, () -> new RutaCalculada(Double.NaN, 1000, dosPuntos()));
    }

    @Test
    @DisplayName("3. Rechaza una duracion negativa")
    void testRechazaDuracionNegativa() {
        assertThrows(IllegalArgumentException.class, () -> new RutaCalculada(100, -1, dosPuntos()));
    }

    @Test
    @DisplayName("4. Rechaza una geometria nula o con menos de dos puntos")
    void testRechazaGeometriaInsuficiente() {
        assertThrows(IllegalArgumentException.class, () -> new RutaCalculada(100, 1000, null));
        assertThrows(IllegalArgumentException.class,
                () -> new RutaCalculada(100, 1000, List.of(new PuntoRuta(-42.77, -65.04))));
    }

    @Test
    @DisplayName("5. La geometria es una copia: modificar la lista original no cambia la ruta")
    void testGeometriaEsCopiaInmutable() {
        List<PuntoRuta> original = dosPuntos();
        RutaCalculada ruta = new RutaCalculada(100, 1000, original);

        original.add(new PuntoRuta(-42.79, -65.02));

        assertEquals(2, ruta.geometria().size());
        assertThrows(UnsupportedOperationException.class,
                () -> ruta.geometria().add(new PuntoRuta(-42.79, -65.02)));
    }

    @Test
    @DisplayName("6. Dos rutas con los mismos valores son iguales (equals de record)")
    void testIgualdadPorValor() {
        assertEquals(new RutaCalculada(100, 1000, dosPuntos()), new RutaCalculada(100, 1000, dosPuntos()));
    }

    @Test
    @DisplayName("7. PuntoRuta valida latitud y longitud y acepta los limites")
    void testPuntoRuta() {
        assertThrows(IllegalArgumentException.class, () -> new PuntoRuta(91, 0));
        assertThrows(IllegalArgumentException.class, () -> new PuntoRuta(0, -181));
        assertThrows(IllegalArgumentException.class, () -> new PuntoRuta(Double.NaN, 0));
        assertEquals(90, new PuntoRuta(90, -180).latitud(), 0.0);
    }

    @Test
    @DisplayName("8. RutasViaje exige los dos tramos")
    void testRutasViajeExigeAmbosTramos() {
        RutaCalculada ruta = new RutaCalculada(100, 1000, dosPuntos());
        assertThrows(IllegalArgumentException.class, () -> new RutasViaje(null, ruta));
        assertThrows(IllegalArgumentException.class, () -> new RutasViaje(ruta, null));
        assertEquals(ruta, new RutasViaje(ruta, ruta).acercamiento());
    }

    @Test
    @DisplayName("9. Un consumidor usa el contrato sin conocer la implementacion, y close() no obliga a capturar nada")
    void testContratoConPlanificadorFalso() throws RutaNoDisponibleException {
        // Implementacion de mentira: sirve para probar a quien CONSUME el contrato
        class PlanificadorFalso implements PlanificadorRutas {
            boolean cerrado = false;

            @Override
            public RutaCalculada calcular(Ubicacion o, Ubicacion d, TipoVehiculo v) {
                return new RutaCalculada(500, 60000, dosPuntos());
            }

            @Override
            public Optional<Ubicacion> ajustarACalle(Ubicacion c, double max) {
                return Optional.of(c);
            }

            @Override
            public void close() {
                cerrado = true;
            }
        }
        PlanificadorFalso falso = new PlanificadorFalso();

        // Sin "catch (Exception e)": close() se redeclaro sin throws
        try (PlanificadorRutas motor = falso) {
            RutaCalculada ruta = motor.calcular(new Ubicacion(-42.77, -65.04), new Ubicacion(-42.78, -65.03),
                    TipoVehiculo.AUTO);
            assertEquals(0.5, ruta.distanciaKm(), 0.0001);
        }
        assertTrue(falso.cerrado);
    }
}
