package md.rutas.impl;

import md.config.CargarParametros;
import md.rutas.api.PlanificadorRutas;

public final class PlanificadorRutasFactory {

    private PlanificadorRutasFactory() {
    }

    /**
     * Crea el motor de rutas configurado para la aplicación.
     *
     * @return implementación del contrato PlanificadorRutas.
     */
    public static PlanificadorRutas crear() {

        String motor = CargarParametros.getMotorRutas();

        return switch (motor) {

            case "GRAPHHOPPER" -> new GraphHopperPlanificador(
                    CargarParametros.getArchivoOsm(),
                    CargarParametros.getDirectorioCacheRutas()
            );

            default -> throw new IllegalArgumentException("Motor de rutas no soportado: " + motor);
        };
    }
}