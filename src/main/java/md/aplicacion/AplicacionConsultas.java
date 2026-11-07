package md.aplicacion;

import java.io.IOException;  

import md.config.DAOLoader;
import md.datos.dao.DAOException;
import md.datos.dao.DAOFactory;
import md.ui.api.UIEngine;
import md.config.CargarParametros;

public class AplicacionConsultas {

	public static void main(String[] args) throws IOException, DAOException {
		
		try {
			
			// Seleccionar persistencia
            DAOFactory persistencia = DAOLoader.getFactory();
            
            // Preparar el contexto de la aplicación
            ContextoAplicacion.inicializar(persistencia);
            
            // Selecciona el idioma y pais
            String idioma = CargarParametros.getLanguage();
            String pais = CargarParametros.getCountry();
            ContextoAplicacion.inicializarIdioma(idioma, pais);
            
            // Crear e iniciar Motor de UI configurado (JavaFX / CLI)
            UIEngine uiEngine = DAOLoader.crearUIEngine();
            uiEngine.start();
		} finally {

            // Liberar los recursos del motor vial al finalizar.
            ContextoAplicacion.cerrar();
        }
	}
}
