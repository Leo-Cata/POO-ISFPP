package md.ui.impl.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import md.aplicacion.ContextoAplicacion;
import md.config.DAOLoader;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // carga los datos primero
            ContextoAplicacion.inicializar(DAOLoader.getFactory());

            // Carga tu archivo FXML de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/view/Login.fxml"));
            Parent root = loader.load();

            primaryStage.setTitle("Movilidad Digital - Iniciar Sesión");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Causa real: " + e.getCause());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}