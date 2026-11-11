package md.ui.impl.javafx.controller;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
    private Button botonClienteSolicitar;
    @FXML
    private Label clienetDriver;
    @FXML
    private Label clienteConductorNom;
    @FXML
    private Label clienteConductorVehiculoTitulo;
    @FXML
    private Label clienteVehiculoModelo;
    @FXML
    private Label clienteConductorPatente;
    @FXML
    private Label clienteViajeEstadoActual;
    @FXML
    private Label clienteViajeEstadoTitulo;

    @FXML
    private Button clienteCancelarViaje;
    @FXML
    private TextField clienteTextfieldCancelar;
    @FXML
    private Button clienteConfirmarCancelar;

    private VisualizadorMapa visualizador;
    private boolean cancelacionBoolean = false;

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

        // al iniciar, se ocultaran los labels de la info del conductor
        mostrarOcultarInfoViaje(false);

        // oculta el textfield y el boton de confirmar cancelar al inicio
        clienteConfirmarCancelar.setVisible(false);
        clienteTextfieldCancelar.setVisible(false);
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
                    resource.getString("client.request.quote.required"));
            return;
        }
        // Valores capturados en el hilo de JavaFX: el hilo de fondo no lee campos de la
        // UI
        Usuario clienteActual = cliente;
        Servicio servicioActual = servicio;
        Ubicacion origen = origenActual;
        Ubicacion destino = destinoActual;

        // evita pedir dos veces el mismo viaje mientras se calcula
        botonClienteSolicitar.setDisable(true);

        // El cálculo de rutas (GraphHopper) corre en un hilo de fondo para no congelar
        // la ventana
        Task<GestorViajes.ResultadoSolicitud> tarea = new Task<>() {
            @Override
            protected GestorViajes.ResultadoSolicitud call() throws Exception {
                return gestor.procesarSolicitudViaje(clienteActual, servicioActual, origen, destino);
            }
        };

        // Se ejecuta en el hilo de JavaFX: acá sí se puede tocar la interfaz
        tarea.setOnSucceeded(e -> {
            resultadoSolicitud = tarea.getValue();

            // No se encontró ningún conductor compatible.
            if (resultadoSolicitud == null) {
                AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),
                        resource.getString("client.error.noDriver"));
                botonClienteSolicitar.setDisable(false);
                return;
            }

            // El vehículo es el que realmente quedó asignado al viaje.
            Vehiculo vehiculo = resultadoSolicitud.viaje().getVehiculo();
            if (vehiculo == null || vehiculo.getUbicacion() == null) {
                botonClienteSolicitar.setDisable(false);
                AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),
                        resource.getString("client.request.vehicle.location.missing"));
                return;
            }

            // Mostramos los recorridos reales calculados por GestorViajes.
            visualizador.mostrar(resultadoSolicitud.rutas(), vehiculo.getUbicacion(), origen, destino);

            AlertaUtil.mostrarAlerta(resource.getString("client.request.success.title"),
                    MessageFormat.format(
                            resource.getString("client.request.success.message"),
                            resultadoSolicitud.conductor().getNombre()));
            asignarConductorInfo();
            mostrarOcultarInfoViaje(true);
        });

        // Se ejecuta en el hilo de JavaFX si el cálculo lanzó una excepción
        tarea.setOnFailed(e -> {
            botonClienteSolicitar.setDisable(false);
            Throwable error = tarea.getException();
            String mensaje = (error instanceof RutaNoDisponibleException)
                    ? resource.getString("client.error.noRoute")
                    : error.getMessage();
            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"), mensaje);
        });

        Thread.ofVirtual().name("solicitud-viaje").start(tarea);
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
                    "%s: %.2f %s%n" +
                            "%s: %.2f %s%n" +
                            "%s: $%.2f",
                    resource.getString("viaje.cotizacion.distancia"),
                    cotizacion.distanciaKm(),
                    resource.getString("viaje.unidad.km"),
                    resource.getString("viaje.cotizacion.duracion"),
                    cotizacion.duracionMinutos(),
                    resource.getString("viaje.unidad.min"),
                    resource.getString("viaje.costo_estimado"),
                    cotizacion.costoTotal());
            AlertaUtil.mostrarAlerta(
                    resource.getString("viaje.cotizacion.titulo"),
                    detalle);

            botonClienteSolicitar.setDisable(false);
        } catch (NumberFormatException e) {
            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),
                    resource.getString("client.invalid.coords"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"), e.getMessage());
        }
    }

    // se encarga de mostrar el textfield y el boton de confirmar, al volver a
    // apretar oculta
    @FXML
    void handleCancelarViaje() {
        cancelacionBoolean = !cancelacionBoolean;

        clienteTextfieldCancelar.setVisible(cancelacionBoolean);
        clienteConfirmarCancelar.setVisible(cancelacionBoolean);
    }

    // boton encargado de llamar al metodo para la cancelacion
    @FXML
    void handleConfirmarCancelar() {
        try {
            // si devuelve false es que hubo un error
            if (!gestor.procesarCancelacionViaje(cliente, clienteTextfieldCancelar.getText())) {
                AlertaUtil.mostrarAlerta(resource.getString("client.unable.cancel.title"),
                        resource.getString("client.unable.cancel.text"));
                return;
            }
            // si se pudo cancelar:
            clienteViajeEstadoActual.setText(resultadoSolicitud.viaje().estadoActual().name());
            AlertaUtil.mostrarAlerta(resource.getString("client.cancel.success.title"),
                    resource.getString("client.cancel.success.text"));

            // por ultimo limpia el mapa
            limpiarEstadoViaje();
            mostrarOcultarInfoViaje(false);
        } catch (IllegalArgumentException | IllegalStateException e) {
            AlertaUtil.mostrarAlerta(resource.getString("client.unable.cancel.title"), e.getLocalizedMessage());
        }
    }

    // funcion auxiliar para mostrar los datos del conductor en pantalla
    private void asignarConductorInfo() {
        clienteConductorNom.setText(resultadoSolicitud.conductor().getNombre());
        clienteVehiculoModelo.setText(resultadoSolicitud.conductor().getConductor().getVehiculoActivo().getModelo());
        clienteConductorPatente.setText(resultadoSolicitud.conductor().getConductor().getVehiculoActivo().getPatente());
        clienteViajeEstadoActual.setText(resultadoSolicitud.viaje().estadoActual().name());
    }

    // funcion auxiliar para mostrar/ocultar los labels de los datos del viaje
    private void mostrarOcultarInfoViaje(boolean mostrar) {
        clienetDriver.setVisible(mostrar);
        clienteConductorNom.setVisible(mostrar);
        clienteConductorVehiculoTitulo.setVisible(mostrar);
        clienteVehiculoModelo.setVisible(mostrar);
        clienteConductorPatente.setVisible(mostrar);
        clienteViajeEstadoActual.setVisible(mostrar);
        clienteViajeEstadoTitulo.setVisible(mostrar);
        clienteCancelarViaje.setVisible(mostrar);
    }

    // funcion auxiliar para limpiar todos los datos del mapa
    private void limpiarEstadoViaje() {
        borrarPunto("origen");
        borrarPunto("destino");
        visualizador.limpiarRutas();
        resultadoSolicitud = null;
        cancelacionBoolean = false;
        cotizacion = null;
        origenActual = null;
        destinoActual = null;
        clienteTextfieldCancelar.clear();
        clienteTextfieldCancelar.setVisible(false);
        clienteConfirmarCancelar.setVisible(false);
    }
}
