package md.datos.utils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public abstract class txtBase {
    protected static final String COMENTARIO = "#";
    protected static final String SEPARADOR = ";";

    /** Abre el archivo */
    protected Scanner abrir(String archivo) throws FileNotFoundException {
        InputStream recurso = getClass()
                .getClassLoader()
                .getResourceAsStream(archivo.replace("\\", "/"));

        if (recurso == null) {
            throw new FileNotFoundException(
                    "No se encontro el archivo de datos: " + archivo);
        }

        return new Scanner(recurso, StandardCharsets.UTF_8);
    }

    /**
     * @return true si la linea esta en blanco o es un comentario (empieza con '#')
     */
    protected boolean esIgnorable(String linea) {
        String recortada = linea.trim();
        return recortada.isEmpty() || recortada.startsWith(COMENTARIO);
    }

    /** Parte la linea por ';' y recorta los espacios de cada campo. */
    protected String[] partir(String linea) {
        String[] partes = linea.split(SEPARADOR);
        for (int i = 0; i < partes.length; i++)
            partes[i] = partes[i].trim();
        return partes;
    }
}
