package md.logica;

import java.util.Optional;

import md.config.CargarParametros;
import md.modelo.Ubicacion;
import md.rutas.api.PlanificadorRutas;

public final class ValidacionUbicaciones {

    private ValidacionUbicaciones() {
    }

    /**
     * Valida una ubicación y la ajusta a la calle transitable más cercana.
     * <p>
     * Comprueba que las coordenadas sean válidas y estén dentro del área de
     * simulación, pide al motor vial la calle más próxima dentro de la tolerancia
     * configurada ({@code ubicacion.tolerancia.ajuste.metros}) y vuelve a validar
     * el punto ajustado.
     *
     * @param candidata ubicación elegida por el usuario o generada.
     * @param motorVial motor de rutas usado para ajustar el punto a una calle.
     * @return la ubicación ajustada, apta para calcular rutas.
     * @throws IllegalArgumentException si algún parámetro es {@code null}, las
     *                                  coordenadas
     *                                  son inválidas, están fuera del área o no hay
     *                                  una
     *                                  calle cercana.
     * @throws IllegalStateException    si la tolerancia configurada no es válida o
     *                                  el motor
     *                                  vial devuelve un resultado inválido.
     */
    public static Ubicacion validarYAjustar(
            Ubicacion candidata,
            PlanificadorRutas motorVial) {

        if (candidata == null || motorVial == null) {
            throw new IllegalArgumentException(
                    "La ubicación y el motor vial son obligatorios.");
        }

        validarCoordenadas(candidata);

        validarDentroDelArea(candidata);

        double tolerancia = CargarParametros.getToleranciaAjusteMetros();

        if (!Double.isFinite(tolerancia) || tolerancia <= 0) {
            throw new IllegalStateException("La tolerancia de ajuste no es válida.");
        }

        Optional<Ubicacion> resultado = motorVial.ajustarACalle(candidata, tolerancia);

        if (resultado == null) {
            throw new IllegalStateException("El motor vial devolvió un resultado inválido.");
        }

        Ubicacion ajustada = resultado.orElseThrow(() -> new IllegalArgumentException(
                "No se encontró una calle cercana a la ubicación."));

        validarCoordenadas(ajustada);
        validarDentroDelArea(ajustada);

        return ajustada;
    }

    private static void validarCoordenadas(Ubicacion ubicacion) {

        double latitud = ubicacion.getLatitud();
        double longitud = ubicacion.getLongitud();

        if (!Double.isFinite(latitud) ||
                latitud < -90 || latitud > 90) {
            throw new IllegalArgumentException("Latitud inválida.");
        }

        if (!Double.isFinite(longitud) ||
                longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("Longitud inválida.");
        }
    }

    private static void validarDentroDelArea(Ubicacion ubicacion) {

        boolean fueraDelArea = ubicacion.getLatitud() < CargarParametros.getLatitudMin() ||
                ubicacion.getLatitud() > CargarParametros.getLatitudMax() ||
                ubicacion.getLongitud() < CargarParametros.getLongitudMin() ||
                ubicacion.getLongitud() > CargarParametros.getLongitudMax();

        if (fueraDelArea) {
            throw new IllegalArgumentException("La ubicación está fuera del área de cobertura.");
        }
    }
}