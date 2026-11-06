package md.config;

import md.datos.dao.DAOFactory;
import md.datos.dao.bd.BdDAOFactory;
import md.datos.dao.txt.TxtDAOFactory;
import md.ui.api.UIEngine;
import md.ui.impl.cli.CommandLineInterface;


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
	
	public static UIEngine crearUIEngine() {
		String uiType = CargarParametros.getUiType();
        if ("JAVAFX".equals(uiType)) {
            try {
                // Evita una referencia directa del selector a la clase concreta.
                // El proyecto completo sigue necesitando JavaFX para compilar su implementación.
                Class<?> clazz = Class.forName("md.ui.impl.javafx.JavaFXUserInterface");
                return (UIEngine) clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
            	throw new IllegalStateException("No se pudo cargar JavaFX: " + e.getMessage());
            }
        }
        if (!"CLI".equals(uiType) && !"JAVAFX".equals(uiType)) {
            throw new IllegalStateException("Tipo de interfaz no soportado: " + uiType);
        }
        return new CommandLineInterface();
    }
}
		
