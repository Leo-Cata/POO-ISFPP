package md.aplicacion;

import java.io.FileNotFoundException;

import md.datos.dao.DAOException;
import md.datos.dao.DAOFactory;

/**
 * Mantiene disponibles los datos iniciales de la aplicación para que los
 * distintos componentes puedan acceder a ellos.
 */
public class ContextoAplicacion {
	private static CargadorModelo.DatosIniciales datos;

	public static void inicializar(DAOFactory persistencia) throws DAOException, FileNotFoundException {

		datos = new CargadorModelo(persistencia).cargar();
	}

	public static CargadorModelo.DatosIniciales getDatosIniciales() {
		return datos;
	}
}
