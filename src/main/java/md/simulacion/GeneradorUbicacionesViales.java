
package md.simulacion;

import java.util.Random;

import md.config.CargarParametros;
import md.logica.ValidacionUbicaciones;
import md.modelo.Ubicacion;
import md.rutas.api.PlanificadorRutas;

public class GeneradorUbicacionesViales {
	/*fija una cantidad concreta para evitar que el método quede ejecutándose indefinidamente 
	 * cuando no consigue una ubicación apta
	 */
    private static final int MAX_INTENTOS = 100;

    private final PlanificadorRutas motorVial;
    private final Random random;

    /*recibir PlanificadorRutas por el constructor permite que el generador utilice cualquier 
     * implementación del contrato, sin depender de una biblioteca concreta (GraphHopperPlanificador).
     */
    public GeneradorUbicacionesViales(PlanificadorRutas motorVial) {

        if (motorVial == null) {
            throw new IllegalArgumentException("El motor vial no puede ser null.");
        }

        this.motorVial = motorVial;
        this.random = new Random();
    }

    /**
     * Genera una ubicación aleatoria dentro del área configurada
     * y la ajusta a una calle transitable.
     *
     * @return ubicación válida sobre una calle.
     * @throws IllegalStateException si no se encuentra una ubicación
     * válida después de varios intentos.
     */
    public Ubicacion generar() {

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {

            double latitud = generarCoordenada(CargarParametros.getLatitudMin(),CargarParametros.getLatitudMax());

            double longitud = generarCoordenada(CargarParametros.getLongitudMin(),CargarParametros.getLongitudMax());

            Ubicacion candidata = new Ubicacion(latitud, longitud);

            try {
                return ValidacionUbicaciones.validarYAjustar(candidata,motorVial);

            } catch (IllegalArgumentException e) {
                // La ubicación no encontró una calle válida.
                // Se genera otra candidata.
            }
        }

        throw new IllegalStateException("No se pudo generar una ubicación vial válida "
                						+ "después de " + MAX_INTENTOS + " intentos."
        );
    }

    /**
     * Genera un número aleatorio dentro del intervalo indicado.
     */
    private double generarCoordenada(double minimo, double maximo) {

        return minimo + random.nextDouble() * (maximo - minimo);
    }
}
