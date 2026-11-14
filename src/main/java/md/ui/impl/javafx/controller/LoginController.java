package md.ui.impl.javafx.controller;

import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.util.StringConverter;
import md.aplicacion.ContextoAplicacion;
import md.logica.GestorViajes;
import md.modelo.RolUsuario;
import md.modelo.Usuario;
import md.ui.impl.javafx.util.NavegadorVistas;
import net.datastructures.ProbeHashMap;
import md.ui.impl.javafx.controller.util.AlertaUtil;

public class LoginController {

	@FXML
	private ResourceBundle resources;
	@FXML
	private ComboBox<Usuario> comboUsuarios;
	@FXML
	private ToggleGroup tipoUsuario;
	@FXML
	private RadioButton radioConductor, radioCliente;

	private ProbeHashMap<String, Usuario> usuarios = new ProbeHashMap<>();

	private final GestorViajes gestorViajes = ContextoAplicacion.getGestorViajes();

	/**
	 * Inicializa la pantalla de ingreso: carga los usuarios disponibles en el
	 * combo de selección y define cómo se muestra cada uno (nombre, email y perfil
	 * de conductor o cliente). Lo invoca JavaFX después de cargar el FXML.
	 */
	public void initialize() {
		// chequea que hayan datos, guarda los usuarios y los pobla en el comboBox
		if (ContextoAplicacion.getDatosIniciales() != null) {
			usuarios = ContextoAplicacion.getDatosIniciales().getUsuarios();

			for (Usuario usuario : usuarios.values()) {
				comboUsuarios.getItems().add(usuario);
			}
		}

		// combierte el toString original a uno que se adecua a lo que se quiere mostar
		// en el ComboBox
		comboUsuarios.setConverter(new StringConverter<Usuario>() {
			@Override
			public String toString(Usuario usuario) {
				if (usuario == null)
					return ""; // Verificamos si tiene perfil de conductor asociado
				String etiqueta = (usuario.getConductor() != null) ? " " + resources.getString("login.profile.driver")
						: " " + resources.getString("login.profile.client");
				return usuario.getNombre() + " (" + usuario.getEmail() + ")" + etiqueta;
			}

			@Override
			public Usuario fromString(String string) {
				return null;
			}
		});

	}

	/**
	 * metodo encargado de la logica de ingresar corre cuando se preciona el boton
	 * de "ingresar" en la pantalla
	 */
	@FXML
	void handleIngresar(ActionEvent event) throws IllegalStateException {
		Usuario usuarioSeleccionado = comboUsuarios.getValue();

		if (usuarioSeleccionado == null) {
			AlertaUtil.mostrarAlerta(resources.getString("login.alert.attention"),
					resources.getString("login.error.noUserSelected"));
			return;
		}

		if (radioConductor.isSelected()) {
			if (usuarioSeleccionado.getConductor() == null) {
				AlertaUtil.mostrarAlerta(resources.getString("login.alert.signInError"),
						resources.getString("login.error.notRegisteredDriver"));
				return;
			}

			// Intenta activar el rol de conductor solamente si todavía no está activo.
			try {
				if (usuarioSeleccionado.getRolActivo() != RolUsuario.CONDUCTOR) {
					usuarioSeleccionado.cambiarRolActivo(RolUsuario.CONDUCTOR);
				}

				ContextoAplicacion.setUsuarioLogeado(usuarioSeleccionado);

				// abre la otra pantalla
				NavegadorVistas.cambiarEscena(event, "/ui/javafx/view/PanelConductor.fxml");
			} catch (IllegalStateException e) {
				AlertaUtil.mostrarAlerta(resources.getString("login.error.unableToSignIn"), e.getMessage());
			}
		} else if (radioCliente.isSelected()) {

			if (usuarioSeleccionado.getRolActivo() != RolUsuario.CLIENTE
					&& gestorViajes.consultarViajeActivoConductor(usuarioSeleccionado) != null) {
				AlertaUtil.mostrarAlerta(resources.getString("login.alert.attention"),
						resources.getString("menu.error.cambio_rol"));
				return;
			}

			// Cambia el rol solamente si todavía no está activo.
			if (usuarioSeleccionado.getRolActivo() != RolUsuario.CLIENTE) {
				usuarioSeleccionado.cambiarRolActivo(RolUsuario.CLIENTE);
			}

			ContextoAplicacion.setUsuarioLogeado(usuarioSeleccionado);
			NavegadorVistas.cambiarEscena(event, "/ui/javafx/view/PanelCliente.fxml");
		}
	}

}
