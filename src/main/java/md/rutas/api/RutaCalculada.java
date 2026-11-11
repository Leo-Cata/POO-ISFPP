package md.rutas.api;

import java.util.List;

/**
 * Resultado de calcular un tramo vial: cuanto mide, cuanto demora y por donde
 * pasa.
 * No contiene ningun tipo de GraphHopper: es el formato neutral que ven los
 * demas paquetes.
 *
 * @param distanciaMetros      largo del recorrido por calles, en metros
 * @param duracionMilisegundos tiempo estimado del recorrido, en milisegundos
 * @param geometria            puntos del recorrido, en el orden en que se
 *                             recorren
 */
public record RutaCalculada(double distanciaMetros, long duracionMilisegundos, List<PuntoRuta> geometria) {

    public RutaCalculada {
        if (!Double.isFinite(distanciaMetros) || distanciaMetros < 0) {
            throw new IllegalArgumentException("La distancia debe ser un numero mayor o igual a cero.");
        }
        if (duracionMilisegundos < 0) {
            throw new IllegalArgumentException("La duracion no puede ser negativa.");
        }
        if (geometria == null || geometria.size() < 2) {
            throw new IllegalArgumentException("La geometria necesita al menos dos puntos.");
        }
        // Copia inmutable: si quien nos paso la lista la modifica despues, la ruta no
        // se entera
        geometria = List.copyOf(geometria);
    }

    /**
     * Devuelve la distancia del recorrido en kilómetros.
     *
     * @return distancia en kilómetros ({@code distanciaMetros / 1000}).
     */
    public double distanciaKm() {
        return distanciaMetros / 1000.0;
    }

    /**
     * Devuelve la duración estimada del recorrido en minutos.
     *
     * @return duración en minutos ({@code duracionMilisegundos / 60000}).
     */
    public double duracionMinutos() {
        return duracionMilisegundos / 60000.0;
    }
}
