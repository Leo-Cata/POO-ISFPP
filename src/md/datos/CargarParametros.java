package md.datos;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Clase Singleton encargada de cargar y centralizar los parámetros de
 * configuración de la aplicación desde el archivo {@code config.properties},
 * incluyendo las rutas de archivos de datos y los límites geográficos del sistema.
 */
public class CargarParametros {
	// atributo para guardar la unica instancia creada
	private static CargarParametros instancia;

	private static final String ARCHIVO_CONFIG = "config.properties";
	private String archivoUsuarios;
	private String archivoServicios;
	private String archivoVehiculos;
	private double latitudMin;
	private double latitudMax;
	private double longitudMin;
	private double longitudMax;

	/**
	 * Constructor privado que inicializa la instancia cargando la configuración
	 * desde el archivo de parámetros correspondiente.
	 */
	private CargarParametros() {
		leerArchivoConfig();
	}

	/**
	 * Carga y procesa los parámetros de configuración de la aplicación desde el
	 * archivo {@code config.properties}, asignando las rutas de archivos de datos y
	 * estableciendo los límites geográficos válidos para las coordenadas del sistema.
	 *
	 * @throws IllegalStateException si el archivo no existe, no se pudo leer o no se pudo abrir.
	 */
	private void leerArchivoConfig() {
		Properties prop = new Properties();

		// abre el archivo config.properties dentro de un try que lo cierra
		// automáticamente al terminar las llaves.
		try (InputStream input = new FileInputStream(ARCHIVO_CONFIG)) {
			prop.load(input);
		} catch (IOException e) {
			throw new IllegalStateException("El archivo " + ARCHIVO_CONFIG + " no existe, no se pudo abrir u ocurrió un error durante la lectura.", e);
		}

		archivoUsuarios = leerClave(prop, "usuario");
		archivoServicios = leerClave(prop, "servicio");
		archivoVehiculos = leerClave(prop, "vehiculo");

		// convierte las coordenadas de texto a numero
		// variables declaradas unicamente dentro del metodo para que no se puedan usar
		// fuera.
		double latitud1 = parsearClave(prop, "latitud1");
		double latitud2 = parsearClave(prop, "latitud2");
		double longitud1 = parsearClave(prop, "longitud1");
		double longitud2 = parsearClave(prop, "longitud2");

		// asegura que el mínimo sea el menor y el máximo el mayor, sin importar en qué
		// orden estén en el config.
		latitudMin = Math.min(latitud1, latitud2);
		latitudMax = Math.max(latitud1, latitud2);
		longitudMin = Math.min(longitud1, longitud2);
		longitudMax = Math.max(longitud1, longitud2);
	}

    /**
     * Devuelve la única instancia de la clase, creándola si aún no existe.
     * El método es sincronizado para garantizar la seguridad en entornos multihilo.
     *
     * @return la única instancia de {@code CargarParametros}
     * 
     * //posible mejora: validaciones minimas (que latitud1 != latitud2, no sea posible pasar NaN/Infinity, etc.)
     */
	public static synchronized CargarParametros getInstance() { 
		if (instancia == null) {
			instancia = new CargarParametros();
		}
		return instancia;
	}

    /**
     * Lee una clave del archivo de configuración y verifica que exista y no esté vacía.
     *
     * @param prop  propiedades ya cargadas desde el archivo
     * @param clave nombre de la clave a buscar
     * @return el valor asociado a la clave, sin espacios en los extremos
     * @throws IllegalStateException si la clave no existe o está vacía en el archivo de configuración
     */
	private static String leerClave(Properties prop, String clave) {
		String valor = prop.getProperty(clave);
		if (valor == null || valor.trim().isEmpty()) {
			throw new IllegalStateException(
					"La clave '" + clave + "' no existe o está mal escrita en " + ARCHIVO_CONFIG);
		}
		return valor.trim();
	}


    /**
     * Lee una clave del archivo de configuración y la convierte a tipo {@code double}.
     *
     * @param prop  propiedades ya cargadas desde el archivo
     * @param clave nombre de la clave a buscar y parsear
     * @return el valor numérico de la clave en formato {@code double}
     * @throws IllegalStateException si el valor de la clave no puede convertirse a un número
     */
	private static double parsearClave(Properties prop, String clave) {
		String texto = leerClave(prop, clave);
		double numero;
		try {
			numero = Double.parseDouble(texto);
		} catch (NumberFormatException e) {
			throw new IllegalStateException("La clave " + clave + "tiene valor " + texto + " pero se espera que contenga unicamente números", e);
		}

		return numero;
	}

	// getters
	public String getArchivoUsuarios() {
		return archivoUsuarios;
	}

	public String getArchivoServicios() {
		return archivoServicios;
	}

	public String getArchivoVehiculos() {
		return archivoVehiculos;
	}

	public double getLatitudMin() {
		return latitudMin;
	}

	public double getLatitudMax() {
		return latitudMax;
	}

	public double getLongitudMin() {
		return longitudMin;
	}

	public double getLongitudMax() {
		return longitudMax;
	}

}
