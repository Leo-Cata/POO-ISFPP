package md.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

public class CargarParametros {

	private static final String ARCHIVO_BASE = "config.properties";
	// los nombres de las claves se escriben UNA sola vez.
	private static final String CLAVE_CIUDAD = "ciudad";
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
	private static final String CLAVE_VELOCIDAD_MEDIA = "viaje.velocidad.media.kmh";
	private static final String CLAVE_ESPERA_ACEPTACION_SEGUNDOS = "viaje.ciclo.espera.aceptacion.segundos";
	private static final String CLAVE_MILISEGUNDOS_REALES_POR_MINUTO = "viaje.ciclo.milisegundos.reales.por.minuto";
	private static final String CLAVE_COSTO_CANCELACION_VIEJE_ACEPTADO = "costo.cancelacion.aceptado";
	private static final String CLAVE_MAPA_CENTRO_LAT = "mapa.centro.latitud";
	private static final String CLAVE_MAPA_CENTRO_LON = "mapa.centro.longitud";
	private static final String CLAVE_MAPA_ZOOM = "mapa.zoom";

	// la tabla clave->valor queda guardada para que los getters la consulten
	// despues.
	private static final Properties properties = new Properties();

	private static final String origenDatos;
	private static final String appUiType;
	private static final String appLanguage;
	private static final String appCountry;
	private static final double latitudMin;
	private static final double latitudMax;
	private static final double longitudMin;
	private static final double longitudMax;
	private static final double costoCancelacionViajeAceptado;
	private static final double velocidadMediaKmH;
	private static final long esperaAceptacionSegundos;
	private static final long milisegundosRealesPorMinuto;
	private static final double toleranciaAjusteMetros;

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

		Properties general = cargarArchivo(ARCHIVO_BASE);
		String ciudad = leerClave(general, CLAVE_CIUDAD).trim().toLowerCase(Locale.ROOT);

		properties.putAll(general);
		properties.putAll(cargarArchivo("application_" + ciudad + ".properties"));
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

		costoCancelacionViajeAceptado = Double
				.parseDouble(leerClave(properties, CLAVE_COSTO_CANCELACION_VIEJE_ACEPTADO));

		toleranciaAjusteMetros = Double.parseDouble(leerClave(properties, CLAVE_TOLERANCIA_AJUSTE));

		if (!Double.isFinite(toleranciaAjusteMetros) || toleranciaAjusteMetros <= 0) {
			throw new IllegalStateException("La tolerancia de ajuste debe ser un número positivo.");
		}

		velocidadMediaKmH = Double.parseDouble(leerClave(properties, CLAVE_VELOCIDAD_MEDIA));

		if (!Double.isFinite(velocidadMediaKmH) || velocidadMediaKmH <= 0) {
			throw new IllegalStateException("La velocidad media debe ser un número positivo.");
		}
		
		esperaAceptacionSegundos =
		        leerEntero(CLAVE_ESPERA_ACEPTACION_SEGUNDOS);

		milisegundosRealesPorMinuto =
		        leerEntero(CLAVE_MILISEGUNDOS_REALES_POR_MINUTO);
	}

	/**
	 * Lee un archivo de configuración desde el classpath.
	 *
	 * @param nombre nombre del archivo, por ejemplo application_trelew.properties
	 * @return las propiedades del archivo
	 * @throws IllegalStateException si el archivo no existe
	 */
	private static Properties cargarArchivo(String nombre) {
		try (InputStream input = CargarParametros.class.getClassLoader().getResourceAsStream(nombre)) {
			if (input == null) {
				throw new IllegalStateException("No se encontró el archivo de configuración '" + nombre
						+ "'. Revisá el valor de 'ciudad' en " + ARCHIVO_BASE + ".");
			}
			Properties propiedades = new Properties();
			// UTF-8 para que los acentos de los valores no se rompan.
			propiedades.load(new InputStreamReader(input, StandardCharsets.UTF_8));
			return propiedades;
		} catch (IOException e) {
			throw new ExceptionInInitializerError("Error al leer " + nombre + ": " + e.getMessage());
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
			throw new IllegalStateException("La clave '" + clave + "' no existe o está mal escrita en " + ARCHIVO_BASE
					+ " o en el archivo de la ciudad.");
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

	/** Latitud del centro con que se abre el mapa. */
	public static double getMapaCentroLatitud() {
		double lat = leerDecimal(CLAVE_MAPA_CENTRO_LAT);
		if (!Double.isFinite(lat) || lat < -90 || lat > 90) {
			throw new IllegalStateException("La clave '" + CLAVE_MAPA_CENTRO_LAT + "' debe estar entre -90 y 90.");
		}
		return lat;
	}

	/** Longitud del centro con que se abre el mapa. */
	public static double getMapaCentroLongitud() {
		double lon = leerDecimal(CLAVE_MAPA_CENTRO_LON);
		if (!Double.isFinite(lon) || lon < -180 || lon > 180) {
			throw new IllegalStateException("La clave '" + CLAVE_MAPA_CENTRO_LON + "' debe estar entre -180 y 180.");
		}
		return lon;
	}

	/** Zoom inicial del mapa, de 1 (todo el planeta) a 19 (calle). */
	public static int getMapaZoom() {
		long zoom = leerEntero(CLAVE_MAPA_ZOOM);
		if (zoom > 19) {
			throw new IllegalStateException("La clave '" + CLAVE_MAPA_ZOOM + "' debe ser como máximo 19.");
		}
		return (int) zoom;
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
	
	/**
	 * Obtiene el tiempo de espera antes de intentar aceptar automáticamente
	 * un viaje.
	 *
	 * @return tiempo de espera expresado en segundos.
	 */
	public static long getEsperaAceptacionSegundos() {
	    return esperaAceptacionSegundos;
	}

	/**
	 * Obtiene la equivalencia en milisegundos reales de un minuto estimado
	 * de duración del viaje.
	 *
	 * @return milisegundos reales correspondientes a un minuto estimado.
	 */
	public static long getMilisegundosRealesPorMinuto() {
	    return milisegundosRealesPorMinuto;
	}

	public static double getCostoCancelacionViajeAceptado() {
		return costoCancelacionViajeAceptado;
	}

	private static double leerDecimal(String clave) {
		try {
			return Double.parseDouble(leerClave(properties, clave).trim());
		} catch (NumberFormatException e) {
			throw new IllegalStateException("La clave '" + clave + "' debe ser un número.");
		}
	}

	private static long leerEntero(String clave) {
		String valor = leerClave(properties, clave).trim();
		try {
			long numero = Long.parseLong(valor);
			if (numero <= 0) {
				throw new IllegalStateException("La clave '" + clave + "' debe ser mayor que cero.");
			}
			return numero;
		} catch (NumberFormatException e) {
			throw new IllegalStateException("La clave '" + clave + "' debe ser un número entero.");
		}
	}
}