package md.datos;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import md.datos.utils.txtBase;
import md.modelo.CategoriaVehiculo;
import md.modelo.Servicio;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;

public class ServicioDaoTxt extends txtBase implements ServicioDAO {
    private String rutaArchivo;

    // constructor con la ruta en vez de que el metodo requiera que se pase por
    // parametro
    public ServicioDaoTxt(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    /**
     * Lee un archivo de texto y carga una lista de servicios disponibles.
     * 
     * @return Una lista (List) de objetos Servicio instanciados.
     * @throws FileNotFoundException    Si no se encuentra el archivo en la ruta
     *                                  especificada.
     * @throws IllegalArgumentException Si una línea del archivo no tiene los datos
     *                                  suficientes o el formato es incorrecto.
     */
    @Override
    public List<Servicio> obtenerTodos() throws FileNotFoundException {
        List<Servicio> servicios = new ArrayList<Servicio>();

        // Uso de try-with-resources para asegurar el cierre automático del Scanner
        try (Scanner lector = abrir(rutaArchivo)) {
            int numeroLinea = 0;
            while (lector.hasNextLine()) {
                String linea = lector.nextLine();
                numeroLinea++;

                // evita leer lineas en blanco o un comentario
                if (esIgnorable(linea))
                    continue;

                // cada campo ya esta limpio (sin espacios extras)
                String[] partes = partir(linea);

                if (partes.length < 7) {
                    throw new IllegalArgumentException("Línea " + numeroLinea + " inválida.");
                }

                // Aplicamos trim() por si el txt tiene espacios en blanco "Perdidos"
                String nombre = partes[0];
                double tarifaBase = Double.parseDouble(partes[1]);
                double precioPorKm = Double.parseDouble(partes[2]);
                double precioPorMinuto = Double.parseDouble(partes[3]);

                // Convertimos a mayúsculas correctamente
                // Convertimos a mayúsculas correctamente
                String tipoVehiculoStr = partes[4].toUpperCase();
                String categoriaVehiculoStr = partes[5].toUpperCase();
                String tipoServicioStr = partes[6].toUpperCase();

                // CONVERSIÓN A ENUMS:
                TipoServicio tipoServicio = TipoServicio.valueOf(tipoServicioStr);
                CategoriaVehiculo categoriaVehiculo = CategoriaVehiculo.valueOf(categoriaVehiculoStr);
                TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(tipoVehiculoStr);

                // Instanciamos pasándole los objetos Enum
                Servicio servicio = new Servicio(nombre, tarifaBase, precioPorKm, precioPorMinuto,
                        tipoVehiculo, categoriaVehiculo, tipoServicio);

                servicios.add(servicio);
            }
        }

        return servicios;
    }
}
