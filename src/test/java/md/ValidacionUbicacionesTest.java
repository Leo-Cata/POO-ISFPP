
package md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import md.config.CargarParametros;
import md.logica.ValidacionUbicaciones;
import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.rutas.api.PlanificadorRutas;
import md.rutas.api.RutaCalculada;
import md.rutas.api.RutaNoDisponibleException;

class ValidacionUbicacionesTest {

    private PlanificadorFalso motor;
    private ValidacionUbicaciones validador;

    @BeforeEach
    void setUp() {
        motor = new PlanificadorFalso();
        validador = new ValidacionUbicaciones(
                CargarParametros.getToleranciaAjusteMetros(),
                CargarParametros.getLatitudMin(), CargarParametros.getLatitudMax(),
                CargarParametros.getLongitudMin(), CargarParametros.getLongitudMax());
    }

    // Ubicación calculada a partir de los límites configurados del proyecto.
    private Ubicacion ubicacionCentral() {
        double latitud = (CargarParametros.getLatitudMin() + CargarParametros.getLatitudMax()) / 2.0;

        double longitud = (CargarParametros.getLongitudMin()+ CargarParametros.getLongitudMax()) / 2.0;

        return new Ubicacion(latitud, longitud);
    }

    // Simula el comportamiento del motor vial sin utilizar GraphHopper.
    private static class PlanificadorFalso implements PlanificadorRutas {

        Optional<Ubicacion> respuesta = Optional.empty();
        int llamadas;
        double toleranciaRecibida;

        @Override
        public RutaCalculada calcular(Ubicacion origen,Ubicacion destino,TipoVehiculo tipo
        ) throws RutaNoDisponibleException {
            throw new UnsupportedOperationException("Este test no necesita calcular rutas.");
        }

        @Override
        public Optional<Ubicacion> ajustarACalle(Ubicacion candidata,double maxDistanciaMetros
        ) {
            llamadas++;
            toleranciaRecibida = maxDistanciaMetros;
            return respuesta;
        }

        @Override
        public void close() {
        }
    }

    @Test
    @DisplayName("1. Devuelve la ubicación ajustada correctamente")
    void testUbicacionValida() {
        Ubicacion candidata = ubicacionCentral();

        Ubicacion ajustada = new Ubicacion(candidata.getLatitud() + 0.0001,candidata.getLongitud());

        motor.respuesta = Optional.of(ajustada);

        Ubicacion resultado = validador.validarYAjustar(candidata,motor);

        assertSame(ajustada, resultado);
        assertEquals(1, motor.llamadas);
        assertEquals(CargarParametros.getToleranciaAjusteMetros(),motor.toleranciaRecibida,0.0001);
    }

    @Test
    @DisplayName("2. Rechaza una ubicación nula")
    void testUbicacionNula() {
        assertThrows(IllegalArgumentException.class,() -> validador.validarYAjustar(null, motor));

        assertEquals(0, motor.llamadas);
    }

    @Test
    @DisplayName("3. Rechaza un motor vial nulo")
    void testMotorNulo() {
        assertThrows(IllegalArgumentException.class,() -> validador.validarYAjustar(ubicacionCentral(),null)
        );

        assertEquals(0, motor.llamadas);
    }

    @Test
    @DisplayName("4. Rechaza coordenadas fuera del área de cobertura")
    void testUbicacionFueraDelArea() {
        Ubicacion fueraDelArea = new Ubicacion(CargarParametros.getLatitudMax() + 1,ubicacionCentral().getLongitud());

        assertThrows(IllegalArgumentException.class,() -> validador.validarYAjustar(fueraDelArea,motor));

        assertEquals(0, motor.llamadas);
    }

    @Test
    @DisplayName("5. Rechaza una ubicación sin calles cercanas")
    void testNoEncuentraCalleCercana() {
        motor.respuesta = Optional.empty();

        assertThrows(IllegalArgumentException.class,() -> validador.validarYAjustar(ubicacionCentral(),motor)
        );

        assertEquals(1, motor.llamadas);
    }

    @Test
    @DisplayName("6. Rechaza una respuesta nula del motor vial")
    void testRespuestaNulaDelMotor() {motor.respuesta = null;

        assertThrows(IllegalStateException.class,() -> validador.validarYAjustar(ubicacionCentral(),motor)
        );

        assertEquals(1, motor.llamadas);
    }

    @Test
    @DisplayName("7. Rechaza una ubicación ajustada fuera del área")
    void testUbicacionAjustadaFueraDelArea() {
        Ubicacion ajustadaFueraDelArea = new Ubicacion(CargarParametros.getLatitudMin() - 1,ubicacionCentral().getLongitud());

        motor.respuesta = Optional.of(ajustadaFueraDelArea);

        assertThrows(IllegalArgumentException.class,() -> validador.validarYAjustar(ubicacionCentral(),motor));
    }

    @Test
    @DisplayName("8. Rechaza coordenadas no finitas")
    void testCoordenadasNoFinitas() {
        Ubicacion invalida = new Ubicacion(Double.NaN,ubicacionCentral().getLongitud());

        assertThrows(IllegalArgumentException.class,() -> validador.validarYAjustar(invalida,motor));

        assertEquals(0, motor.llamadas);
    }
}
