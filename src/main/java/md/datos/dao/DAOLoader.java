package md.datos.dao;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import md.datos.dao.txt.TxtDAOFactory;

public class DAOLoader {
    private static DAOFactory instanciaFabrica;

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

    static {
        try {
            Properties prop = new Properties();
            try (InputStream input = new FileInputStream("config.properties")) {
                prop.load(input);

                String tipoOrigen = prop.getProperty("origen.datos", "TXT").toUpperCase();

                String archivoServicios = prop.getProperty("servicio", "servicios.txt");
                String archivoVehiculos = prop.getProperty("vehiculo", "vehiculos.txt");
                String archivoUsuarios = prop.getProperty("usuario", "usuarios.txt");

                double lat1 = Double.parseDouble(prop.getProperty("latitud1", "0"));
                double lat2 = Double.parseDouble(prop.getProperty("latitud2", "0"));
                double lon1 = Double.parseDouble(prop.getProperty("longitud1", "0"));
                double lon2 = Double.parseDouble(prop.getProperty("longitud2", "0"));

                latitudMin = Math.min(lat1, lat2);
                latitudMax = Math.max(lat1, lat2);
                longitudMin = Math.min(lon1, lon2);
                longitudMax = Math.max(lon1, lon2);

                if ("BD".equals(tipoOrigen)) {
                    // instanciaFabrica = new BdDAOFactory(); // Si existiera base de datos
                } else {
                    instanciaFabrica = new TxtDAOFactory(archivoServicios, archivoVehiculos, archivoUsuarios);
                }
            }
        } catch (Exception e) {
            instanciaFabrica = new TxtDAOFactory("servicios.txt", "vehiculos.txt", "usuarios.txt");
        }
    }

    public static DAOFactory getFactory() {
        return instanciaFabrica;
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
