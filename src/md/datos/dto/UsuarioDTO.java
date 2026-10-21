package md.datos.dto;

import java.util.HashSet;
import java.util.Set;

public class UsuarioDTO {

	// atributos del objeto usuario
	private String nombre;
	private String telefono;
	private String email;

	private String licencia;
	private String patentePrincipal;
	private Set<String> patentesAdicionales;

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

	public String getNombre() {
		return nombre;
	}

	public String getTelefono() {
		return telefono;
	}

	public String getEmail() {
		return email;
	}

	public String getLicencia() {
		return licencia;
	}

	public String getPatentePrincipal() {
		return patentePrincipal;
	}

	public Set<String> getPatentesAdicionales() {
		return new HashSet<>(patentesAdicionales);
	}

}
