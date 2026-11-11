package md.ui.impl.javafx.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import javafx.scene.control.ComboBox;
import md.aplicacion.ContextoAplicacion;
import md.modelo.Conductor;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.ui.impl.javafx.util.NavegadorVistas;

public class PanelConductorController {
	
	// Datos personales
	@FXML
	private Label labelNombreConductor;

	@FXML
	private Label labelEmailConductor;

	@FXML
	private Label labelTelefonoConductor;

	@FXML
	private Label labelLicenciaConductor;


	// Vehículos
	@FXML
	private ComboBox<Vehiculo> comboVehiculos;

	@FXML
	private Label labelModeloVehiculo;

	@FXML
	private Label labelPatenteVehiculo;

	@FXML
	private Label labelCategoriaVehiculo;


	// Conductor que inició sesión
	private Conductor conductorActual;
	
	@FXML
	private void initialize() {

	    Usuario usuario = ContextoAplicacion.getUsuarioLogeado();

	    if (usuario == null || usuario.getConductor() == null) {
	        throw new IllegalStateException(
	                "No hay un conductor asociado al usuario logueado."
	        );
	    }

	    conductorActual = usuario.getConductor();

	    cargarDatosPersonales(usuario, conductorActual);

	    cargarVehiculos(conductorActual);
	}
	
    @FXML
    void handleCerrarSesion(ActionEvent event) {

        ContextoAplicacion.setUsuarioLogeado(null);

        NavegadorVistas.cambiarEscena(event, "/ui/javafx/view/login.fxml");
    }
    
    @FXML
    private void handleIniciarViaje(ActionEvent event) {

    }
    
    @FXML
    private void handleCalificarCliente(ActionEvent event) {

    }
    
    @FXML
    private void handleFinalizarViaje(ActionEvent event) {

    }
    
    private void cargarDatosPersonales(Usuario usuario, Conductor conductor) {

        labelNombreConductor.setText(usuario.getNombre());

        labelEmailConductor.setText(usuario.getEmail());

        labelTelefonoConductor.setText(usuario.getTelefono());

        labelLicenciaConductor.setText(conductor.getLicenciaConducir());
    }
    
    private void cargarVehiculos(Conductor conductor) {

        comboVehiculos.getItems().clear();

		for (Vehiculo vehiculo : conductor.getVehiculos().values()) {
		    comboVehiculos.getItems().add(vehiculo);
		}

        comboVehiculos.setConverter(new StringConverter<Vehiculo>() {

            public String toString(Vehiculo vehiculo) {

                if (vehiculo == null) {
                    return "";
                }

                return vehiculo.getModelo() + " - " + vehiculo.getPatente();
            }

            public Vehiculo fromString(String texto) {
                return null;
            }
        });

        Vehiculo vehiculoActivo = conductor.getVehiculoActivo();

        if (vehiculoActivo != null) {

            comboVehiculos.getSelectionModel().select(vehiculoActivo);

            mostrarDatosVehiculo(vehiculoActivo);
        }

        comboVehiculos.valueProperty().addListener(
                (observable, anterior, seleccionado) -> {

                    if (seleccionado != null) {

                        conductor.setVehiculoActivo(seleccionado);

                        conductor.setCategoriaVehiculoActivo(
                                seleccionado.getCategoriaVehiculo()
                        );

                        mostrarDatosVehiculo(seleccionado);
                    }
                }
        );
    }
    
    private void mostrarDatosVehiculo(Vehiculo vehiculo) {

        if (vehiculo == null) {
            labelModeloVehiculo.setText("—");
            labelPatenteVehiculo.setText("—");
            labelCategoriaVehiculo.setText("—");
            return;
        }

        labelModeloVehiculo.setText(vehiculo.getModelo());

        labelPatenteVehiculo.setText(vehiculo.getPatente());

        labelCategoriaVehiculo.setText(
                vehiculo.getCategoriaVehiculo().toString()
        );
    }
}
