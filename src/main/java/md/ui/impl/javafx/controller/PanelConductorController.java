package md.ui.impl.javafx.controller;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.util.StringConverter;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import md.aplicacion.ContextoAplicacion;
import md.logica.GestorViajes;
import md.modelo.CalificacionViaje;
import md.modelo.Conductor;
import md.modelo.EstadoConductor;
import md.modelo.EstadoViaje;
import md.modelo.RegistroViaje;
import md.modelo.RolUsuario;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Viaje;
import md.ui.impl.javafx.controller.util.AlertaUtil;
import md.ui.impl.javafx.util.NavegadorVistas;
import md.modelo.TipoServicio;
import java.util.stream.Collectors;

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

	@FXML
	private Label labelEstadoConductor;

	// Vehículos
	@FXML
	private ComboBox<Vehiculo> comboVehiculos;

	@FXML
	private Label labelModeloVehiculo;

	@FXML
	private Label labelPatenteVehiculo;

	@FXML
	private Label labelCategoriaVehiculo;

	@FXML
	private Label labelServiciosVehiculo;

	@FXML
	private Button botonCambiarDisponibilidad;

	@FXML
	private Button botonIniciarViaje;

	@FXML
	private Button botonFinalizarViaje;

	@FXML
	private Button botonCancelarViaje;

	@FXML
	private Button botonCalificarCliente;

	// Datos del viaje actual
	@FXML
	private Label labelClienteViaje;

	@FXML
	private Label labelOrigenViaje;

	@FXML
	private Label labelDestinoViaje;

	@FXML
	private Label labelDistanciaViaje;

	@FXML
	private Label labelDuracionViaje;

	@FXML
	private Label labelCostoViaje;

	@FXML
	private Label labelHoraInicioViaje;

	@FXML
	private Label labelEstadoViaje;

	@FXML
	private Node contenedorViajeActual;

	@FXML
	private Label labelSinViaje;

	// Conductor que inició sesión
	private Conductor conductorActual;

	private final GestorViajes gestorViajes = ContextoAplicacion.getGestorViajes();

	@FXML
	private void initialize() {

		Usuario usuario = ContextoAplicacion.getUsuarioLogeado();

		if (usuario == null || usuario.getConductor() == null) {
			throw new IllegalStateException("No hay un conductor asociado al usuario logueado.");
		}

		conductorActual = usuario.getConductor();

		cargarDatosPersonales(usuario, conductorActual);

		cargarVehiculos(conductorActual);

		actualizarEstadoBotones();

		actualizarEstadoConductor();

		actualizarPanelViaje();
	}

	/**
	 * Cierra la sesión del conductor y vuelve a la pantalla de ingreso.
	 *
	 * @param event evento del botón.
	 */
	@FXML
	void handleCerrarSesion(ActionEvent event) {

		ContextoAplicacion.setUsuarioLogeado(null);

		NavegadorVistas.cambiarEscena(event, "/ui/javafx/view/login.fxml");
	}

	@FXML
	private void handleCambiarRolCliente(ActionEvent event) {

		ResourceBundle msj = ContextoAplicacion.getMensajes();

		Usuario usuario = ContextoAplicacion.getUsuarioLogeado();

		if (gestorViajes.consultarViajeActivoConductor(usuario) != null) {
			AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING,
					msj.getString("menu.error.cambio_rol"),
					msj.getString("menu.error.cambio_rol"));
			return;
		}

		try {
			usuario.cambiarRolActivo(RolUsuario.CLIENTE);

			ContextoAplicacion.setUsuarioLogeado(usuario);

			NavegadorVistas.cambiarEscena(event, "/ui/javafx/view/PanelCliente.fxml");

		} catch (IllegalStateException e) {

			AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, msj.getString("menu.error.cambio_rol"),
					msj.getString("menu.error.cambio_rol"));
		}
	}

	@FXML
	private void handleCambiarDisponibilidad(ActionEvent event) {

		ResourceBundle msj = ContextoAplicacion.getMensajes();

		EstadoConductor estado = conductorActual.getEstadoConductor();

		if (estado == EstadoConductor.DISPONIBLE) {

			conductorActual.setEstado(EstadoConductor.FUERA_DE_SERVICIO);

		} else if (estado == EstadoConductor.FUERA_DE_SERVICIO) {

			conductorActual.setEstado(EstadoConductor.DISPONIBLE);

		} else {

			AlertaUtil.mostrarAlerta(msj.getString("login.alert.attention"),
					msj.getString("menu.conductor.panel.estado.error.cambio_no_permitido"));
			return;
		}

		actualizarEstadoConductor();
		actualizarEstadoBotones();
	}

	@FXML
	private void handleIniciarViaje(ActionEvent event) {

		ResourceBundle msj = ContextoAplicacion.getMensajes();
		Usuario usuario = ContextoAplicacion.getUsuarioLogeado();

		try {

			boolean exito = gestorViajes.procesarInicioViaje(usuario);

			if (exito) {

				AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, msj.getString("viaje.titulo.iniciar"),
						msj.getString("viaje.inicio.exitoso"));

			} else {

				AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, msj.getString("login.alert.attention"),
						msj.getString("viaje.error.sin_viaje_aceptado"));
			}

			// Actualizar la información del conductor y los botones.
			actualizarEstadoConductor();
			actualizarEstadoBotones();
			actualizarPanelViaje();

		} catch (Exception e) {

			AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, msj.getString("login.alert.attention"), e.getMessage());
		}
	}

	@FXML
	private void handleCalificarCliente(ActionEvent event) {

		ResourceBundle msj = ContextoAplicacion.getMensajes();
		Usuario usuario = ContextoAplicacion.getUsuarioLogeado();

		if (usuario == null || usuario.getConductor() == null) {
			AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, msj.getString("login.alert.attention"),
					msj.getString("viaje.error.sin_viaje_iniciado"));
			return;
		}

		try {
			// Consultar el viaje actual del conductor.
			Viaje viaje = gestorViajes.consultarViajeActivoConductor(usuario);
			if (viaje == null || viaje.estadoActual() != EstadoViaje.INICIADO) {
				AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, msj.getString("login.alert.attention"),
						msj.getString("viaje.error.sin_viaje_iniciado"));
				return;
			}

			// Comprobar que el conductor todavía no haya calificado.
			if (viaje.getCalificacionConductor() != CalificacionViaje.NO_CALIFICADO) {
				AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, msj.getString("calificacion.titulo"),
						msj.getString("calificacion.error.ya_calificado"));
				return;
			}

			// Crear las opciones del cuadro de diálogo.
			ButtonType botonMalo = new ButtonType(msj.getString("calificacion.opcion.malo"),
					ButtonBar.ButtonData.OK_DONE);

			ButtonType botonRegular = new ButtonType(msj.getString("calificacion.opcion.regular"),
					ButtonBar.ButtonData.OK_DONE);

			ButtonType botonBueno = new ButtonType(msj.getString("calificacion.opcion.bueno"),
					ButtonBar.ButtonData.OK_DONE);

			ButtonType botonMuyBueno = new ButtonType(msj.getString("calificacion.opcion.muy_bueno"),
					ButtonBar.ButtonData.OK_DONE);

			ButtonType botonExcelente = new ButtonType(msj.getString("calificacion.opcion.excelente"),
					ButtonBar.ButtonData.OK_DONE);

			ButtonType botonNoCalificar = new ButtonType(msj.getString("calificacion.opcion.no_calificar"),
					ButtonBar.ButtonData.CANCEL_CLOSE);

			// Mostrar el cuadro encima del panel actual.
			Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION);

			dialogo.setTitle(msj.getString("calificacion.titulo"));
			dialogo.setHeaderText(null);
			dialogo.setContentText(msj.getString("calificacion.seleccione"));

			ButtonType[] opciones = { botonMalo, botonRegular, botonBueno, botonMuyBueno, botonExcelente,
					botonNoCalificar };

			dialogo.getButtonTypes().setAll(opciones);

			// Evitar que Enter seleccione accidentalmente una calificación.
			for (ButtonType opcion : opciones) {
				Button boton = (Button) dialogo.getDialogPane().lookupButton(opcion);
				boton.setDefaultButton(false);
			}

			// Mostrar el diálogo y obtener la elección.
			Optional<ButtonType> resultado = dialogo.showAndWait();

			// Si se cerró con la X, no se registra ninguna calificación.
			if (resultado.isEmpty()) {
				return;
			}

			ButtonType seleccion = resultado.get();

			// El conductor decidió no calificar.
			if (seleccion == botonNoCalificar) {
				AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, msj.getString("calificacion.titulo"),
						msj.getString("calificacion.mensaje.omitida"));
				return;
			}

			// Convertir el botón seleccionado al valor del modelo.
			CalificacionViaje calificacion;

			if (seleccion == botonMalo) {
				calificacion = CalificacionViaje.MALO;

			} else if (seleccion == botonRegular) {
				calificacion = CalificacionViaje.REGULAR;

			} else if (seleccion == botonBueno) {
				calificacion = CalificacionViaje.BUENO;

			} else if (seleccion == botonMuyBueno) {
				calificacion = CalificacionViaje.MUY_BUENO;

			} else if (seleccion == botonExcelente) {
				calificacion = CalificacionViaje.EXCELENTE;

			} else {
				return;
			}

			// Registrar la calificación utilizando la lógica existente.
			gestorViajes.calificarComoConductor(usuario, viaje, calificacion);
			AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, msj.getString("calificacion.titulo"),
					msj.getString("calificacion.mensaje.registrada"));

		} catch (IllegalArgumentException | IllegalStateException e) {

			AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, msj.getString("login.alert.attention"),
					msj.getString("calificacion.error.registro"));
		}
	}

	@FXML
	private void handleCancelarViaje(ActionEvent event) {
		ResourceBundle msj = ContextoAplicacion.getMensajes();
		Usuario usuario = ContextoAplicacion.getUsuarioLogeado();

		TextInputDialog dialog = new TextInputDialog();

		dialog.setTitle(msj.getString("driver.cancel.trip"));

		dialog.setHeaderText(null);

		dialog.setContentText(msj.getString("driver.cancel.placeholder"));

		Optional<String> resultado = dialog.showAndWait();

		// Si el usuario cierra la ventana o presiona Cancelar.
		if (resultado.isEmpty()) {
			return;
		}

		String motivo = resultado.get().trim();

		// Validar que el motivo no esté vacío.
		if (motivo.isEmpty()) {
			AlertaUtil.mostrarAlerta(
					Alert.AlertType.WARNING,
					msj.getString("driver.cancel.reason.required.title"),
					msj.getString("driver.cancel.reason.required"));
			return;
		}

		boolean exito;

		try {
			exito = gestorViajes.procesarCancelacionViaje(usuario, motivo);

		} catch (Exception e) {
			AlertaUtil.mostrarAlerta(
					Alert.AlertType.ERROR,
					msj.getString("driver.cancel.error.title"),
					msj.getString("driver.cancel.error.text"));
			return;
		}

		if (!exito) {
			AlertaUtil.mostrarAlerta(
					Alert.AlertType.WARNING,
					msj.getString("driver.unable.cancel.title"),
					msj.getString("driver.unable.cancel.text"));
			return;
		}

		// Actualizar la interfaz después de cancelar correctamente.
		actualizarEstadoConductor();
		actualizarEstadoBotones();
		actualizarPanelViaje();

		AlertaUtil.mostrarAlerta(
				Alert.AlertType.INFORMATION,
				msj.getString("driver.cancel.success.title"),
				msj.getString("driver.cancel.success.text"));
	}

	@FXML
	private void handleFinalizarViaje(ActionEvent event) {

		ResourceBundle msj = ContextoAplicacion.getMensajes();
		Usuario usuario = ContextoAplicacion.getUsuarioLogeado();

		try {

			boolean exito = gestorViajes.procesarFinalizacionViaje(usuario);

			if (exito) {

				AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, msj.getString("viaje.titulo.finalizar"),
						msj.getString("viaje.fin.exitoso"));

			} else {

				AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, msj.getString("login.alert.attention"),
						msj.getString("viaje.error.sin_viaje_iniciado"));
			}

			// El conductor debería volver a estar disponible.
			actualizarEstadoConductor();
			actualizarEstadoBotones();
			actualizarPanelViaje();

		} catch (Exception e) {

			AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, msj.getString("login.alert.attention"),
					msj.getString("viaje.error.finalizacion"));
		}
	}

	private void cargarDatosPersonales(Usuario usuario, Conductor conductor) {

		labelNombreConductor.setText(usuario.getNombre());

		labelEmailConductor.setText(usuario.getEmail());

		labelTelefonoConductor.setText(usuario.getTelefono());

		labelLicenciaConductor.setText(conductor.getLicenciaConducir());
	}

	private void cargarVehiculos(Conductor conductor) {

		comboVehiculos.getItems().clear();

		for (Vehiculo vehiculo : conductor.getVehiculos()) {
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

		comboVehiculos.valueProperty().addListener((observable, anterior, seleccionado) -> {

			if (seleccionado != null) {

				conductor.setVehiculoActivo(seleccionado);

				conductor.setCategoriaVehiculoActivo(seleccionado.getCategoriaVehiculo());

				mostrarDatosVehiculo(seleccionado);
			}
		});
	}

	private void mostrarDatosVehiculo(Vehiculo vehiculo) {

		if (vehiculo == null) {
			labelModeloVehiculo.setText("—");
			labelPatenteVehiculo.setText("—");
			labelCategoriaVehiculo.setText("—");
			labelServiciosVehiculo.setText("—");
			return;
		}

		labelModeloVehiculo.setText(vehiculo.getModelo());

		labelPatenteVehiculo.setText(vehiculo.getPatente());

		labelCategoriaVehiculo.setText(vehiculo.getCategoriaVehiculo().toString());

		mostrarServicios(vehiculo);
	}

	private void actualizarEstadoConductor() {

		ResourceBundle msj = ContextoAplicacion.getMensajes();

		switch (conductorActual.getEstadoConductor()) {

			case DISPONIBLE:
				labelEstadoConductor.setText(msj.getString("menu.conductor.panel.estado.disponible"));

				botonCambiarDisponibilidad
						.setText(msj.getString("menu.conductor.panel.estado.accion.pasar_fuera_servicio"));

				botonCambiarDisponibilidad.setDisable(false);
				break;

			case FUERA_DE_SERVICIO:
				labelEstadoConductor.setText(msj.getString("menu.conductor.panel.estado.fuera_de_servicio"));

				botonCambiarDisponibilidad
						.setText(msj.getString("menu.conductor.panel.estado.accion.volver_disponible"));

				botonCambiarDisponibilidad.setDisable(false);
				break;

			case VIAJE_A_ORIGEN:
				labelEstadoConductor.setText(msj.getString("menu.conductor.panel.estado.viaje_a_origen"));

				botonCambiarDisponibilidad.setDisable(true);
				break;

			case VIAJE_A_DESTINO:
				labelEstadoConductor.setText(msj.getString("menu.conductor.panel.estado.viaje_a_destino"));

				botonCambiarDisponibilidad.setDisable(true);
				break;
		}
	}

	private void actualizarEstadoBotones() {

		Usuario usuario = ContextoAplicacion.getUsuarioLogeado();

		Viaje viaje = gestorViajes.consultarViajeActivoConductor(usuario);

		boolean viajeAceptado = viaje != null && viaje.estadoActual() == EstadoViaje.ACEPTADO;

		boolean viajeIniciado = viaje != null && viaje.estadoActual() == EstadoViaje.INICIADO;

		boolean viajeCancelable = viajeAceptado || viajeIniciado;

		boolean conductorYaCalifico = viaje != null
				&& viaje.getCalificacionConductor() != CalificacionViaje.NO_CALIFICADO;

		botonIniciarViaje.setDisable(!viajeAceptado);

		botonFinalizarViaje.setDisable(!viajeIniciado);

		botonCancelarViaje.setDisable(!viajeCancelable);

		botonCalificarCliente.setDisable(!viajeIniciado || conductorYaCalifico);
	}

	private void actualizarPanelViaje() {

		Usuario usuario = ContextoAplicacion.getUsuarioLogeado();

		Viaje viaje = gestorViajes.consultarViajeActivoConductor(usuario);

		boolean tieneViaje = viaje != null;

		contenedorViajeActual.setVisible(tieneViaje);
		contenedorViajeActual.setManaged(tieneViaje);

		labelSinViaje.setVisible(!tieneViaje);
		labelSinViaje.setManaged(!tieneViaje);

		if (tieneViaje) {
			cargarDatosViaje(viaje);
		}
	}

	private void cargarDatosViaje(Viaje viaje) {

		Locale locale = ContextoAplicacion.getLocale();
		// Datos de la cotización asociada al viaje.
		GestorViajes.Cotizacion cotizacion = gestorViajes.obtenerCotizacion(viaje);
		// Datos del cliente.
		labelClienteViaje.setText(viaje.getCliente().getNombre());

		// Origen y destino.
		labelOrigenViaje.setText(formatearUbicacion(viaje.getOrigen(), locale));

		labelDestinoViaje.setText(formatearUbicacion(viaje.getDestino(), locale));

		// Distancia.
		labelDistanciaViaje.setText(formatearNumero(cotizacion.distanciaKm(), 2, locale) + " "
				+ ContextoAplicacion.getMensajes().getString("viaje.unidad.km"));

		// Duración estimada.
		labelDuracionViaje.setText(formatearNumero(Math.ceil(cotizacion.duracionMinutos()), 0, locale) + " "
				+ ContextoAplicacion.getMensajes().getString("viaje.unidad.min"));

		// Costo total.
		labelCostoViaje.setText("$ " + formatearNumero(cotizacion.costoTotal(), 2, locale));
		// Hora de inicio.
		labelHoraInicioViaje.setText(obtenerHoraInicio(viaje, locale));

		// Estado actual del viaje.
		labelEstadoViaje.setText(obtenerTextoEstadoViaje(viaje.estadoActual()));
	}

	private String formatearUbicacion(Ubicacion ubicacion, Locale locale) {

		if (ubicacion == null) {
			return "—";
		}

		return String.format(locale, "%.5f / %.5f", ubicacion.getLatitud(), ubicacion.getLongitud());
	}

	private String formatearNumero(double valor, int decimales, Locale locale) {

		NumberFormat formato = NumberFormat.getNumberInstance(locale);

		formato.setMinimumFractionDigits(decimales);
		formato.setMaximumFractionDigits(decimales);

		return formato.format(valor);
	}

	private String obtenerHoraInicio(Viaje viaje, Locale locale) {

		DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm", locale);

		for (RegistroViaje registro : viaje.getRegistroViaje()) {

			if (registro.getEstadoViaje() == EstadoViaje.INICIADO) {

				return registro.getFechaHora().format(formato);
			}
		}

		// El viaje fue aceptado, pero todavía no comenzó.
		return "—";
	}

	private String obtenerClaveServicio(TipoServicio tipoServicio) {
		return switch (tipoServicio) {
			case PASAJEROS -> "servicio.pasajeros";
			case ENVIOS -> "servicio.envios";
		};
	}

	private void mostrarServicios(Vehiculo vehiculo) {
		String servicios = vehiculo.getTipoServicios().stream()
				.map(tipo -> ContextoAplicacion.getMensajes().getString(obtenerClaveServicio(tipo)))
				.collect(Collectors.joining(" · "));

		labelServiciosVehiculo.setText(servicios.isEmpty() ? "—" : servicios);
	}

	private String obtenerTextoEstadoViaje(EstadoViaje estado) {

		if (estado == null) {
			return "—";
		}

		String clave = switch (estado) {
			case SOLICITADO -> "viaje.estado.solicitado";
			case ACEPTADO -> "viaje.estado.aceptado";
			case INICIADO -> "viaje.estado.iniciado";
			case FINALIZADO -> "viaje.estado.finalizado";
			case CANCELADO -> "viaje.estado.cancelado";
			case RECHAZADO -> "viaje.estado.rechazado";
		};

		return ContextoAplicacion.getMensajes().getString(clave);
	}
}
