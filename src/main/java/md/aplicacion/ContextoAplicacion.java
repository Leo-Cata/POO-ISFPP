package md.aplicacion;

import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.ResourceBundle;

import md.datos.dao.DAOException;
import md.datos.dao.DAOFactory;
import md.logica.GeneradorUbicacionesViales;
import md.logica.GestorViajes;
import md.logica.ValidacionUbicaciones;
import md.modelo.Usuario;
import md.rutas.api.PlanificadorRutas;
import md.rutas.impl.PlanificadorRutasFactory;
import md.config.CargarParametros;
import md.logica.InicializadorVehiculos;

/**
 * Mantiene disponibles los datos iniciales de la aplicación para que los
 * distintos componentes puedan acceder a ellos.
 */
public class ContextoAplicacion {
	private static CargadorModelo.DatosIniciales datos;
	private static Locale locale;
	private static ResourceBundle mensajes;
	private static Usuario usuarioLogeado; // mantiene el usuario logeado
	private static GestorViajes gestorViajes;
	private static PlanificadorRutas motorVial; // conserva la instancia del motor de rutas que creamos mediante
												// PlanificadorRutasFactory
	private static GeneradorUbicacionesViales generadorUbicaciones;
	private static ValidacionUbicaciones validadorUbicaciones;

	/**
	 * Carga los datos iniciales y prepara los recursos viales
	 * necesarios para iniciar la aplicación.
	 */

	public static void inicializar(DAOFactory persistencia)
			throws DAOException, FileNotFoundException {

		if (persistencia == null) {
			throw new IllegalArgumentException("La persistencia no puede ser null.");
		}

		if (motorVial != null) {
			throw new IllegalStateException("El contexto ya fue inicializado.");
		}

		CargadorModelo.DatosIniciales nuevosDatos = new CargadorModelo(persistencia).cargar();

		PlanificadorRutas nuevoMotor = null;
		ValidacionUbicaciones nuevoValidador;
		GeneradorUbicacionesViales nuevoGenerador;
		GestorViajes nuevoGestor;

		try {

			// Creamos el motor vial configurado.
			nuevoMotor = PlanificadorRutasFactory.crear(
					CargarParametros.getMotorRutas(),
					CargarParametros.getArchivoOsm(),
					CargarParametros.getDirectorioCacheRutas());

			// Creamos el validador con los parámetros geográficos configurados.
			nuevoValidador = new ValidacionUbicaciones(
					CargarParametros.getToleranciaAjusteMetros(),
					CargarParametros.getLatitudMin(),
					CargarParametros.getLatitudMax(),
					CargarParametros.getLongitudMin(),
					CargarParametros.getLongitudMax());

			// Creamos el generador de ubicaciones compartido,
			// utilizando el motor vial y el validador.
			nuevoGenerador = new GeneradorUbicacionesViales(
					nuevoMotor,
					nuevoValidador,
					CargarParametros.getLatitudMin(),
					CargarParametros.getLatitudMax(),
					CargarParametros.getLongitudMin(),
					CargarParametros.getLongitudMax());

			// Asignamos ubicaciones iniciales a los vehículos de todos los conductores.
			InicializadorVehiculos inicializadorVehiculos = new InicializadorVehiculos(nuevoGenerador);
			inicializadorVehiculos.inicializar(nuevosDatos.getUsuarios());

			// Creamos el gestor de viajes con los parámetros de configuración.
			nuevoGestor = new GestorViajes(
					nuevosDatos.getUsuarios(),
					nuevoMotor,
					CargarParametros.getVelocidadMediaKmH(),
					CargarParametros.getCostoCancelacionViajeAceptado());

		} catch (RuntimeException e) {

			// Si falla la inicialización, liberamos el motor creado.
			if (nuevoMotor != null) {
				try {
					nuevoMotor.close();
				} catch (RuntimeException errorCierre) {
					e.addSuppressed(errorCierre);
				}
			}

			throw e;
		}

		// Guardamos las instancias para que estén disponibles
		// desde los distintos componentes de la aplicación.
		datos = nuevosDatos;
		motorVial = nuevoMotor;
		validadorUbicaciones = nuevoValidador;
		generadorUbicaciones = nuevoGenerador;
		gestorViajes = nuevoGestor;
	}

