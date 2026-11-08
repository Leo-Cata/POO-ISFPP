package md.ui.impl.javafx.controller.util;

import javafx.scene.control.Alert;

public class AlertaUtil {

    // Método auxiliar para mostrar alertas de JavaFX
    public static void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
