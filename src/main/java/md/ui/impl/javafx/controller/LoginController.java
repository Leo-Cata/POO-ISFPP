package md.ui.impl.javafx.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.util.StringConverter;
import md.aplicacion.ContextoAplicacion;
import md.modelo.RolUsuario;
import md.modelo.Usuario;
import md.ui.impl.javafx.util.NavegadorVistas;
import net.datastructures.ProbeHashMap;

public class LoginController {

    @FXML
    private ComboBox<Usuario> comboUsuarios;
    @FXML
    private ToggleGroup tipoUsuario;
    @FXML
    private RadioButton radioConductor, radioCliente;

    private ProbeHashMap<String, Usuario> usuarios = new ProbeHashMap<>();

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
                String etiqueta = (usuario.getConductor() != null) ? " [Conductor]" : "[Cliente]";
                return usuario.getNombre() + " (" + usuario.getEmail() + ")" + etiqueta;
            }

            @Override
            public Usuario fromString(String string) {
                return null;
            }
        });

    }

    /**
     * metodo encargado de la logica de ingresar
     * corre cuando se preciona el boton de "ingresar" en la pantalla
     */
    @FXML
    void handleIngresar(ActionEvent event) throws IllegalStateException {
        Usuario usuarioSeleccionado = comboUsuarios.getValue();

        if (usuarioSeleccionado == null) {
            mostrarAlerta("Atencion", "No se selecciono un usuario");
            return;
        }

        if (radioConductor.isSelected()) {
            if (usuarioSeleccionado.getConductor() == null) {
                mostrarAlerta("Error Inicio de sesion", "El usuario seleccionado no esta dado de alta como conductor");
                return;
            }

            // intenta setearlo como conductor activo y guardar el usuario seleccionado
            try {
                usuarioSeleccionado.cambiarRolActivo(RolUsuario.CONDUCTOR);
                ContextoAplicacion.setUsuarioLogeado(usuarioSeleccionado);

                // abre la otra pantalla
                NavegadorVistas.cambiarEscena(event, "null"); // TODO: poner la ruta del controlador
            } catch (IllegalStateException e) {
                mostrarAlerta("No es posible ingresar", e.getMessage());
            }
        } else if (radioCliente.isSelected()) {
            usuarioSeleccionado.cambiarRolActivo(RolUsuario.CLIENTE);

            ContextoAplicacion.setUsuarioLogeado(usuarioSeleccionado);
            NavegadorVistas.cambiarEscena(event, "null"); // TODO: poner la ruta del controlador
        }
    }

    // Método auxiliar para mostrar alertas de JavaFX
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
