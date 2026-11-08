package md.ui.impl.javafx.controller;

import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.util.StringConverter;
import md.aplicacion.ContextoAplicacion;
import md.logica.Movilidad;
import md.logica.PlanificacionViajeService;
import md.logica.ValidacionUbicaciones;
import md.modelo.CategoriaVehiculo;
import md.modelo.Servicio;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.rutas.api.RutaNoDisponibleException;
import md.rutas.api.RutasViaje;
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
        // obtiene el servicio
        Servicio servicio = comboServicios.getValue();

        // chequea que las coords esten bien
        if (txtLatOrigen.getText().isBlank() || txtLatDestino.getText().isBlank() || servicio == null) {
            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),
                    resource.getString("client.error.missingData"));
            return;
        }

        // genera ubicaciones con estas coords
        Ubicacion origen = new Ubicacion(Double.parseDouble(txtLatOrigen.getText()),
                Double.parseDouble(txtLonOrigen.getText()));
        Ubicacion destino = new Ubicacion(Double.parseDouble(txtLatDestino.getText()),
                Double.parseDouble(txtLonDestino.getText()));

        // crea una nueva movilidad para despues obtener el conductor
        Movilidad movilidad = new Movilidad(ContextoAplicacion.getDatosIniciales().getUsuarios());

        // System.out.println("Servicio: " + servicio.getNombre() + " | " +
        // servicio.getTipoVehiculo()
        // + " | " + servicio.getCategoriaVehiculo() + " | " +
        // servicio.getTipoServicio());

        // for (Usuario u :
        // ContextoAplicacion.getDatosIniciales().getUsuarios().values()) {
        // if (u.getConductor() != null) {
        // Vehiculo v = u.getConductor().getVehiculoActivo();
        // System.out.println(u.getEmail() + " rol=" + u.getRolActivo()
        // + " estado=" + u.getConductor().getEstadoConductor()
        // + " ubic=" + v.getUbicacion()
        // + " | " + v.getTipoVehiculo() + " | " + v.getCategoriaVehiculo()
        // + " | " + v.getTipoServicios());
        // }
        // }

        // Usuario conductor = movilidad.seleccionarConductor(servicio, origen);
        // // chequea que se haya asignado conductor
        // if (conductor == null) {
        // AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),
        // resource.getString("client.error.noDriver"));
        // return;
        // }

        // obtiene el vehiculo del conductor
        // Vehiculo vehiculo = conductor.getConductor().getVehiculoActivo();

        // VEHICULO HARDCODEADO PARA EL TEST
        Vehiculo vehiculo = ContextoAplicacion.getDatosIniciales().getVehiculos().get("AB456DE");
        vehiculo.setUbicacion(new Ubicacion(-42.7767, -65.0267));
        try {
            RutasViaje rutas = new PlanificacionViajeService(ContextoAplicacion.getMotorVial())
                    .planificar(vehiculo, origen, destino);
            visualizador.mostrar(rutas, vehiculo.getUbicacion(), origen, destino);
        } catch (RutaNoDisponibleException e) {
            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),
                    resource.getString("client.error.noRoute"));
        }
    }
}
