package md.rutas.impl;

import md.rutas.api.PlanificadorRutas;

public final class PlanificadorRutasFactory {

    private PlanificadorRutasFactory() {
    }

    /**
     * Crea la implementación del motor de rutas según la configuración recibida.
     *
     * @param motor           nombre del motor que se utilizará.
     * @param archivoOsm      ruta del archivo OSM.
     * @param directorioCache directorio donde se almacena la caché del motor.
     * @return implementación del contrato PlanificadorRutas.
     * @throws IllegalArgumentException si el motor no está soportado.
     */
    public static PlanificadorRutas crear(
            String motor,
            String archivoOsm,
            String directorioCache) {

        return switch (motor) {

            case "GRAPHHOPPER" -> new GraphHopperPlanificador(
                    archivoOsm,
                    directorioCache);

            default -> throw new IllegalArgumentException(
                    "Motor de rutas no soportado: " + motor);
        };
    }
}