package md.ui.impl.javafx.controller;

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
import md.logica.GestorViajes;
import md.modelo.EstadoViaje;
import md.modelo.RolUsuario;
import md.modelo.Servicio;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Viaje;
import md.ui.api.VisualizadorMapa;
import md.ui.impl.javafx.controller.util.AlertaUtil;
import md.ui.impl.javafx.map.VisualizadorRutasLeaflet;
import md.ui.impl.javafx.util.NavegadorVistas;
import javafx.application.Platform;
import md.logica.GestorCicloViajeCliente;

public class PanelClienteController {
	@FXML
	private WebView mapaView;
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
	@FXML
	private Button clienteBotonCancelacionPagar;
	@FXML
	private Label clienteLabelCancelacion;
	@FXML
	private Label clienteCostoPagar;

	private VisualizadorMapa visualizador;
	private boolean cancelacionBoolean = false;

	// Gestores encargados del procesamiento del viaje.
	private GestorViajes gestor;
	private GestorCicloViajeCliente cicloViajeCliente;
	private GestorViajes.Cotizacion cotizacion;

	// variables de ubicacion para cotizar/solicitar
	private Ubicacion origenActual;
	private Ubicacion destinoActual;

	// variables de servicio, cliente y viaje
	private Usuario cliente;
	private Servicio servicio;
	private Viaje viajeActual;

	// variable para que no deje solicitar un viaje si tiene una deuda
	private boolean deuda;

	/**
	 * Inicializa el panel del cliente: carga el mapa, registra la selección de
	 * puntos por clic, activa la espera del origen, carga los servicios en el combo
	 * y crea el {@link GestorViajes}. Lo invoca JavaFX después de cargar el FXML.
	 */
	@FXML
	public void initialize() {
		resource = ContextoAplicacion.getMensajes();

		// Instancia el adaptador pasando la referencia del WebView
		visualizador = new VisualizadorRutasLeaflet(mapaView);

		visualizador.configurarSeleccion(this::recibirPuntoMapa);

		// Activa la espera del primer clic (Origen)
		visualizador.activarSeleccion("origen");

		// setea todos los servicios en el comboBox
		comboServicios.getItems().setAll(ContextoAplicacion.getDatosIniciales().getServicios());

		comboServicios.setConverter(new StringConverter<Servicio>() {
			public String toString(Servicio servicio) {
				return servicio != null ? servicio.getNombre() : "";
			}

			public Servicio fromString(String texxt) {
				return null;
			}
		});

		// Invalida la cotización si el cliente modifica el servicio seleccionado.
		comboServicios.valueProperty().addListener((obs, servicioAnterior, servicioNuevo) -> {
			if (servicioAnterior != servicioNuevo) {
				invalidarCotizacion();
			}
		});

		// Obtiene el gestor de viajes compartido por la aplicación.
		gestor = ContextoAplicacion.getGestorViajes();

		// Crea el coordinador del ciclo temporal del viaje.
		cicloViajeCliente = new GestorCicloViajeCliente(gestor);

		// Recupera el usuario logueado y su posible viaje activo.
		cliente = ContextoAplicacion.getUsuarioLogeado();
		viajeActual = cliente != null ? gestor.consultarViajeActivo(cliente) : null;

		// al iniciar, se desactiva el boton de solicitar viaje
		botonClienteSolicitar.setDisable(true);

		// Muestra la información si el cliente tiene un viaje activo.
		if (viajeActual != null) {
			mostrarOcultarInfoViaje(true);
		} else {
			mostrarOcultarInfoViaje(false);
		}

		// oculta el textfield y el boton de confirmar cancelar al inicio
		clienteConfirmarCancelar.setVisible(false);
		clienteTextfieldCancelar.setVisible(false);

		// Oculta la interfaz de cancelación al iniciar
		clienteLabelCancelacion.setVisible(false);
		clienteCostoPagar.setVisible(false);
		clienteBotonCancelacionPagar.setVisible(false);

		// Recupera la deuda pendiente del cliente.
		deuda = cliente != null && gestor.tieneDeudaPendiente(cliente);

		if (deuda) {
			clienteLabelCancelacion.setVisible(true);
			clienteCostoPagar.setText(
					String.format("$%.2f", gestor.obtenerDeudaPendiente(cliente)));
			clienteCostoPagar.setVisible(true);
			clienteBotonCancelacionPagar.setVisible(true);
			clienteBotonCancelacionPagar.setDisable(false);
		}

	}

