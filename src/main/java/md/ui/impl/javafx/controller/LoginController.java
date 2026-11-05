package md.ui.impl.javafx.controller;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.util.StringConverter;
import md.modelo.Usuario;

public class LoginController {

    @FXML
    private ComboBox<Usuario> comboUsuarios;
    @FXML
    private ToggleGroup tipoUsuario;
    @FXML
    private RadioButton radioConductor, radioCliente;

    private List<Usuario> usuarios = new ArrayList<Usuario>();

    public void initialize() {
        Usuario usuario1 = new Usuario("Juan Pérez", "1122334455", "juan@mail.com");
        Usuario usuario2 = new Usuario("María Gómez", "1155667788", "maria@mail.com");
        usuarios.add(usuario1);
        usuarios.add(usuario2);

        comboUsuarios.getItems().addAll(usuarios);

        // combierte el toString original a uno que se adecua a lo que se quiere mostar
        // en el ComboBox
        comboUsuarios.setConverter(new StringConverter<Usuario>() {
            @Override
            public String toString(Usuario usuario) {
                return usuario != null ? usuario.getNombre() + " " + usuario.getEmail() : "";
            }

            @Override
            public Usuario fromString(String string) {
                return null;
            }
        });
    }
}
