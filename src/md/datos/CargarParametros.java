package md.datos;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CargarParametros {
	private static final String ARCHIVO_CONFIG = "config.properties";
	private static String archivoUsuarios;
	private static String archivoServicios;
	private static String archivoVehiculos;
	private static double latitudMin;
	private static double latitudMax;
	private static double longitudMin;
	private static double longitudMax;

	/**
	 * Carga y procesa los parámetros de configuración de la aplicación desde el
	 * archivo config.properties, asignando las rutas de archivos de datos y
	 * estableciendo los límites geográficos válidos para las coordenadas del
	 * sistema.
	 *
	 * @throws IOException si el archivo config.properties no existe, no puede
	 *                     abrirse o ocurre un error durante su lectura.
	 */
	public static void parametros() throws IOException {
		Properties prop = new Properties();

		// abre el archivo config.properties dentro de un try que lo cierra
		// automáticamente al terminar las llaves.
		try (InputStream input = new FileInputStream(ARCHIVO_CONFIG)) {
			prop.load(input);
		}

		archivoUsuarios = leerClave(prop, "usuario");
		archivoServicios = leerClave(prop, "servicio");
		archivoVehiculos = leerClave(prop, "vehiculo");

		// convierte las coordenadas de texto a numero
		// variables declaradas unicamente dentro del metodo para que no se puedan usar
		// fuera.
		double latitud1 = Double.parseDouble(leerClave(prop, "latitud1"));
		double latitud2 = Double.parseDouble(leerClave(prop, "latitud2"));
		double longitud1 = Double.parseDouble(leerClave(prop, "longitud1"));
		double longitud2 = Double.parseDouble(leerClave(prop, "longitud2"));

		// asegura que el mínimo sea el menor y el máximo el mayor, sin importar en qué
		// orden estén en el config.
		latitudMin = Math.min(latitud1, latitud2);
		latitudMax = Math.max(latitud1, latitud2);
		longitudMin = Math.min(longitud1, longitud2);
		longitudMax = Math.max(longitud1, longitud2);
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
	public static String getArchivoUsuarios() {
		return archivoUsuarios;
	}

	public static String getArchivoServicios() {
		return archivoServicios;
	}

	public static String getArchivoVehiculos() {
		return archivoVehiculos;
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

}
