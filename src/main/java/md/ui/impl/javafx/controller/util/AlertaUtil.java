package md.ui.impl.javafx.controller.util;

import javafx.scene.control.Alert;

public class AlertaUtil {

    /**
     * Muestra una alerta de advertencia y espera a que el usuario la cierre.
     *
     * @param titulo  título de la ventana.
     * @param mensaje texto a mostrar.
     */
    public static void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra una alerta del tipo indicado y espera a que el usuario la cierre.
     *
     * @param tipo    tipo de alerta (información, advertencia, error, etc.).
     * @param titulo  título de la ventana.
     * @param mensaje texto a mostrar.
     */
    public static void mostrarAlerta(
            Alert.AlertType tipo, String titulo, String mensaje) {

        Alert alert = new Alert(tipo);

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        alert.showAndWait();
    }
}
