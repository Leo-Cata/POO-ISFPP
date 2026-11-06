package md.logica;

import java.util.Optional;

import md.config.CargarParametros;
import md.modelo.Ubicacion;
import md.rutas.api.PlanificadorRutas;

public final class ValidacionUbicaciones {

    private ValidacionUbicaciones() {
    }

    public static Ubicacion validarYAjustar(
            Ubicacion candidata,
            PlanificadorRutas motorVial) {

        if (candidata == null || motorVial == null) {
            throw new IllegalArgumentException(
                    "La ubicación y el motor vial son obligatorios."
            );
        }

        validarCoordenadas(candidata);

        validarDentroDelArea(candidata);

        double tolerancia =
                CargarParametros.getToleranciaAjusteMetros();

        if (!Double.isFinite(tolerancia) || tolerancia <= 0) {
            throw new IllegalStateException(
                    "La tolerancia de ajuste no es válida."
            );
        }

        Optional<Ubicacion> resultado =
                motorVial.ajustarACalle(candidata, tolerancia);

        if (resultado == null) {
            throw new IllegalStateException(
                    "El motor vial devolvió un resultado inválido."
            );
        }

        Ubicacion ajustada = resultado.orElseThrow(
                () -> new IllegalArgumentException(
                        "No se encontró una calle cercana a la ubicación."
                )
        );

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

        boolean fueraDelArea =
                ubicacion.getLatitud() < CargarParametros.getLatitudMin() ||
                ubicacion.getLatitud() > CargarParametros.getLatitudMax() ||
                ubicacion.getLongitud() < CargarParametros.getLongitudMin() ||
                ubicacion.getLongitud() > CargarParametros.getLongitudMax();

        if (fueraDelArea) {
            throw new IllegalArgumentException(
                    "La ubicación está fuera del área de cobertura."
            );
        }
    }
}