	private void recibirPuntoMapa(String tipo, Ubicacion ubicacionClick) {
		Ubicacion sobreCalle;
		try {
			sobreCalle = ContextoAplicacion.getValidadorUbicaciones()
					.validarYAjustar(ubicacionClick, ContextoAplicacion.getMotorVial());
		} catch (Exception e) {
			AlertaUtil.mostrarAlerta(resource.getString("client.alert.invalidLocation"), e.getMessage());
			visualizador.activarSeleccion(tipo);
			return;
		}
		// Invalida la cotización anterior al modificar el origen o destino desde el
		// mapa.
		invalidarCotizacion();

		if ("origen".equals(tipo)) {
			origenActual = sobreCalle;

			visualizador.mostrarMarcadorSeleccion("origen", sobreCalle);

			if (destinoActual == null) {
				visualizador.activarSeleccion("destino");
			} else {
				visualizador.cancelarSeleccion();
			}

		} else if ("destino".equals(tipo)) {
			destinoActual = sobreCalle;

			visualizador.mostrarMarcadorSeleccion("destino", sobreCalle);

			if (origenActual == null) {
				visualizador.activarSeleccion("origen");
			} else {
				visualizador.cancelarSeleccion();
			}
		}
	}

	/**
	 * Borra las coordenadas y el marcador del origen y espera un nuevo clic.
	 *
	 * @param event evento del botón.
	 */
	@FXML
	void handleBorrarOrigen(ActionEvent event) {
		borrarPunto("origen");
	}

