
package md.simulacion;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

/**
 * Gestiona la carga y consulta de los parámetros de configuración
 * específicos de la simulación.
 *
 * Los parámetros se obtienen del archivo simulacion.properties,
 * ubicado en el classpath de la aplicación.
 *
 * Esta clase es independiente de la configuración general
 * de la aplicación.
 *
 */
public final class ConfiguracionSimulacion {

    private static final String ARCHIVO_CONFIG = "simulacion.properties";

    private static final String CLAVE_DURACION = "simulacion.duracion.segundos";
    private static final String CLAVE_FACTOR = "simulacion.factor.aceleracion";
    private static final String CLAVE_POLITICA = "simulacion.politica";
    private static final String CLAVE_ESPERA = "simulacion.espera.maxima.segundos";
    private static final String CLAVE_INTERVALO = "simulacion.intervalo.solicitudes.ms";

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = ConfiguracionSimulacion.class
                .getClassLoader()
                .getResourceAsStream(ARCHIVO_CONFIG)) {

            if (input == null) {
                throw new IllegalStateException(
                        "No se encontró el archivo " + ARCHIVO_CONFIG);
            }

            PROPERTIES.load(
                    new InputStreamReader(input, StandardCharsets.UTF_8));

        } catch (Exception e) {
            throw new ExceptionInInitializerError(
                    "Error al cargar la configuración de simulación: "
                            + e.getMessage());
        }
    }

    /**
     * Constructor privado para impedir la creación de instancias.
     * de manera = new ConfiguracionSimulacion ya que contiene atributos y metodos
     * estaticos
     */
    private ConfiguracionSimulacion() {
    }

    /**
     * Obtiene la duración total de la simulación.
     *
     * @return duración en segundos
     */
    public static long getDuracionSegundos() {
        return leerEntero(CLAVE_DURACION);
    }

    /**
     * Obtiene el factor de aceleración de los recorridos.
     *
     * @return factor de aceleración
     * @throws IllegalStateException si el valor no es válido
     */
    public static double getFactorAceleracion() {
        double factor = leerDecimal(CLAVE_FACTOR);

        if (!Double.isFinite(factor) || factor <= 0) {
            throw new IllegalStateException(
                    "El factor de aceleración debe ser mayor que cero.");
        }

        return factor;
    }

    /**
     * Obtiene la política utilizada para asignar conductores.
     *
     * El valor se normaliza a mayúsculas.
     *
     * @return nombre de la política de asignación
     */
    public static String getPolitica() {
        return leerClave(CLAVE_POLITICA)
                .trim()
                .toUpperCase(Locale.ROOT); // Locale.ROOT asegura que las palabras, las cuales son identificadores
                                           // tecnicos no sean traducidas
    }

    /**
     * Obtiene el tiempo máximo de espera para conseguir conductor.
     *
     * @return tiempo máximo en segundos
     */
    public static long getEsperaMaximaSegundos() {
        return leerEntero(CLAVE_ESPERA);
    }

    /**
     * Obtiene el intervalo entre solicitudes generadas.
     *
     * @return intervalo en milisegundos
     */
    public static long getIntervaloSolicitudesMs() {
        return leerEntero(CLAVE_INTERVALO);
    }

    /**
     * Lee una propiedad del archivo de configuración.
     *
     * @param clave nombre de la propiedad
     * @return valor asociado a la propiedad
     * @throws IllegalStateException si la propiedad no existe o está vacía
     */
    private static String leerClave(String clave) {
        String valor = PROPERTIES.getProperty(clave);

        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalStateException(
                    "La clave '" + clave
                            + "' no existe o está vacía en "
                            + ARCHIVO_CONFIG);
        }

        return valor;
    }

    /**
     * Lee y valida una propiedad numérica entera positiva.
     *
     * @param clave nombre de la propiedad
     * @return valor entero leído
     * @throws IllegalStateException si el valor no es válido
     */
    private static long leerEntero(String clave) {
        try {
            long numero = Long.parseLong(leerClave(clave).trim());

            if (numero <= 0) {
                throw new IllegalStateException(
                        "La clave '" + clave
                                + "' debe ser mayor que cero.");
            }

            return numero;

        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "La clave '" + clave
                            + "' debe ser un número entero.");
        }
    }

    /**
     * Lee una propiedad numérica decimal.
     *
     * @param clave nombre de la propiedad
     * @return valor decimal leído
     * @throws IllegalStateException si el valor no es numérico
     */
    private static double leerDecimal(String clave) {
        try {
            return Double.parseDouble(leerClave(clave).trim());

        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "La clave '" + clave
                            + "' debe ser un número.");
        }
    }
}
