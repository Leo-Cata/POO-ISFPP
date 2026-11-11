package md.aplicacion;

import java.io.IOException;

import md.config.DAOLoader;
import md.datos.dao.DAOException;
import md.datos.dao.DAOFactory;
import md.ui.api.UIEngine;
import md.config.CargarParametros;

public class AplicacionConsultas {
    /**
     * Punto de entrada de la aplicación gráfica o de consola.
     * <p>
     * Selecciona la persistencia configurada (TXT o BD) con {@link DAOLoader},
     * inicializa el {@link ContextoAplicacion}, define el idioma y el país
     * y arranca la interfaz indicada en {@code app.ui.type}. Al terminar libera
     * los recursos del motor vial.
     *
     * @param args argumentos de línea de comandos (no se utilizan).
     * @throws IOException  si falla la lectura de algún archivo de configuración o
     *                      de datos.
     * @throws DAOException si ocurre un error al cargar los datos desde la
     *                      persistencia.
     */
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
