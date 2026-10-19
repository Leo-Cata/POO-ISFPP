package md.datos;

import java.io.FileNotFoundException;
import java.util.Scanner;

import md.datos.utils.txtBase;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
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
    public ProbeHashMap<String, Usuario> obtenerTodos(ProbeHashMap<String, Vehiculo> vehiculosCargados)
            throws FileNotFoundException {
        ProbeHashMap<String, Usuario> usuarios = new ProbeHashMap<>();
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

                Usuario usuario = new Usuario(nombre, telefono, email);

                // Verificamos si la línea tiene datos de conductor (licencia y al menos 1
                // patente)
                // Índice 3: Licencia | Índice 4: Patente principal
                if (partes.length >= 5 && !partes[3].isEmpty()) {
                    String licencia = partes[3];
                    String patentePrincipal = partes[4];

                    // Buscamos el vehículo en el mapa que pasamos por parámetro
                    Vehiculo vehiculoPrincipal = vehiculosCargados.get(patentePrincipal);

                    if (vehiculoPrincipal == null) {
                        throw new IllegalArgumentException(
                                "Vehículo no encontrado para la patente: " + patentePrincipal);
                    }

                    // Damos de alta al conductor con su primer vehículo
                    usuario.altaConductor(licencia, vehiculoPrincipal);

                    // Si el array tiene más posiciones (índice 5 en adelante), cargamos los
                    // vehículos extra
                    for (int i = 5; i < partes.length; i++) {
                        String patenteExtra = partes[i];

                        if (!patenteExtra.isEmpty()) {
                            Vehiculo vehiculoExtra = vehiculosCargados.get(patenteExtra);
                            if (vehiculoExtra != null) {
                                usuario.getConductor().agregarVehiculo(vehiculoExtra);
                            }
                        }
                    }
                }

                // Verificamos si el email ya existe
                if (usuarios.get(email) != null) {
                    throw new IllegalArgumentException("Usuario duplicado encontrado en el archivo: " + email);
                }

                // Guardamos el usuario en el mapa usando su email como clave
                usuarios.put(email, usuario);
            }
        }

        return usuarios;
    }

}
