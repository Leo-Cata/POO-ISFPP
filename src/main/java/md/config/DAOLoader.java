package md.config;

import md.datos.dao.DAOFactory;
import md.datos.dao.bd.BdDAOFactory;
import md.datos.dao.txt.TxtDAOFactory;


public class DAOLoader {
	
	public static DAOFactory getFactory() {
		String type = CargarParametros.getOrigenDatos();
		switch (type) {
		case "TXT":
			return new TxtDAOFactory(CargarParametros.getArchivoServicios(), CargarParametros.getArchivoVehiculos(),
					CargarParametros.getArchivoUsuarios());
		case "BD":
			return new BdDAOFactory(CargarParametros.getArchivoBdConfig());
		default:
			throw new IllegalStateException("Tipo de persistencia no soportado: " + type);
		}
	}
}	
