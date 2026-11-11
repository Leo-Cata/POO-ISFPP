
package md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import md.config.CargarParametros;
import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.rutas.api.PlanificadorRutas;
import md.rutas.api.RutaCalculada;
import md.rutas.api.RutaNoDisponibleException;
import md.simulacion.GeneradorUbicacionesViales;

class GeneradorUbicacionesVialesTest {

    private MotorFalso motor;
    private GeneradorUbicacionesViales generador;

    @BeforeEach
    void setUp() {motor = new MotorFalso();
        generador = new GeneradorUbicacionesViales(motor);
    }

    @Test
    @DisplayName("1. Rechaza un motor vial nulo")
    void testRechazaMotorNulo() {

        assertThrows(IllegalArgumentException.class,() -> new GeneradorUbicacionesViales(null));
    }

    @Test
    @DisplayName("2. Genera una ubicación dentro de los límites configurados")
    void testGeneraUbicacionDentroDelArea() {

        motor.aceptarSiguienteCandidata = true;

        Ubicacion resultado = generador.generar();

        assertSame(motor.candidatas.get(0), resultado);
        assertEquals(1, motor.candidatas.size());

        assertTrue(resultado.getLatitud() >= CargarParametros.getLatitudMin()
                && resultado.getLatitud() <= CargarParametros.getLatitudMax());

        assertTrue(resultado.getLongitud() >= CargarParametros.getLongitudMin()
                && resultado.getLongitud() <= CargarParametros.getLongitudMax());

        assertEquals(CargarParametros.getToleranciaAjusteMetros(),motor.ultimaTolerancia,0.0001);
    }

    @Test
    @DisplayName("3. Reintenta cuando una candidata no encuentra una calle")
    void testReintentaHastaEncontrarCalle() {

        motor.respuestas.add(Optional.empty());
        motor.respuestas.add(Optional.empty());

        motor.aceptarSiguienteCandidata = true;

        Ubicacion resultado = generador.generar();

        assertEquals(3, motor.candidatas.size());
        assertSame(motor.candidatas.get(2), resultado);
    }

    @Test
    @DisplayName("4. Falla después de agotar los intentos disponibles")
    void testAgotaLosIntentos() {

        assertThrows(IllegalStateException.class,() -> generador.generar());

        assertEquals(100, motor.candidatas.size());
    }

    private static class MotorFalso implements PlanificadorRutas {

        private final Queue<Optional<Ubicacion>> respuestas =new ArrayDeque<>();

        private final List<Ubicacion> candidatas =new ArrayList<>();

        private boolean aceptarSiguienteCandidata;

        private double ultimaTolerancia;

        @Override
        public RutaCalculada calcular(Ubicacion origen,Ubicacion destino,TipoVehiculo tipoVehiculo
        ) throws RutaNoDisponibleException {

            throw new UnsupportedOperationException("Este test no necesita calcular recorridos.");
        }


        @Override
        public Optional<Ubicacion> ajustarACalle(Ubicacion candidata,double maxDistanciaMetros) {

        	candidatas.add(candidata);
        	ultimaTolerancia = maxDistanciaMetros;

        	// Primero consume las respuestas pendientes.
        	if (!respuestas.isEmpty()) {
        		return respuestas.remove();
        	}

        	// Si no quedan respuestas, acepta la siguiente candidata
        	// cuando así lo indique el test.
        	if (aceptarSiguienteCandidata) {
        		aceptarSiguienteCandidata = false;
        		return Optional.of(candidata);
        	}

        	return Optional.empty();
        }


        @Override
        public void close() {
        }
    }
}
