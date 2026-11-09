package md.ui.impl.javafx.controller;

import java.util.ResourceBundle; 

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.util.StringConverter;
import md.aplicacion.ContextoAplicacion;
import md.logica.ValidacionUbicaciones;
import md.modelo.RolUsuario;
import md.modelo.Servicio;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.rutas.api.RutaNoDisponibleException;
import md.ui.api.VisualizadorMapa;
import md.ui.impl.javafx.controller.util.AlertaUtil;
import md.ui.impl.javafx.map.VisualizadorRutasLeaflet;
import md.ui.impl.javafx.util.NavegadorVistas;
import md.logica.GestorViajes;

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
        resource = ContextoAplicacion.getMensajes();

        // Instancia el adaptador pasando la referencia del WebView
        visualizador = new VisualizadorRutasLeaflet(mapaView);

        visualizador.configurarSeleccion(this::recibirPuntoMapa);

        // 2\. Activa la espera del primer clic (Origen)
        visualizador.activarSeleccion("origen");
        // Reorganiza las teselas si cambia el ancho o alto del WebView
        mapaView.widthProperty().addListener((obs, oldV, newV) -> visualizador.refrescarTamano());
        mapaView.heightProperty().addListener((obs, oldV, newV) -> visualizador.refrescarTamano());

        // setea todos los servicios en el comboBox
        comboServicios.getItems().setAll(ContextoAplicacion.getDatosIniciales().getServicios());

        comboServicios.setConverter(new StringConverter<Servicio>() {
            @Override
            public String toString(Servicio servicio) {
                return servicio != null ? servicio.getNombre() : "";
            }

            @Override
            public Servicio fromString(String texxt) {
                return null;
            }
        });

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

    @FXML
    ComboBox<Servicio> comboServicios;

    @FXML
    void handleSolicitarViaje(ActionEvent event) {

        Servicio servicio = comboServicios.getValue();

        // Verificamos que estén completos todos los datos.
        if (txtLatOrigen.getText().isBlank()
                || txtLonOrigen.getText().isBlank()
                || txtLatDestino.getText().isBlank()
                || txtLonDestino.getText().isBlank()
                || servicio == null) {

            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),resource.getString("client.error.missingData"));

            return;
        }

        try {

            Ubicacion origen = new Ubicacion(Double.parseDouble(txtLatOrigen.getText()),Double.parseDouble(txtLonOrigen.getText()));

            Ubicacion destino = new Ubicacion(Double.parseDouble(txtLatDestino.getText()),Double.parseDouble(txtLonDestino.getText()));

            Usuario cliente = ContextoAplicacion.getUsuarioLogeado();

            if (cliente == null || cliente.getRolActivo() != RolUsuario.CLIENTE) {
                throw new IllegalStateException("No hay un usuario logueado con el rol CLIENTE.");
            }

            // Delegamos todo el procesamiento a la capa lógica.
            GestorViajes gestor = new GestorViajes();

            GestorViajes.ResultadoSolicitud resultado =gestor.procesarSolicitudViaje(cliente,servicio,origen,destino);

            // No se encontró ningún conductor compatible.
            if (resultado == null) {

                AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),resource.getString("client.error.noDriver"));

                return;
            }

            // El vehículo es el que realmente quedó asignado al viaje.
            Vehiculo vehiculo = resultado.viaje().getVehiculo();

            if (vehiculo == null || vehiculo.getUbicacion() == null) {
                throw new IllegalStateException("El viaje no tiene un vehículo con ubicación asignada.");
            }

            // Mostramos los recorridos reales calculados por GestorViajes.
            visualizador.mostrar(resultado.rutas(),vehiculo.getUbicacion(),origen,destino);

            GestorViajes.Cotizacion cotizacion = resultado.cotizacion();

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Solicitud procesada");
            alerta.setHeaderText("Conductor asignado: " + resultado.conductor().getNombre());

            alerta.setContentText(String.format("El viaje fue aceptado.%n%n" + "Distancia estimada: %.2f km%n"
                    + "Duración estimada: %.2f minutos%n"+ "Costo estimado: $%.2f",cotizacion.distanciaKm(),cotizacion.duracionMinutos(),
                    cotizacion.costoTotal()));

            alerta.showAndWait();

        } catch (NumberFormatException e) {

            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),"Las coordenadas ingresadas no son válidas.");

        } catch (RutaNoDisponibleException e) {

            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),resource.getString("client.error.noRoute"));

        } catch (IllegalArgumentException | IllegalStateException e) {

            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),e.getMessage());
        }
    }
    @FXML
    void handleCerrarSesion(ActionEvent event) {

        ContextoAplicacion.setUsuarioLogeado(null);

        NavegadorVistas.cambiarEscena(event,"/ui/javafx/view/login.fxml");
    }
}
