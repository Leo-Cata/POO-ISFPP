package md.ui.impl.javafx.map;

import java.util.function.BiConsumer;
import javafx.application.Platform;
import md.modelo.Ubicacion;

public class PuenteSeleccionMapa {
    private BiConsumer<String, Ubicacion> receptor;

    /**
     * Registra el receptor que será notificado cuando JavaScript informe un clic
     * en el mapa.
     *
     * @param receptor función que recibe el tipo de punto ({@code "origen"} o
     *                 {@code "destino"}) y la ubicación seleccionada.
     */
    public void registrarReceptor(BiConsumer<String, Ubicacion> receptor) {
        this.receptor = receptor;
    }

    /**
     * Invocado desde JavaScript (mapa.html) cuando el usuario hace clic en el mapa.
     */
    public void seleccionarPunto(String tipo, double latitud, double longitud) {
        if (Double.isFinite(latitud) && Double.isFinite(longitud) && receptor != null) {
            Ubicacion ubicacion = new Ubicacion(latitud, longitud);
            // Delega la ejecución al hilo principal de JavaFX
            Platform.runLater(() -> receptor.accept(tipo, ubicacion));
        }
    }
}