	/**
	 * Establece el idioma de la aplicación y carga el paquete de mensajes
	 * {@code ui.i18n.labels} correspondiente.
	 *
	 * @param idioma código de idioma (por ejemplo, {@code "es"} o {@code "en"}).
	 * @param pais   código de país (por ejemplo, {@code "ES"} o {@code "US"}).
	 */
	public static void inicializarIdioma(String idioma, String pais) {

		locale = new Locale(idioma, pais);

		mensajes = ResourceBundle.getBundle(
				"ui.i18n.labels",
				locale);
	}

	/**
	 * Obtiene los datos iniciales cargados para la aplicación.
	 *
	 * @return los datos iniciales, o null si el contexto no fue inicializado.
	 */
	public static CargadorModelo.DatosIniciales getDatosIniciales() {
		return datos;
	}

	/**
	 * Obtiene el idioma actualmente configurado.
	 *
	 * @return el locale de la aplicación, o null si todavía no fue configurado.
	 */
	public static Locale getLocale() {
		return locale;
	}

	/**
	 * Obtiene el conjunto de mensajes correspondiente al idioma configurado.
	 *
	 * @return los mensajes de la aplicación, o null si todavía no fueron cargados.
	 */
	public static ResourceBundle getMensajes() {
		return mensajes;
	}

	/**
	 * Obtiene la latitud del centro inicial del mapa.
	 *
	 * @return latitud configurada.
	 */
	public static double getMapaCentroLatitud() {
		return CargarParametros.getMapaCentroLatitud();
	}

	/**
	 * Obtiene la longitud del centro inicial del mapa.
	 *
	 * @return longitud configurada.
	 */
	public static double getMapaCentroLongitud() {
		return CargarParametros.getMapaCentroLongitud();
	}

	/**
	 * Obtiene el nivel de zoom inicial del mapa.
	 *
	 * @return zoom configurado.
	 */
	public static int getMapaZoom() {
		return CargarParametros.getMapaZoom();
	}

	/**
	 * Obtiene el usuario que inició sesión.
	 * /**
	 * Obtiene el usuario que inició sesión.
	 *
	 * @return el usuario autenticado, o null si no hay ninguno.
	 */
	public static Usuario getUsuarioLogeado() {
		return usuarioLogeado;
	}

	/**
	 * Establece el usuario que inició sesión.
	 *
	 * @param usuario usuario autenticado, o null para cerrar la sesión.
	 */
	public static void setUsuarioLogeado(Usuario usuario) {
		usuarioLogeado = usuario;
	}

	/**
	 * Obtiene el motor de planificación de rutas compartido por la aplicación.
	 *
	 * @return el motor vial inicializado.
	 * @throws IllegalStateException si el motor todavía no fue inicializado.
	 */
	public static PlanificadorRutas getMotorVial() {

		if (motorVial == null) {
			throw new IllegalStateException("El motor vial todavía no fue inicializado.");
		}

		return motorVial;
	}

	/**
	 * Obtiene el gestor de viajes compartido por la aplicación.
	 *
	 * @return el gestor de viajes inicializado.
	 * @throws IllegalStateException si el gestor todavía no fue inicializado.
	 */
	public static GestorViajes getGestorViajes() {
		if (gestorViajes == null) {
			throw new IllegalStateException("El gestor de viajes todavía no fue inicializado.");
		}

		return gestorViajes;
	}

	/**
	 * Cierra el motor vial y libera sus recursos.
	 */
	public static void cerrar() {

		if (motorVial != null) {
			try {
				motorVial.close();
			} finally {
				motorVial = null;
				gestorViajes = null;
				generadorUbicaciones = null;
				datos = null;
				validadorUbicaciones = null;
			}
		}
	}

	/**
	 * Obtiene el generador de ubicaciones viales compartido.
	 *
	 * @return el generador inicializado.
	 * @throws IllegalStateException si el generador todavía no fue inicializado.
	 */
	public static GeneradorUbicacionesViales getGeneradorUbicaciones() {
		if (generadorUbicaciones == null) {
			throw new IllegalStateException(
					"El generador de ubicaciones todavía no fue inicializado.");
		}

		return generadorUbicaciones;
	}

	/**
	 * Obtiene el validador de ubicaciones compartido por la aplicación.
	 *
	 * @return el validador inicializado.
	 * @throws IllegalStateException si el validador todavía no fue inicializado.
	 */
	public static ValidacionUbicaciones getValidadorUbicaciones() {
		if (validadorUbicaciones == null) {
			throw new IllegalStateException("El validador de ubicaciones todavía no fue inicializado.");
		}

		return validadorUbicaciones;
	}
}
