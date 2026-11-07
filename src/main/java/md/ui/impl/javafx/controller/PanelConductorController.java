package md.ui.impl.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import md.ui.api.VisualizadorMapa;
import md.ui.impl.javafx.map.VisualizadorRutasLeaflet;

public class PanelConductorController {
    @FXML
    private WebView mapaView;
    private VisualizadorMapa visualizador;

    @FXML
    public void initialize() {
        // Instancia el adaptador pasando la referencia del WebView
        visualizador = new VisualizadorRutasLeaflet(mapaView);
    }

}
