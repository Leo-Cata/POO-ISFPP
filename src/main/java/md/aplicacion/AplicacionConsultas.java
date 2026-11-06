package md.aplicacion;

import java.io.IOException; 
import java.util.List;

import md.aplicacion.CargadorModelo.DatosIniciales;
import md.config.DAOLoader;
import md.datos.dao.DAOException;
import md.datos.dao.DAOFactory;
import md.interfaz.Interfaz;
import md.modelo.Servicio;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import net.datastructures.ProbeHashMap;
import md.ui.api.UIEngine;

public class AplicacionConsultas {

	public static void main(String[] args) throws IOException, DAOException {
		
		try {
			
			// Seleccionar persistencia
            DAOFactory persistencia = DAOLoader.getFactory();
            
            // Preparar el contexto de la aplicación
            ContextoAplicacion.inicializar(persistencia);
            
            
            // Crear e iniciar Motor de UI configurado (JavaFX / CLI)
            UIEngine uiEngine = DAOLoader.crearUIEngine();
            uiEngine.start();
		} catch (DAOException e) {
            System.err.println("No se pudieron cargar los datos: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        catch (IOException e) {
            System.err.println("No se pudo iniciar la aplicacion: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
	}
}