	/**
	 * Borra las coordenadas y el marcador del destino y espera un nuevo clic.
	 *
	 * @param event evento del botón.
	 */
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
			origenActual = null;
		} else {
			destinoActual = null;
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

	/**
	 * Registra el viaje solicitado por el cliente.
	 * Comprueba que no existan deudas pendientes y que la solicitud
	 * haya sido cotizada. El registro se realiza en un hilo de fondo
	 * para no bloquear la interfaz.
	 *
	 * El viaje queda inicialmente en estado SOLICITADO.
	 *
	 * @param event evento del botón.
	 */
	@FXML
	void handleSolicitarViaje(ActionEvent event) {

		if (deuda) {
			AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),
					resource.getString("client.request.debt.text"));
			return;
		}

		// Verificamos que se haya cotizado el viaje.
		if (cotizacion == null || origenActual == null
				|| destinoActual == null || servicio == null) {

			botonClienteSolicitar.setDisable(true);

			AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),
					resource.getString("client.request.quote.required"));
			return;
		}

		// Capturamos los datos antes de iniciar el hilo de fondo.
		Usuario clienteActual = cliente;
		Servicio servicioActual = servicio;
		Ubicacion origen = origenActual;
		Ubicacion destino = destinoActual;

		botonClienteSolicitar.setDisable(true);

		// Registra la solicitud sin asignar todavía un conductor.
		Task<Viaje> tarea = new Task<>() {

			@Override
			protected Viaje call() throws Exception {
				return gestor.registrarSolicitudViaje(
						clienteActual, servicioActual, origen, destino);
			}
		};

		// Se ejecuta en el hilo de JavaFX.
		tarea.setOnSucceeded(e -> {

			viajeActual = tarea.getValue();

			if (viajeActual == null) {
				botonClienteSolicitar.setDisable(false);

				AlertaUtil.mostrarAlerta(
						resource.getString("login.alert.attention"),
						"No se pudo registrar la solicitud del viaje.");
				return;
			}

			// La cotización ya fue utilizada.
			cotizacion = null;

			// Mostramos inicialmente el estado SOLICITADO.
			mostrarOcultarInfoViaje(true);

			// Inicia el ciclo temporal del viaje.
			cicloViajeCliente.iniciar(
					viajeActual,
					viaje -> Platform.runLater(() -> {
						viajeActual = viaje;
						mostrarOcultarInfoViaje(true);

						// Si no se encontró conductor, permite realizar otra solicitud.
						if (viaje.estadoActual() == EstadoViaje.RECHAZADO) {
							botonClienteSolicitar.setDisable(false);
						}
					}),
					error -> Platform.runLater(() -> AlertaUtil.mostrarAlerta(
							resource.getString("login.alert.attention"),
							error.getMessage())));
		});

		// Se ejecuta si ocurrió un error al registrar la solicitud.
		tarea.setOnFailed(e -> {

			botonClienteSolicitar.setDisable(false);

			Throwable error = tarea.getException();

			AlertaUtil.mostrarAlerta(
					resource.getString("login.alert.attention"),
					error != null ? error.getMessage() : "Error al solicitar el viaje.");
		});

		Thread.ofVirtual().name("solicitud-viaje").start(tarea);
	}

	/**
	 * Cierra la sesión del usuario y vuelve a la pantalla de ingreso.
	 *
	 * @param event evento del botón.
	 */
	@FXML
	void handleCerrarSesion(ActionEvent event) {

		cicloViajeCliente.close();

		ContextoAplicacion.setUsuarioLogeado(null);

		NavegadorVistas.cambiarEscena(event, "/ui/javafx/view/login.fxml");
	}

	/**
	 * Calcula y muestra la cotización del viaje (distancia, duración y costo
	 * estimado) a partir de las coordenadas cargadas y del servicio elegido. Valida
	 * que los datos estén completos y que el usuario tenga el rol de cliente; si
	 * todo es correcto habilita el botón de solicitar.
	 *
	 * @param event evento del botón.
	 */
	@FXML
	void handleCotizarViaje(ActionEvent event) {
		// Descarta cualquier cotización anterior antes de intentar calcular una nueva.
		invalidarCotizacion();
		servicio = comboServicios.getValue();

		// Verificamos que estén completos todos los datos.
		if (origenActual == null || destinoActual == null
				|| servicio == null) {

			AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"),
					resource.getString("client.error.missingData"));

			return;
		}

		try {

			cliente = ContextoAplicacion.getUsuarioLogeado();

			if (cliente == null || cliente.getRolActivo() != RolUsuario.CLIENTE) {
				throw new IllegalStateException("No hay un usuario logueado con el rol CLIENTE.");
			}

			cotizacion = gestor.calcularCotizacion(servicio, origenActual, destinoActual);

			// muestra el detalle de la cotizacion
			String detalle = String.format("%s: %.2f %s%n" + "%s: %.2f %s%n" + "%s: $%.2f",
					resource.getString("viaje.cotizacion.distancia"), cotizacion.distanciaKm(),
					resource.getString("viaje.unidad.km"), resource.getString("viaje.cotizacion.duracion"),
					cotizacion.duracionMinutos(), resource.getString("viaje.unidad.min"),
					resource.getString("viaje.costo_estimado"), cotizacion.costoTotal());
			AlertaUtil.mostrarAlerta(resource.getString("viaje.cotizacion.titulo"), detalle);

			botonClienteSolicitar.setDisable(false);
		} catch (IllegalArgumentException | IllegalStateException e) {
			AlertaUtil.mostrarAlerta(resource.getString("login.alert.attention"), e.getMessage());
		}
	}

	/**
	 * Muestra u oculta el campo de motivo y el botón de confirmar cancelación
	 * (alterna cada vez que se presiona).
	 */
	@FXML
	void handleCancelarViaje() {
		cancelacionBoolean = !cancelacionBoolean;

		clienteTextfieldCancelar.setVisible(cancelacionBoolean);
		clienteConfirmarCancelar.setVisible(cancelacionBoolean);
	}

	/**
	 * Confirma la cancelación del viaje con el motivo ingresado. Si se pudo
	 * cancelar, muestra el costo de cancelación a pagar y deja al cliente con
	 * deuda, lo que le impide solicitar otro viaje hasta pagar. Si no se pudo,
	 * informa el problema.
	 */
	@FXML
	void handleConfirmarCancelar() {
		try {
			Viaje viaje = gestor.consultarViajeActivo(cliente);
			String motivo = clienteTextfieldCancelar.getText();

			if (viaje == null || motivo == null || motivo.isBlank()) {
				AlertaUtil.mostrarAlerta(
						resource.getString("client.unable.cancel.title"),
						resource.getString("client.unable.cancel.text"));
				return;
			}

			// Detiene las transiciones automáticas pendientes.
			cicloViajeCliente.cancelar();

			// Ejecuta la cancelación real del viaje.
			if (!gestor.procesarCancelacionViaje(cliente, motivo)) {
				AlertaUtil.mostrarAlerta(
						resource.getString("client.unable.cancel.title"),
						resource.getString("client.unable.cancel.text"));
				return;
			}

			// Se obtiene el costo antes de limpiar el estado.
			double costoCancelacion = gestor.obtenerCostoCancelacion(viaje);

			clienteViajeEstadoActual.setText(obtenerTextoEstado(viaje.estadoActual()));

			// Se muestra la información del importe pendiente
			clienteLabelCancelacion.setVisible(true);
			clienteCostoPagar.setText(String.format("$%.2f", costoCancelacion));
			clienteCostoPagar.setVisible(true);
			clienteBotonCancelacionPagar.setVisible(true);
			clienteBotonCancelacionPagar.setDisable(false);

			AlertaUtil.mostrarAlerta(resource.getString("client.cancel.success.title"),
					resource.getString("client.cancel.success.text") + String.format("%.2f", costoCancelacion));

			// setea con deuda
			deuda = true;

			// Se limpia el estado del viaje
			limpiarEstadoViaje();
			mostrarOcultarInfoViaje(false);

		} catch (IllegalArgumentException | IllegalStateException e) {
			AlertaUtil.mostrarAlerta(resource.getString("client.unable.cancel.title"),
					resource.getString("client.unable.cancel.text"));
		}

	}

	/**
	 * Registra el pago del costo de cancelación: oculta el importe y el botón de
	 * pago y levanta la deuda del cliente.
	 */
	@FXML
	void handleCancelarPagar() {
		gestor.registrarPagoCancelacion(cliente);

		clienteLabelCancelacion.setVisible(false);
		clienteCostoPagar.setText("");
		clienteCostoPagar.setVisible(false);
		clienteBotonCancelacionPagar.setVisible(false);
		deuda = false;
	}

	private void asignarConductorInfo() {

		if (viajeActual == null) {
			return;
		}

		// El estado siempre se muestra, independientemente de que haya conductor.
		clienteViajeEstadoActual.setText(
				obtenerTextoEstado(viajeActual.estadoActual()));

		Usuario conductor = viajeActual.getConductor();

		// Todavía no se asignó un conductor.
		if (conductor == null) {
			clienteConductorNom.setText("");
			clienteVehiculoModelo.setText("");
			clienteConductorPatente.setText("");

			clienetDriver.setVisible(false);
			clienteConductorNom.setVisible(false);
			clienteConductorVehiculoTitulo.setVisible(false);
			clienteVehiculoModelo.setVisible(false);
			clienteConductorPatente.setVisible(false);

			return;
		}

		clienteConductorNom.setText(conductor.getNombre());

		Vehiculo vehiculo = viajeActual.getVehiculo();

		if (vehiculo != null) {
			clienteVehiculoModelo.setText(vehiculo.getModelo());
			clienteConductorPatente.setText(vehiculo.getPatente());
		}

		clienetDriver.setVisible(true);
		clienteConductorNom.setVisible(true);
		clienteConductorVehiculoTitulo.setVisible(true);
		clienteVehiculoModelo.setVisible(true);
		clienteConductorPatente.setVisible(true);
	}

	// funcion auxiliar para mostrar/ocultar los labels de los datos del viaje
	private void mostrarOcultarInfoViaje(boolean mostrar) {

		if (!mostrar || viajeActual == null) {

			clienetDriver.setVisible(false);
			clienteConductorNom.setVisible(false);
			clienteConductorVehiculoTitulo.setVisible(false);
			clienteVehiculoModelo.setVisible(false);
			clienteConductorPatente.setVisible(false);

			clienteViajeEstadoActual.setVisible(false);
			clienteViajeEstadoTitulo.setVisible(false);
			clienteCancelarViaje.setVisible(false);

			return;
		}

		clienteViajeEstadoActual.setVisible(true);
		clienteViajeEstadoTitulo.setVisible(true);

		EstadoViaje estado = viajeActual.estadoActual();

		boolean puedeCancelar = estado == EstadoViaje.SOLICITADO
				|| estado == EstadoViaje.ACEPTADO
				|| estado == EstadoViaje.INICIADO;

		clienteCancelarViaje.setVisible(puedeCancelar);

		asignarConductorInfo();
	}

	// funcion auxiliar para limpiar todos los datos del mapa
	private void limpiarEstadoViaje() {
		borrarPunto("destino");
		borrarPunto("origen");
		visualizador.limpiarRutas();
		viajeActual = null;
		cancelacionBoolean = false;
		cotizacion = null;
		origenActual = null;
		destinoActual = null;
		clienteTextfieldCancelar.clear();
		clienteTextfieldCancelar.setVisible(false);
		clienteConfirmarCancelar.setVisible(false);
	}

	/**
	 * Obtiene la traducción del estado actual del viaje según el idioma
	 * seleccionado en la aplicación.
	 *
	 * @param estado estado del viaje que se desea mostrar.
	 * @return texto traducido del estado o un guion si el estado es null.
	 */
	private String obtenerTextoEstado(EstadoViaje estado) {

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

		return resource.getString(clave);
	}
}
