package md.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

public class CargarParametros {

	private static final String ARCHIVO_CONFIG = "application.properties";

	// los nombres de las claves se escriben UNA sola vez.
	private static final String CLAVE_ORIGEN = "origen.datos";
	private static final String CLAVE_UI_TYPE = "app.ui.type";
	private static final String CLAVE_IDIOMA = "app.language";
	private static final String CLAVE_PAIS = "app.country";
	private static final String CLAVE_BD_CONFIG = "bd.config";
	private static final String CLAVE_USUARIOS = "usuario";
	private static final String CLAVE_SERVICIOS = "servicio";
	private static final String CLAVE_VEHICULOS = "vehiculo";
	private static final String CLAVE_LATITUD1 = "latitud1";
	private static final String CLAVE_LATITUD2 = "latitud2";
	private static final String CLAVE_LONGITUD1 = "longitud1";
	private static final String CLAVE_LONGITUD2 = "longitud2";
	private static final String CLAVE_TOLERANCIA_AJUSTE = "ubicacion.tolerancia.ajuste.metros";
	private static final String CLAVE_MOTOR_RUTAS = "rutas.motor";
	private static final String CLAVE_ARCHIVO_OSM = "rutas.osm";
	private static final String CLAVE_CACHE_RUTAS = "rutas.cache";
	private static final String CLAVE_VELOCIDAD_MEDIA ="viaje.velocidad.media.kmh";
	private static final String CLAVE_COSTO_CANCELACION_VIEJE_ACEPTADO ="costo.cancelacion.aceptado";

	// la tabla clave->valor queda guardada para que los getters la consulten
	// despues.
	private static final Properties properties = new Properties();

	private static String origenDatos;
	private static String appUiType;
	private static String appLanguage;
	private static String appCountry;
	private static double latitudMin;
	private static double latitudMax;
	private static double longitudMin;
	private static double longitudMax;
	private static double costoCancelacionViajeAceptado;
	private static double velocidadMediaKmH;
	private static double toleranciaAjusteMetros;

	/**
	 * Carga y procesa los parámetros de configuración de la aplicación desde el
	 * archivo application.properties, asignando las rutas de archivos de datos y
	 * estableciendo los límites geográficos válidos para las coordenadas del
	 * sistema.
	 *
	 * @throws IOException si el archivo application.properties no existe, no puede
	 *                     abrirse o ocurre un error durante su lectura.
	 */
	static {

		try (InputStream input = CargarParametros.class.getClassLoader().getResourceAsStream(ARCHIVO_CONFIG)) { // abre
																												// el
																												// archivo
																												// application.properties

			if (input != null) {
				// UTF-8 para que los acentos de los valores no se rompan.
				properties.load(new InputStreamReader(input, StandardCharsets.UTF_8)); // leer el archivo y copia cada
																						// clave-valor a la tabla
			}
		} catch (Exception e) {
			throw new ExceptionInInitializerError("Error al cargar la configuración: " + e.getMessage());
		}

		origenDatos = leerClave(properties, CLAVE_ORIGEN);
		appUiType = leerClave(properties, CLAVE_UI_TYPE);
		appLanguage = leerClave(properties, CLAVE_IDIOMA);
		appCountry = leerClave(properties, CLAVE_PAIS);

		// convierte las coordenadas de texto a numero
		// variables declaradas unicamente dentro del metodo para que no se puedan usar
		// fuera.
		double latitud1 = Double.parseDouble(leerClave(properties, CLAVE_LATITUD1));
		double latitud2 = Double.parseDouble(leerClave(properties, CLAVE_LATITUD2));
		double longitud1 = Double.parseDouble(leerClave(properties, CLAVE_LONGITUD1));
		double longitud2 = Double.parseDouble(leerClave(properties, CLAVE_LONGITUD2));

		// asegura que el mínimo sea el menor y el máximo el mayor, sin importar en qué
		// orden estén en el config.
		latitudMin = Math.min(latitud1, latitud2);
		latitudMax = Math.max(latitud1, latitud2);
		longitudMin = Math.min(longitud1, longitud2);
		longitudMax = Math.max(longitud1, longitud2);

		costoCancelacionViajeAceptado = Double.parseDouble(leerClave(properties, CLAVE_COSTO_CANCELACION_VIEJE_ACEPTADO));
		
		toleranciaAjusteMetros = Double.parseDouble(leerClave(properties, CLAVE_TOLERANCIA_AJUSTE));

		if (!Double.isFinite(toleranciaAjusteMetros) || toleranciaAjusteMetros <= 0) {
			throw new IllegalStateException("La tolerancia de ajuste debe ser un número positivo.");
		}
		
		velocidadMediaKmH = Double.parseDouble(
		        leerClave(properties, CLAVE_VELOCIDAD_MEDIA));

		if (!Double.isFinite(velocidadMediaKmH) || velocidadMediaKmH <= 0) {
		    throw new IllegalStateException(
		            "La velocidad media debe ser un número positivo.");
		}
	}

	/**
	 * Lee una clave del archivo de configuración y verifica que exista.
	 *
	 * @param prop  propiedades ya cargadas desde el archivo
	 * @param clave nombre de la clave a buscar
	 * @return el valor asociado a la clave
	 * @throws IllegalStateException si la clave no existe en el archivo
	 */
	private static String leerClave(Properties prop, String clave) {
		String valor = prop.getProperty(clave);
		if (valor == null || valor.trim().isEmpty()) {
			throw new IllegalStateException(
					"La clave '" + clave + "' no existe o está mal escrita en " + ARCHIVO_CONFIG);
		}
		return valor;
	}

	// getters
	public static String getUiType() {
		return appUiType.trim().toUpperCase(Locale.ROOT);
	}

	public static String getOrigenDatos() {
		return origenDatos.trim().toUpperCase(Locale.ROOT);
	}

	public static String getLanguage() {
		return appLanguage.trim().toUpperCase(Locale.ROOT);
	}

	public static String getCountry() {
		return appCountry.trim().toUpperCase(Locale.ROOT);
	}

	public static String getArchivoBdConfig() {
		return leerClave(properties, CLAVE_BD_CONFIG);
	}

	public static String getArchivoUsuarios() {
		return leerClave(properties, CLAVE_USUARIOS);
	}

	public static String getArchivoServicios() {
		return leerClave(properties, CLAVE_SERVICIOS);
	}

	public static String getArchivoVehiculos() {
		return leerClave(properties, CLAVE_VEHICULOS);
	}

	public static double getLatitudMin() {
		return latitudMin;
	}

	public static double getLatitudMax() {
		return latitudMax;
	}

	public static double getLongitudMin() {
		return longitudMin;
	}

	public static double getLongitudMax() {
		return longitudMax;
	}

	public static double getToleranciaAjusteMetros() {
		return toleranciaAjusteMetros;
	}

	public static String getMotorRutas() {
		return leerClave(properties, CLAVE_MOTOR_RUTAS).trim().toUpperCase(Locale.ROOT);
	}

	public static String getArchivoOsm() {
		return leerClave(properties, CLAVE_ARCHIVO_OSM).trim();
	}

	public static String getDirectorioCacheRutas() {
		return leerClave(properties, CLAVE_CACHE_RUTAS).trim();
	}
	
	public static double getVelocidadMediaKmH() {
	    return velocidadMediaKmH;
	}
	
	public static double getCostoCancelacionViajeAceptado() {
	    return costoCancelacionViajeAceptado;
	}
}