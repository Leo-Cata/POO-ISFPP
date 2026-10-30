package md.datos.dao.txt;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import md.datos.dao.UsuarioDAO;
import md.datos.dto.UsuarioDTO;
import md.datos.utils.txtBase;
import net.datastructures.ProbeHashMap;

/**
 * Implementación de {@link UsuarioDAO} que obtiene los datos de usuarios a
 * partir de un archivo de texto plano, construyendo un mapa indexado por correo
 * electrónico.
 */
public class UsuarioDaoTxt extends txtBase implements UsuarioDAO {
	// Ruta al archivo de texto que contiene los datos de los usuarios.
	private String rutaArchivo;

	public UsuarioDaoTxt(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}

	/**
	 * Lee un archivo de texto, carga los usuarios y si tiene patente principal y/o
	 * adicionales las carga como texto. Verifica que no se dupliquen correos
	 * electrónicos.
	 *
	 * @return Un {@link ProbeHashMap} donde la clave es el email del usuario y el
	 *         valor es el {@link UsuarioDTO} correspondiente.
	 * @throws FileNotFoundException    Si no se encuentra el archivo en la ruta
	 *                                  especificada.
	 * @throws IllegalArgumentException Si faltan datos básicos en alguna línea, si
	 *                                  se detectan correos electrónicos duplicados,
	 *                                  o si el conductor no tiene asignada su
	 *                                  patente principal.
	 */
	@Override
	public ProbeHashMap<String, UsuarioDTO> obtenerTodos() {
		ProbeHashMap<String, UsuarioDTO> usuarios = new ProbeHashMap<>();
		try (Scanner lector = abrir(rutaArchivo)) {
			int numeroLinea = 0;
			while (lector.hasNextLine()) {
				String linea = lector.nextLine();
				numeroLinea++;

				// Evita procesar líneas en blanco o comentarios.
				if (esIgnorable(linea))
					continue;

				// Cada campo ya esta limpio (sin espacios extras)
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
								"Línea " + numeroLinea + ": el conductor no tiene patente principal.");
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
					throw new IllegalArgumentException("Usuario duplicado encontrado en el archivo: " + email);
				}
				UsuarioDTO usuario = new UsuarioDTO(nombre, telefono, email, licencia, patentePrincipal,
						patentesAdicionales);

				usuarios.put(email, usuario);

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return usuarios;
	}

}
