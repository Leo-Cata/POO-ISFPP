package md.ui.impl.javafx.controller;

import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    private ResourceBundle resource;
    @FXML
    private ComboBox<Servicio> comboServicios;
    @FXML
    private Button botonClienteCotizar;
    @FXML
    private Button botonClienteSolicitar;

    // gesto de viaje para delegar todo el procesamiento a la capa lógica.
    private GestorViajes gestor;
    private GestorViajes.Cotizacion cotizacion;
    private GestorViajes.ResultadoSolicitud resultadoSolicitud;

    // variables de ubicacion para cotizar/solicitar
    private Ubicacion origenActual;
    private Ubicacion destinoActual;

    // variables de servicio y cliente
    private Usuario cliente;
    private Servicio servicio;

    @FXML
    public void initialize() {
        resource = ContextoAplicacion.getMensajes();

        // Instancia el adaptador pasando la referencia del WebView
        visualizador = new VisualizadorRutasLeaflet(mapaView);

        visualizador.configurarSeleccion(this::recibirPuntoMapa);

        // 2\. Activa la espera del primer clic (Origen)
        visualizador.activarSeleccion("origen");

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

        // se genera un nuevo gestor
        gestor = new GestorViajes();

        // al iniciar, se desactiva el boton de solicitar viaje
        botonClienteSolicitar.setDisable(true);
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

        invalidarCotizacion();
    }

    // metodo auxiliar para no permitir cambiar de puntos una vez cotizado
    private void invalidarCotizacion() {
        cotizacion = null;
        if (botonClienteSolicitar != null) {
            botonClienteSolicitar.setDisable(true);
        }
    }

    @FXML
    void handleSolicitarViaje(ActionEvent event) {

        // chequea que se haya cotizado primero el viaje
        if (cotizacion == null || origenActual == null || destinoActual == null || servicio == null) {
            botonClienteSolicitar.setDisable(true);
            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),
                    "Debe cotizar el viaje antes de solicitarlo.");
            return;
        }
        try {

            resultadoSolicitud = gestor.procesarSolicitudViaje(cliente, servicio, origenActual,
                    destinoActual);

            // No se encontró ningún conductor compatible.
            if (resultadoSolicitud == null) {

                AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),
                        resource.getString("client.error.noDriver"));

                return;
            }

            // El vehículo es el que realmente quedó asignado al viaje.
            Vehiculo vehiculo = resultadoSolicitud.viaje().getVehiculo();

            if (vehiculo == null || vehiculo.getUbicacion() == null) {
                throw new IllegalStateException("El viaje no tiene un vehículo con ubicación asignada.");
            }

            // Mostramos los recorridos reales calculados por GestorViajes.
            visualizador.mostrar(resultadoSolicitud.rutas(), vehiculo.getUbicacion(), origenActual, destinoActual);

            botonClienteSolicitar.setDisable(true);

            AlertaUtil.mostrarAlerta("Solicitud procesada", "Conductor asignado: "
                    + resultadoSolicitud.conductor().getNombre() + "\\nViaje aceptado exitosamente.");
        } catch (NumberFormatException e) {

            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),
                    "Las coordenadas ingresadas no son válidas.");

        } catch (RutaNoDisponibleException e) {

            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),
                    resource.getString("client.error.noRoute"));

        } catch (IllegalArgumentException | IllegalStateException e) {

            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"), e.getMessage());
        }
    }

    @FXML
    void handleCerrarSesion(ActionEvent event) {

        ContextoAplicacion.setUsuarioLogeado(null);

        NavegadorVistas.cambiarEscena(event, "/ui/javafx/view/login.fxml");
    }

    @FXML
    void handleCotizarViaje(ActionEvent event) {
        servicio = comboServicios.getValue();

        // Verificamos que estén completos todos los datos.
        if (txtLatOrigen.getText().isBlank()
                || txtLonOrigen.getText().isBlank()
                || txtLatDestino.getText().isBlank()
                || txtLonDestino.getText().isBlank()
                || servicio == null) {

            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),
                    resource.getString("client.error.missingData"));

            return;
        }

        try {

            origenActual = new Ubicacion(Double.parseDouble(txtLatOrigen.getText()),
                    Double.parseDouble(txtLonOrigen.getText()));

            destinoActual = new Ubicacion(Double.parseDouble(txtLatDestino.getText()),
                    Double.parseDouble(txtLonDestino.getText()));

            cliente = ContextoAplicacion.getUsuarioLogeado();

            if (cliente == null || cliente.getRolActivo() != RolUsuario.CLIENTE) {
                throw new IllegalStateException("No hay un usuario logueado con el rol CLIENTE.");
            }

            cotizacion = gestor.calcularCotizacion(servicio, origenActual, destinoActual);

            // muestra el detalle de la cotizacion
            String detalle = String.format(
                    "%s: %.2f km%n" +
                            "%s: %.2f minutos%n" +
                            "%s: $%.2f",
                    resource.getString("viaje.cotizacion.distancia"),
                    cotizacion.distanciaKm(),
                    resource.getString("viaje.cotizacion.duracion"),
                    cotizacion.duracionMinutos(),
                    resource.getString("viaje.costo_estimado"),
                    cotizacion.costoTotal());

            AlertaUtil.mostrarAlerta(
                    resource.getString("viaje.cotizacion.titulo"),
                    detalle);

            botonClienteSolicitar.setDisable(false);
        } catch (NumberFormatException e) {
            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),
                    "Las coordenadas ingresadas no son válidas.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"), e.getMessage());
        }
    }
}
