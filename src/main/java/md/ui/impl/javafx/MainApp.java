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

			// Cargamos la pantalla de login.
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/ui/javafx/view/login.fxml"),
					ContextoAplicacion.getMensajes());

			Parent root = loader.load();

			primaryStage.setTitle(
					ContextoAplicacion.getMensajes().getString("login.app.title"));

			primaryStage.setScene(new Scene(root));
			primaryStage.setOnCloseRequest(event -> ContextoAplicacion.cerrar());
			primaryStage.show();

		} catch (Exception e) {
			System.err.println("Causa real: " + e.getCause());
			e.printStackTrace();
		}
	}

	/**
	 * Lanza la aplicación JavaFX.
	 *
	 * @param args argumentos de línea de comandos que se pasan a {@code launch}.
	 */
	public static void main(String[] args) {
		launch(args);
	}
}