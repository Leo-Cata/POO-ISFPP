package md.rutas.api;

/**
 * Un punto de la geometria de una ruta, en grados decimales.
 * Es un record: se crea una sola vez y despues no puede cambiar.
 */
public record PuntoRuta(double latitud, double longitud) {

    // Constructor compacto: valida los valores antes de que Java los asigne a los campos
    public PuntoRuta {
        if (!Double.isFinite(latitud) || latitud < -90 || latitud > 90) {
            throw new IllegalArgumentException("Latitud invalida: " + latitud);
        }
        if (!Double.isFinite(longitud) || longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("Longitud invalida: " + longitud);
        }
    }
}
