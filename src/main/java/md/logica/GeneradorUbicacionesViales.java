
package md.logica;

import java.util.Random;

import md.modelo.Ubicacion;
import md.rutas.api.PlanificadorRutas;

public class GeneradorUbicacionesViales {
    /*
     * fija una cantidad concreta para evitar que el método quede ejecutándose
     * indefinidamente
     * cuando no consigue una ubicación apta
     */
    private static final int MAX_INTENTOS = 100;

    private final PlanificadorRutas motorVial;
    private final ValidacionUbicaciones validador;
    private final Random random;

    private final double latitudMin;
    private final double latitudMax;
    private final double longitudMin;
    private final double longitudMax;

    /*
     * recibir PlanificadorRutas por el constructor permite que el generador utilice
     * cualquier
     * implementación del contrato, sin depender de una biblioteca concreta
     * (GraphHopperPlanificador).
     */
    public GeneradorUbicacionesViales(
            PlanificadorRutas motorVial,
            ValidacionUbicaciones validador,
            double latitudMin,
            double latitudMax,
            double longitudMin,
            double longitudMax) {

        if (motorVial == null || validador == null) {
            throw new IllegalArgumentException("El motor vial y el validador son obligatorios.");
        }

        // Conservá acá las validaciones de los límites geográficos.

        this.motorVial = motorVial;
        this.validador = validador;
        this.latitudMin = latitudMin;
        this.latitudMax = latitudMax;
        this.longitudMin = longitudMin;
        this.longitudMax = longitudMax;
        this.random = new Random();
    }

    /**
     * Genera una ubicación aleatoria dentro del área configurada
     * y la ajusta a una calle transitable.
     *
     * @return ubicación válida sobre una calle.
     * @throws IllegalStateException si no se encuentra una ubicación
     *                               válida después de varios intentos.
     */
    public Ubicacion generar() {

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {

            double latitud = generarCoordenada(latitudMin, latitudMax);
            double longitud = generarCoordenada(longitudMin, longitudMax);

            Ubicacion candidata = new Ubicacion(latitud, longitud);

            try {
                return validador.validarYAjustar(candidata, motorVial);
            } catch (IllegalArgumentException e) {
                // La candidata no encontró una calle válida.
                // El ciclo continúa con un nuevo intento.
            }
        }

        throw new IllegalStateException(
                "No se pudo generar una ubicación vial válida después de "
                        + MAX_INTENTOS + " intentos.");
    }

    /**
     * Genera un número aleatorio dentro del intervalo indicado.
     */
    private double generarCoordenada(double minimo, double maximo) {

        return minimo + random.nextDouble() * (maximo - minimo);
    }
}
