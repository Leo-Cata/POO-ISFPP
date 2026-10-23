package md.datos.dto;

import java.util.HashSet;
import java.util.Set;

/**
 * Objeto de transferencia de datos (DTO) que representa la información de un
 * usuario, incluyendo sus datos personales y patentes de vehículos asociadas.
 */
public class UsuarioDTO {

	// atributos del objeto usuario
	private String nombre;
	private String telefono;
	private String email;

	private String licencia;
	private String patentePrincipal;
	private Set<String> patentesAdicionales;

	/**
	 * Construye una nueva instancia de {@code UsuarioDTO} con todos los datos del
	 * usuario. Si el conjunto de patentes adicionales es {@code null}, se
	 * inicializa como un conjunto vacío.
	 *
	 * @param nombre              nombre completo del usuario.
	 * @param telefono            número de teléfono de contacto del usuario.
	 * @param email               dirección de correo electrónico del usuario.
	 * @param licencia            número de licencia de conducir del usuario; puede
	 *                            ser {@code null} si no es conductor.
	 * @param patentePrincipal    patente del vehículo principal asociado al
	 *                            usuario; puede ser {@code null} si no es
	 *                            conductor.
	 * @param patentesAdicionales conjunto de patentes de vehículos adicionales;
	 *                            puede ser {@code null}.
	 */
	public UsuarioDTO(String nombre, String telefono, String email, String licencia, String patentePrincipal,
			Set<String> patentesAdicionales) {
		this.nombre = nombre;
		this.telefono = telefono;
		this.email = email;
		this.licencia = licencia;
		this.patentePrincipal = patentePrincipal;

		if (patentesAdicionales == null) {
			this.patentesAdicionales = new HashSet<>();
		} else {
			this.patentesAdicionales = new HashSet<>(patentesAdicionales);
		}
	}

	/**
	 * Retorna el nombre completo del usuario.
	 *
	 * @return nombre del usuario.
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * Retorna el número de teléfono de contacto del usuario.
	 *
	 * @return teléfono del usuario.
	 */
	public String getTelefono() {
		return telefono;
	}

	/**
	 * Retorna la dirección de correo electrónico del usuario.
	 *
	 * @return email del usuario.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Retorna el número de licencia de conducir del usuario.
	 *
	 * @return licencia del usuario o null si el usuario no es conductor.
	 */
	public String getLicencia() {
		return licencia;
	}

	/**
	 * Retorna la patente del vehículo principal asociado al usuario.
	 *
	 * @return patente principal del usuario o null si el usuario no es conductor.
	 */
	public String getPatentePrincipal() {
		return patentePrincipal;
	}

	/**
	 * Retorna una copia defensiva del conjunto de patentes adicionales del usuario,
	 * evitando la modificación directa del estado interno del objeto.
	 *
	 * @return conjunto de patentes adicionales del usuario o vacío si el usuario no
	 *         es conductor o tiene un solo vehículo.
	 */
	public Set<String> getPatentesAdicionales() {
		return new HashSet<>(patentesAdicionales);
	}

}
