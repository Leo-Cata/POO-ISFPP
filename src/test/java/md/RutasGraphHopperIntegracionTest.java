package md;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.rutas.api.RutaCalculada;
import md.rutas.impl.GraphHopperPlanificador;

/**
 * Pruebas de integración contra el OSM y la caché distribuidos con el proyecto.
 * Se ejecutan de forma explícita porque abren GraphHopper y pueden consumir
 * memoria y tiempo de inicialización.
 */
@EnabledIfSystemProperty(named = "md.integration.graphhopper", matches = "true")
class RutasGraphHopperIntegracionTest {

    private static final Path ARCHIVO_OSM =
            Path.of("mapas", "puerto_madryn_ampliado.osm");

    private static final Path DIRECTORIO_CACHE =
            Path.of("mapas", "graphhopper-cache-madryn-ampliado");

    private static final Ubicacion PUNTO_A = new Ubicacion(-42.770, -65.040);
    private static final Ubicacion PUNTO_B = new Ubicacion(-42.775, -65.030);
    private static final double DISTANCIA_MAXIMA_AJUSTE_METROS = 1000.0;

    private static GraphHopperPlanificador motor;

    @BeforeAll
    static void iniciarMotor() {
        assumeTrue(
                Files.isRegularFile(ARCHIVO_OSM),
                "No se encontró el extracto OSM: " + ARCHIVO_OSM.toAbsolutePath()
        );
        assumeTrue(
                Files.isDirectory(DIRECTORIO_CACHE)
                        && Files.exists(DIRECTORIO_CACHE.resolve("properties")),
                "No se encontró una caché GraphHopper preparada en: "
                        + DIRECTORIO_CACHE.toAbsolutePath()
        );

        motor = new GraphHopperPlanificador(
                ARCHIVO_OSM.toString(),
                DIRECTORIO_CACHE.toString()
        );
    }

    @AfterAll
    static void cerrarMotor() {
        if (motor != null) {
            motor.close();
            motor = null;
        }
    }

    @Test
    @DisplayName("1. GraphHopper calcula un recorrido para automóvil")
    void testCalculaRutaParaAuto() throws Exception {
        Ubicacion origen = ajustarAPuntoTransitable(PUNTO_A);
        Ubicacion destino = ajustarAPuntoTransitable(PUNTO_B);

        RutaCalculada ruta = motor.calcular(origen, destino, TipoVehiculo.AUTO);

        assertTrue(ruta.distanciaMetros() >= 0);
        assertTrue(ruta.duracionMilisegundos() >= 0);
        assertTrue(ruta.geometria().size() >= 2);
    }

    @Test
    @DisplayName("2. GraphHopper calcula un recorrido para motocicleta")
    void testCalculaRutaParaMoto() throws Exception {
        Ubicacion origen = ajustarAPuntoTransitable(PUNTO_A);
        Ubicacion destino = ajustarAPuntoTransitable(PUNTO_B);

        RutaCalculada ruta = motor.calcular(origen, destino, TipoVehiculo.MOTO);

        assertTrue(ruta.distanciaMetros() >= 0);
        assertTrue(ruta.duracionMilisegundos() >= 0);
        assertTrue(ruta.geometria().size() >= 2);
    }

    private Ubicacion ajustarAPuntoTransitable(Ubicacion candidata) {
        Optional<Ubicacion> ajustada = motor.ajustarACalle(
                candidata,
                DISTANCIA_MAXIMA_AJUSTE_METROS
        );

        return ajustada.orElseThrow(() -> new AssertionError(
                "No se encontró una calle cercana a " + candidata
        ));
    }
}
