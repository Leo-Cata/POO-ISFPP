package md.datos.dao.txt;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import md.datos.dao.UsuarioDAO;
import md.datos.dto.UsuarioDTO;
import md.datos.utils.txtBase;
import net.datastructures.ProbeHashMap;

public class UsuarioDaoTxt extends txtBase implements UsuarioDAO {
    private String rutaArchivo;

    public UsuarioDaoTxt(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    /**
     * Lee un archivo de texto, carga los usuarios y vincula a aquellos que son
     * conductores con sus vehículos correspondientes. Verifica que no se dupliquen
     * correos electrónicos.
     * 
     * @param vehiculosCargados El mapa de vehículos previamente cargados en
     *                          memoria.
     * @return Un ProbeHashMap donde la clave es el email y el valor es el Usuario.
     * @throws FileNotFoundException    Si no se encuentra el archivo en la ruta
     *                                  especificada.
     * @throws IllegalArgumentException Si faltan datos básicos, si hay emails
     *                                  duplicados o si se asigna una patente
     *                                  inexistente a un conductor.
     */
    @Override
    public ProbeHashMap<String, UsuarioDTO> obtenerTodos()
            throws FileNotFoundException {
        ProbeHashMap<String, UsuarioDTO> usuarios = new ProbeHashMap<>();
        try (Scanner lector = abrir(rutaArchivo)) {
            int numeroLinea = 0;
            while (lector.hasNextLine()) {
                String linea = lector.nextLine();
                numeroLinea++;

                // evita leer lineas en blanco o un comentario
                if (esIgnorable(linea))
                    continue;

                // cada campo ya esta limpio (sin espacios extras)
                String[] partes = partir(linea);

                if (partes.length < 3) {
                    throw new IllegalArgumentException("Línea " + numeroLinea + " es inválida (faltan datos básicos)");
                }

                String nombre = partes[0];
                String telefono = partes[1];
                String email = partes[2];
                
                String licencia = null;
                String patentePrincipal = null;
                
                Set<String> patentesAdicionales = new HashSet<>();

                if (partes.length >= 4 && !partes[3].isEmpty()) {
                    licencia = partes[3];

                    if (partes.length < 5 || partes[4].isEmpty()) {
                        throw new IllegalArgumentException(
                                "Línea " + numeroLinea
                                + ": el conductor no tiene patente principal.");
                    }

                    patentePrincipal = partes[4];

                    // Las patentes adicionales comienzan
                    // en la posición 5.
                    for (int i = 5; i < partes.length; i++) {
                        if (!partes[i].isEmpty()) {
                            patentesAdicionales.add(partes[i]);
                        }
                    }
                }
                
                // La clave del mapa sigue siendo el email.
                if (usuarios.get(email) != null) {
                    throw new IllegalArgumentException(
                            "Usuario duplicado encontrado en el archivo: "
                            + email);
                }
                UsuarioDTO usuario = new UsuarioDTO(
                        nombre,
                        telefono,
                        email,
                        licencia,
                        patentePrincipal,
                        patentesAdicionales
                );
                
                usuarios.put(email,usuario);
                

                
            }
        }

        return usuarios;
    }

}
