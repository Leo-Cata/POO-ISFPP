package movilidadUrbanaDigital;

import java.util.Objects;


public abstract class Usuario {
    // atributos del objeto usuario
    private String nombre;
    private String telefono;
    private String email;
    private RolUsuario rolActivo;
    private Cliente cliente;
    private Conductor conductor;


    // constructor del objeto usuario
    public Usuario(String nombre, String telefono, String email, RolUsuario rolActivo) {
		this.nombre = nombre;
		this.telefono = telefono;
		this.email = email;
		this.rolActivo = rolActivo;
		//el diagrama indica una relacion de composicion, el usuario crea al cliente
		this.cliente = new Cliente();
		//el conductor en nullo al principio segun la cardianlidad, si es conductor de lo da de alta luego
		this.conductor = null;
	}

    public void altaConductor(String licencia, Vehiculo vehiculo) {
    	this.conductor = new Conductor(licencia, vehiculo.getCategoriaVehiculo(), vehiculo, EstadoConductor.FUERA_DE_SERVICIO);
    }
    
    // getters y setters del objeto usuario
    public String getNombre() {
        return nombre;
    }

	public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
   
    public RolUsuario getRolActivo() {
		return rolActivo;
	}

	public void setRolActivo(RolUsuario rolActivo) {
		this.rolActivo = rolActivo;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Conductor getConductor() {
		return conductor;
	}

	public void setConductor(Conductor conductor) {
		this.conductor = conductor;
	}

	// hash y equals del objeto usuario
    @Override
 	public int hashCode() {
 		return Objects.hash(email);
 	}

 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Usuario other = (Usuario) obj;
 		return Objects.equals(email, other.email);
 	}

	@Override
	public String toString() {
		return "Usuario [nombre=" + nombre + ", telefono=" + telefono + ", email=" + email + ", rolActivo=" + rolActivo
				+ ", cliente=" + cliente + ", conductor=" + conductor + "]";
	}

    
}
