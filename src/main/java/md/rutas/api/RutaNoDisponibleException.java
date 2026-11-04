package md.rutas.api;

/**
 * El motor de rutas no pudo calcular un recorrido (no hay camino, o un punto cae fuera del mapa).
 * Es una excepcion propia del contrato: no deja escapar excepciones internas del proveedor.
 */
public class RutaNoDisponibleException extends Exception {

    private static final long serialVersionUID = 1L;

    public RutaNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public RutaNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
