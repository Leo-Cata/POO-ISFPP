package md.rutas.api;

/**
 * Los dos tramos de un viaje, con sus metricas por separado:
 * el acercamiento (conductor hasta el origen) y el traslado (origen hasta el destino).
 */
public record RutasViaje(RutaCalculada acercamiento, RutaCalculada traslado) {

    public RutasViaje {
        if (acercamiento == null) {
            throw new IllegalArgumentException("El tramo de acercamiento no puede ser null.");
        }
        if (traslado == null) {
            throw new IllegalArgumentException("El tramo de traslado no puede ser null.");
        }
    }
}
