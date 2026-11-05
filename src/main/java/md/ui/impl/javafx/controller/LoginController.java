package md.ui.impl.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import md.modelo.Usuario;

public class LoginController {

    @FXML
    private ComboBox<Usuario> comboUsuarios;
    @FXML
    private ToggleGroup tipoUsuario;
    @FXML
    private RadioButton radioConductor, radioCliente;

    public void initialize() {
        Usuario usuario1 = new Usuario("Juan Pérez", "1122334455", "juan@mail.com");
        Usuario usuario2 = new Usuario("María Gómez", "1155667788", "maria@mail.com");

        comboUsuarios.getItems().addAll(usuario1, usuario2);

    }
}
