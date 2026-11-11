package md.ui.impl.javafx.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import md.aplicacion.ContextoAplicacion;
import md.ui.impl.javafx.util.NavegadorVistas;

public class PanelConductorController {
	
	
    @FXML
    void handleCerrarSesion(ActionEvent event) {

        ContextoAplicacion.setUsuarioLogeado(null);

        NavegadorVistas.cambiarEscena(event, "/ui/javafx/view/login.fxml");
    }
}
