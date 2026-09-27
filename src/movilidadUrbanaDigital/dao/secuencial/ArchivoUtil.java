package movilidadUrbanaDigital.dao.secuencial;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilidad compartida por todos los DAO secuenciales para leer y escribir
 * lineas de texto. No se instancia (metodos estaticos).
 * 
 * ninguna clase externa necesita tocarla directamente, es una organizacion interna de cómo están 
 * implementados los DAO secuenciales.
 */
class ArchivoUtil {
	   private ArchivoUtil() {}	//impide que se instanci desde otra clase
	   
	    static List<String> leerLineas(String ruta) {
	        try {
	            Path path = Paths.get(ruta);		//convierte el texto en una ubicación de archivo.
	            if (!Files.exists(path)) {
	                return new ArrayList<>();
	            }
	            return Files.readAllLines(path);	// lee el archivo entero y devuelve cada línea como un String dentro de una lista.
	        } catch (IOException e) {
	            throw new RuntimeException("Error al leer el archivo: " + ruta, e);
	        }
	    }
	    
	    //reemplaza todo el contenido del archivo con la lista pasada
	    static void escribirLineas(String ruta, List<String> lineas) {
	        try {
	            Path path = Paths.get(ruta);
	            if (path.getParent() != null) {
	                Files.createDirectories(path.getParent());
	            }
	            Files.write(path, lineas);		// sobrescribe el archivo entero con las líneas de la lista.
	        } catch (IOException e) {
	            throw new RuntimeException("Error al escribir el archivo: " + ruta, e);
	        }
	    }
	    
	    
	    // agrega una sola línea al final del archivo
	    static void agregarLinea(String ruta, String linea) {
	        try {
	            Path path = Paths.get(ruta);		//convierte el texto en una ubicación de archivo.
	            if (path.getParent() != null) {
	                Files.createDirectories(path.getParent());
	            }
	            if (!Files.exists(path)) {
	                Files.createFile(path);
	            }
	            
	            // Collections.singletonList(linea): convierte el String en una lista de un solo elemento, porque Files.write recibe una lista.
	            // StandardOpenOption.APPEND: escribe la linea al final.
	            Files.write(path, Collections.singletonList(linea), StandardOpenOption.APPEND);		
	        } catch (IOException e) {
	            throw new RuntimeException("Error al escribir el archivo: " + ruta, e);
	        }
	    }
}
