package md.ui.impl.javafx.util;

import java.io.IOException;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavegadorVistas {
    /**
     * Cambia la escena activa y retorna el controlador de la nueva vista.
     */
    public static <T> T cambiarEscena(Event event, String rutaFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(NavegadorVistas.class.getResource(rutaFXML));
            Parent root = loader.load();
            // Obtiene el Stage a partir del componente que disparó el evento
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            // Retorna el controlador genérico para inyectar datos
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}