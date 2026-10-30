package md.datos.dao.txt;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import md.datos.dao.DAOException;
import md.datos.dao.ServicioDAO;
import md.datos.dto.ServicioDTO;
import md.datos.utils.txtBase;
import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;

/**
 * Implementación de ServicioDAO que obtiene los datos de servicios
 * desde un archivo de texto plano con campos delimitados.
 */
public class ServicioDaoTxt extends txtBase implements ServicioDAO {
	// Ruta del archivo de texto que contiene los datos de los servicios.
	private String rutaArchivo;

	// constructor con la ruta en vez de que el metodo requiera que se pase por
	// parametro
	public ServicioDaoTxt(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}

	/**
	 * Lee el archivo de texto cuya ruta recibe por constructor y construye una
	 * lista de objetos ServicioDTO a partir de cada línea válida. Las
	 * líneas en blanco y los comentarios son ignorados automáticamente. Cada línea
	 * debe contener al menos siete campos en el orden: nombre, tarifaBase,
	 * precioPorKm, precioPorMinuto, tipoVehiculo, categoriaVehiculo y tipoServicio.
	 *
	 * @return Lista de ServicioDTO con todos los servicios leídos del archivo.
	 * @throws DAOException 			Si el archivo no existe en la ruta
	 *                                  especificada.
	 * @throws IllegalArgumentException Si una línea del archivo contiene menos de
	 *                                  siete campos o tiene un formato incorrecto.
	 */
	@Override
	public List<ServicioDTO> obtenerTodos() throws DAOException {
		List<ServicioDTO> servicios = new ArrayList<ServicioDTO>();

		// Uso de try-with-resources para asegurar el cierre automático del Scanner
		try (Scanner lector = abrir(rutaArchivo)) {
			int numeroLinea = 0;
			while (lector.hasNextLine()) {
				String linea = lector.nextLine();
				numeroLinea++;

				// Evita leer lineas en blanco o un comentario
				if (esIgnorable(linea))
					continue;

				// Cada campo ya esta limpio (sin espacios extras)
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
				String tipoVehiculoStr = partes[4].toUpperCase();
				String categoriaVehiculoStr = partes[5].toUpperCase();
				String tipoServicioStr = partes[6].toUpperCase();

				// CONVERSIÓN A ENUMS:
				TipoServicio tipoServicio = TipoServicio.valueOf(tipoServicioStr);
				CategoriaVehiculo categoriaVehiculo = CategoriaVehiculo.valueOf(categoriaVehiculoStr);
				TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(tipoVehiculoStr);

				// Instanciamos pasándole los objetos Enum
				ServicioDTO servicio = new ServicioDTO(nombre, tarifaBase, precioPorKm, precioPorMinuto, tipoVehiculo,
						categoriaVehiculo, tipoServicio);

				servicios.add(servicio);
			}
		} catch (FileNotFoundException e) {
			throw new DAOException("No se encontro el archivo de datos: " + rutaArchivo, e);
		}

		return servicios;
	}
}
