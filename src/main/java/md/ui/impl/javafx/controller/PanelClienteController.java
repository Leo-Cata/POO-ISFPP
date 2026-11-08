package md.ui.impl.javafx.controller;

import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import md.aplicacion.ContextoAplicacion;
import md.logica.ValidacionUbicaciones;
import md.modelo.Ubicacion;
import md.ui.api.VisualizadorMapa;
import md.ui.impl.javafx.controller.util.AlertaUtil;
import md.ui.impl.javafx.map.VisualizadorRutasLeaflet;

public class PanelClienteController {
    @FXML
    private WebView mapaView;
    private VisualizadorMapa visualizador;
    @FXML
    private TextField txtLatOrigen;
    @FXML
    private TextField txtLonOrigen;
    @FXML
    private TextField txtLatDestino;
    @FXML
    private TextField txtLonDestino;

    @FXML
    public void initialize() {
        // Instancia el adaptador pasando la referencia del WebView
        visualizador = new VisualizadorRutasLeaflet(mapaView);

        visualizador.configurarSeleccion(this::recibirPuntoMapa);

        // 2\. Activa la espera del primer clic (Origen)
        visualizador.activarSeleccion("origen");
        // Reorganiza las teselas si cambia el ancho o alto del WebView
        mapaView.widthProperty().addListener((obs, oldV, newV) -> visualizador.refrescarTamano());
        mapaView.heightProperty().addListener((obs, oldV, newV) -> visualizador.refrescarTamano());
    }

    private void recibirPuntoMapa(String tipo, Ubicacion ubicacionClick) {
        Ubicacion sobreCalle;
        try {
            sobreCalle = ValidacionUbicaciones.validarYAjustar(ubicacionClick, ContextoAplicacion.getMotorVial());

        } catch (Exception e) {
            AlertaUtil.mostrarAlerta(resource.getString("client.alert.invalidLocation"), e.getMessage());
            visualizador.activarSeleccion(tipo);
            return;
        }
        if ("origen".equals(tipo)) {
            txtLatOrigen.setText(String.valueOf(sobreCalle.getLatitud()));
            txtLonOrigen.setText(String.valueOf(sobreCalle.getLongitud()));
            visualizador.mostrarMarcadorSeleccion("origen", sobreCalle);

            if (txtLatDestino.getText().isBlank()) {
                visualizador.activarSeleccion("destino");
            } else {
                visualizador.cancelarSeleccion();
            }
        } else if ("destino".equals(tipo)) {
            txtLatDestino.setText(String.valueOf(sobreCalle.getLatitud()));
            txtLonDestino.setText(String.valueOf(sobreCalle.getLongitud()));
            visualizador.mostrarMarcadorSeleccion("destino", sobreCalle);

            if (txtLatOrigen.getText().isBlank()) {
                visualizador.activarSeleccion("origen");
            } else {
                visualizador.cancelarSeleccion();
            }
        }
    }

    @FXML
    void handleBorrarOrigen(ActionEvent event) {
        borrarPunto("origen");
    }

    @FXML
    void handleBorrarDestino(ActionEvent event) {
        borrarPunto("destino");
    }

    /**
     * Borra los campos y el marcador del punto indicado y espera un nuevo clic de
     * ese tipo.
     */
    private void borrarPunto(String tipo) {
        if ("origen".equals(tipo)) {
            txtLatOrigen.clear();
            txtLonOrigen.clear();
        } else {
            txtLatDestino.clear();
            txtLonDestino.clear();
        }
        visualizador.limpiarMarcador(tipo);
        visualizador.activarSeleccion(tipo);
    }

    @FXML
    private ResourceBundle resource;

}
