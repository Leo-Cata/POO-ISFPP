package md.datos.dao.txt;

import java.io.FileNotFoundException;
import java.util.EnumSet;
import java.util.Scanner;
import java.util.Set;

import md.datos.dao.VehiculoDAO;
import md.datos.dto.VehiculoDTO;
import md.datos.utils.txtBase;
import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import net.datastructures.ProbeHashMap;

public class VehiculoDaoTxt extends txtBase implements VehiculoDAO {

    private String rutaParametro;

    public VehiculoDaoTxt(String rutaParametro) {
        this.rutaParametro = rutaParametro;
    }

    /**
     * Lee un archivo de texto y carga los vehículos del sistema en un mapa.
     * Verifica que no se ingresen vehículos con la misma patente.
     * 
     * @return Un ProbeHashMap donde la clave es la patente y el valor es el
     *         Vehiculo.
     * @throws FileNotFoundException    Si no se encuentra el archivo en la ruta
     *                                  especificada.
     * @throws IllegalArgumentException Si faltan datos en una línea o si se detecta
     *                                  una patente duplicada.
     */
    @Override
    public ProbeHashMap<String, VehiculoDTO> obtenerTodos() throws FileNotFoundException {
        ProbeHashMap<String, VehiculoDTO> vehiculos = new ProbeHashMap<>();

        // Uso de try-with-resources para asegurar el cierre automático del Scanner
        try (Scanner lector = abrir(rutaParametro)) {
            int numeroLinea = 0;
            while (lector.hasNextLine()) {
                String linea = lector.nextLine();
                numeroLinea++;

                // evita leer lineas en blanco o un comentario
                if (esIgnorable(linea))
                    continue;

                // cada campo ya esta limpio (sin espacios extras)
                String[] partes = partir(linea);

                // Un vehículo válido debe tener al menos 6 campos (índices 0 al 5)
                if (partes.length < 6) {
                    throw new IllegalArgumentException("Línea " + numeroLinea + " inválida (faltan datos).");
                }

                String patente = partes[0];
                String modelo = partes[1];
                int capacidadPasajeros = Integer.parseInt(partes[2]);

                TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(partes[3].toUpperCase());
                CategoriaVehiculo categoriaVehiculo = CategoriaVehiculo.valueOf(partes[4].toUpperCase());

                Set<TipoServicio> tiposServicio =
                        EnumSet.noneOf(TipoServicio.class);

                // El primer tipo de servicio es obligatorio.
                tiposServicio.add(
                        TipoServicio.valueOf(partes[5].toUpperCase())
                );
                // Ubicacion ubicacionInicial = null;

                // Verificamos SI el array tiene una 7ma posición (índice 6) antes de intentar
                // leerla
                if (partes.length > 6 && !partes[6].isEmpty()) {
                    tiposServicio.add(
                            TipoServicio.valueOf(partes[6].toUpperCase())
                    );
                }

                // Verificamos si la patente ya existe
                if (vehiculos.get(patente) != null) {
                    throw new IllegalArgumentException(
                            "Vehículo duplicado encontrado en el archivo con patente : " + patente);
                }
                // Instanciamos el vehículo cumpliendo con el constructor
                VehiculoDTO vehiculo = new VehiculoDTO(
                		patente, 
                		modelo, 
                		capacidadPasajeros, 
                		tipoVehiculo, 
                		categoriaVehiculo,
                        tiposServicio
                );

                vehiculos.put(patente, vehiculo);
            }
        }

        return vehiculos;
    }
}
