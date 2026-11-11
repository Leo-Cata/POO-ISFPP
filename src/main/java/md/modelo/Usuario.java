	package md.modelo;
	
	import java.util.Objects;
	
	public class Usuario {
		// atributos del objeto usuario
		private String nombre;
		private String telefono;
		private String email;
		private RolUsuario rolActivo;
		private Cliente cliente;
		private Conductor conductor;
	
		// constructor del objeto usuario
		public Usuario(String nombre, String telefono, String email) {
			this.nombre = nombre;
			this.telefono = telefono;
			this.email = email;
			this.rolActivo = RolUsuario.CLIENTE;
			// el diagrama indica una relacion de composicion, el usuario crea al cliente
			this.cliente = new Cliente();
			// el conductor en nullo al principio segun la cardianlidad, si es conductor se
			// lo da de alta luego
			this.conductor = null;
		}
	
		/**
		 * Da de alta al usuario como conductor con su primer vehiculo.
		 * 
		 * @throws IllegalStateException si ya era conductor
		 *                               si la licencia o el vehiculo es null
		 */
		public void altaConductor(String licencia, Vehiculo vehiculo) {
	
			if (licencia == null)
				throw new IllegalArgumentException("La licencia no puede ser null.");
	
			if (vehiculo == null)
				throw new IllegalArgumentException("El vehiculo no puede ser null.");
	
			if (this.conductor != null)
				throw new IllegalStateException(nombre + "(" + email + ")" + "ya está dado de alta.");
	
			this.conductor = new Conductor(licencia, vehiculo.getCategoriaVehiculo(), vehiculo,
					EstadoConductor.FUERA_DE_SERVICIO);
		}
	
		/**
		 * Cambia el rol con el que usa la app. Un solo rol a la vez y nunca en medio de
		 * un viaje.
		 * 
		 * @throws IllegalStateException si tiene un viaje en curso, o si pide CONDUCTOR
		 *                               sin ser conductor
		 */
		public void cambiarRolActivo(RolUsuario rolNuevo) {
	
		    if (rolNuevo == null) {
		        throw new IllegalArgumentException("El rol no puede ser null.");
		    }
	
		    if (rolNuevo == this.rolActivo) {
		        return;
		    }
	
		    if (this.cliente.enViaje()
		            || this.conductor != null && this.conductor.enViaje()) {
		        throw new IllegalStateException(
		                "No se puede cambiar el rol con un viaje en curso.");
		    }
	
		    if (rolNuevo == RolUsuario.CONDUCTOR) {
	
		        if (this.conductor == null) {
		            throw new IllegalStateException("El usuario no está dado de alta como conductor.");
		        }
	
		        EstadoConductor estado = this.conductor.getEstadoConductor();
	
		        if (estado != EstadoConductor.FUERA_DE_SERVICIO) {
	
		            throw new IllegalStateException("El conductor debe estar FUERA_DE_SERVICIO para asumir el rol.");
		        }
	
		        // Al asumir el rol de conductor, comienza a recibir solicitudes.
		        this.conductor.setEstado(EstadoConductor.DISPONIBLE);
		    }
	
		    // Si deja de ser conductor, ya no puede recibir solicitudes.
		    if (rolNuevo == RolUsuario.CLIENTE
		            && this.conductor != null
		            && this.conductor.getEstadoConductor() == EstadoConductor.DISPONIBLE) {
	
		        this.conductor.setEstado(EstadoConductor.FUERA_DE_SERVICIO);
		    }
	
		    this.rolActivo = rolNuevo;
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
	
		// dos usuarios son iguales si poseen el mismo email
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
