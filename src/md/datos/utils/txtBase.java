package md.datos.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public abstract class txtBase {
    protected static final String COMENTARIO = "#";
    protected static final String SEPARADOR = ";";

    /** Abre el archivo */
    protected Scanner abrir(String fileName) throws FileNotFoundException {
        if (fileName == null || fileName.trim().isEmpty())
            throw new IllegalArgumentException("El nombre del archivo no puede ser null ni vacio");
        return new Scanner(new File(fileName));
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
