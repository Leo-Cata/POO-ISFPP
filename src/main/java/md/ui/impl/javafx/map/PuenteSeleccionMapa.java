package md.ui.impl.javafx.map;

import java.util.function.BiConsumer;
import javafx.application.Platform;
import md.modelo.Ubicacion;

public class PuenteSeleccionMapa {
    private BiConsumer<String, Ubicacion> receptor;

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
