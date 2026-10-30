package md.datos.dao.bd;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

import md.datos.dao.DAOException;

public class ConexionBD {
	
	 private final String rutaPropiedades;
	    private String url;
	    private String usuario;
	    private String clave;
	    private String esquema;
	    
	    public ConexionBD(String rutaPropiedades) {
	        this.rutaPropiedades = rutaPropiedades;
	    }
	    
	    /**
	     * Abre una conexion nueva. Quien la pide es responsable de cerrarla
	     * (lo mas simple: abrirla dentro de un try-with-resources).
	     */
	    public Connection obtener() throws DAOException {
	        cargarConfiguracion();
	        try {
	            Connection con = DriverManager.getConnection(url, usuario, clave);
	            if (esquema != null) {
	                con.setSchema(esquema);
	            }
	            return con;
	        } catch (SQLException e) {
	            throw new DAOException("No se pudo conectar a la base de datos (" + url + ").", e);
	        }
	    }

	    private synchronized void cargarConfiguracion() throws DAOException {
	        if (url != null) {
	            return; // ya estaba cargada
	        }
	        Properties prop = new Properties();
	        try (InputStream entrada = new FileInputStream(rutaPropiedades)) {
	            prop.load(entrada);
	        } catch (IOException e) {
	            throw new DAOException("No se pudo leer el archivo de conexion a la BD: " + rutaPropiedades, e);
	        }

	        String driver = requerida(prop, "driver");
	        try {
	            Class.forName(driver);
	        } catch (ClassNotFoundException e) {
	            throw new DAOException("No se encontro el driver JDBC '" + driver
	                    + "'. Verifique que su .jar este en el build path / classpath.", e);
	        }

	        this.usuario = requerida(prop, "usr");
	        this.clave = requerida(prop, "pwd");
	        String schema = prop.getProperty("schema");
	        this.esquema = (schema == null || schema.trim().isEmpty()) ? null : schema.trim();
	        this.url = requerida(prop, "url"); // ultima: url != null indica "configuracion completa"
	    }

	    private String requerida(Properties prop, String nombre) throws DAOException {
	        String valor = prop.getProperty(nombre);
	        if (valor == null || valor.trim().isEmpty()) {
	            throw new DAOException("Falta la propiedad '" + nombre + "' en " + rutaPropiedades);
	        }
	        return valor.trim();
	    }

}
