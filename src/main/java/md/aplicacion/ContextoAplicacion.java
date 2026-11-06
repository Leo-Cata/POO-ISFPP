package md.aplicacion;

import java.io.FileNotFoundException;

import md.datos.dao.DAOException;
import md.datos.dao.DAOFactory;
import md.modelo.Usuario;

/**
 * Mantiene disponibles los datos iniciales de la aplicación para que los
 * distintos componentes puedan acceder a ellos.
 */
public class ContextoAplicacion {
	private static CargadorModelo.DatosIniciales datos;
	private static Usuario usuarioLogeado; // mantiene el usuario logeado

	public static void inicializar(DAOFactory persistencia) throws DAOException, FileNotFoundException {

		datos = new CargadorModelo(persistencia).cargar();
	}

	public static CargadorModelo.DatosIniciales getDatosIniciales() {
		return datos;
	}

	public static void setUsuarioLogeado(Usuario usuario) {
		usuarioLogeado = usuario;
	}

	public static Usuario getUsuarioLogeaddo() {
		return usuarioLogeado;
	}
}
