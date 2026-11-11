package md.ui.api;

import java.util.function.BiConsumer;

import md.modelo.Ubicacion;
import md.rutas.api.RutasViaje;

public interface VisualizadorMapa {

    /**
     * Dibuja los dos tramos del viaje (acercamiento y traslado) y los marcadores
     * del conductor, el origen y el destino.
     *
     * @param rutas     rutas calculadas para el viaje.
     * @param conductor ubicación actual del conductor.
     * @param origen    punto de origen del viaje.
     * @param destino   punto de destino del viaje.
     */
    void mostrar(RutasViaje rutas, Ubicacion conductor, Ubicacion origen, Ubicacion destino);

    /**
     * Registra el receptor que se invoca cuando el usuario hace clic en el mapa.
     *
     * @param receptor función que recibe el tipo de punto ({@code "origen"} o
     *                 {@code "destino"}) y la ubicación seleccionada.
     */
    void configurarSeleccion(BiConsumer<String, Ubicacion> receptor);

    /**
     * Indica al mapa que espere un clic del usuario para el tipo de punto dado.
     *
     * @param tipo {@code "origen"} o {@code "destino"}.
     */
    void activarSeleccion(String tipo);

    /**
     * Dibuja el marcador de un punto ya validado y confirmado.
     *
     * @param tipo      {@code "origen"} o {@code "destino"}.
     * @param ubicacion ubicación confirmada sobre la calle.
     */
    void mostrarMarcadorSeleccion(String tipo, Ubicacion ubicacion);

    /**
     * Cancela la espera de un clic en el mapa.
     */
    void cancelarSeleccion();

    /**
     * Elimina los marcadores de origen y destino seleccionados.
     */
    void limpiarSeleccion();

    /**
     * Elimina las líneas de ruta dibujadas anteriormente. No borra los marcadores
     * de selección.
     */
    void limpiarRutas();

    /**
     * Elimina un único marcador de selección.
     *
     * @param tipo {@code "origen"} o {@code "destino"}.
     */
    void limpiarMarcador(String tipo);
}
