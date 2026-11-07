package md.ui.impl.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import md.aplicacion.ContextoAplicacion;

public class MainApp extends Application {

	@Override
	public void start(Stage primaryStage) {
		try {

			// Carga tu archivo FXML de login y recibe el ResourceBundle para resolver
			// las claves que comienzan con %
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/view/Login.fxml"),
					ContextoAplicacion.getMensajes());
			Parent root = loader.load();

			primaryStage.setTitle(ContextoAplicacion.getMensajes().getString("login.app.title"));
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