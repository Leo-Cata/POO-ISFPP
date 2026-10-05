package movilidadUrbanaDigital.logica;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import movilidadUrbanaDigital.CategoriaVehiculo;
import movilidadUrbanaDigital.Conductor;
import movilidadUrbanaDigital.EstadoConductor;
import movilidadUrbanaDigital.Servicio;
import movilidadUrbanaDigital.Ubicacion;
import movilidadUrbanaDigital.Usuario;
import movilidadUrbanaDigital.Vehiculo;
import movilidadUrbanaDigital.Viaje;
import movilidadUrbanaDigital.datos.CargarDatos;
import net.datastructures.ProbeHashMap;

public class SistemaViajes {
    private List<Servicio> serviciosDisponibles;
    private ProbeHashMap<String, Vehiculo> vehiculosRegistrados;
    private ProbeHashMap<String, Usuario> usuariosRegistrados;
    private ProbeHashMap<String,Conductor> conductoresRegistrados;
    private ProbeHashMap<UUID,Viaje> viajesRegistrados;
    
    //Inicializa las estructuras vacías al nacer el objeto
    public SistemaViajes() {
        this.serviciosDisponibles = new ArrayList<>();
        this.vehiculosRegistrados = new ProbeHashMap<>();
        this.usuariosRegistrados = new ProbeHashMap<>();
        this.conductoresRegistrados = new ProbeHashMap<>();
        this.viajesRegistrados = new ProbeHashMap<>();
    }
    
    //metodo de inicializacion
    //carga los datos a las coleccion que inicializamos vacias, con los datos txt
    public void iniciarSistema() {
        try {
            this.serviciosDisponibles = CargarDatos.cargarServicios("servicios.txt");
            this.vehiculosRegistrados = CargarDatos.cargarVehiculos("vehiculos.txt");
            this.usuariosRegistrados = CargarDatos.cargarUsuarios("usuarios.txt", this.vehiculosRegistrados);
            
            System.out.println("Sistema inicializado correctamente.");
            
        } catch (FileNotFoundException e) {
            System.err.println("Error crítico: No se encontró uno de los archivos de base de datos.");
        } catch (IllegalArgumentException e) {
            System.err.println("Error en el formato de los datos: " + e.getMessage());
        }
    }

    /**
     * Instancia un nuevo objeto Usuario
     * @param nombre
     * @param telefono
     * @param email
     * @return usuario registrado
     * @throws IllegalArgumentException si el email ya existe en el sistema
     */
    public Usuario registrarUsuario(String nombre, String telefono, String email) {
        
        //Verificar que el email no este registrado antes 
        if (usuariosRegistrados.get(email) != null) {
        	throw new IllegalArgumentException("El email " + email + " ya está registrado en el sistema.");
        }
        
        //Instanciar y guardar
        Usuario usuario = new Usuario(nombre, telefono, email);
        usuariosRegistrados.put(email, usuario);
        
        return usuario;
    }
        
    
    /**
     * Habilita a un usuario existente como conductor.
     * @param email
     * @param licencia
     * @param vehiculo
     * @return conductor habilitado
     * @throws IllegalArgumentException si el usuario no existe o la licencia ya está en uso
     */
    public Conductor habilitarConductor(String email, String licencia, Vehiculo vehiculo) {
        
        //Verificar que la licencia no esté registrada antes
        if (conductoresRegistrados.get(licencia) != null) {
            throw new IllegalArgumentException("La licencia " + licencia + " ya está registrada en el sistema.");
        }
        
        //Buscar al usuario y verificar que exista
        Usuario usuario = usuariosRegistrados.get(email);
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario con email " + email + " no existe.");
        }
        
        //Delegar la creación: el objeto Usuario se encarga de instanciar su Conductor
        usuario.altaConductor(licencia, vehiculo);
        
        //Recuperar esa instancia que acaba de crear el Usuario
        Conductor conductor = usuario.getConductor();
        
        //Guardar el objeto en el mapa de rápido acceso
        conductoresRegistrados.put(licencia, conductor);
        
        return conductor;
    }
    
    /**
     * El conductor inicia su jornada laboral, elige qué vehículo usar y con qué categoría operará.
     * @param licencia del conductor
     * @param patente del vehículo que va a usar
     * @param categoriaVehiculoActivo la categoría (igual o menor) que acepta para recibir viajes
     */
    public void iniciarTurnoConductor(String licencia, String patente, CategoriaVehiculo categoriaVehiculoActivo) {
        
        // Buscar al conductor y al vehículo
        Conductor conductor = conductoresRegistrados.get(licencia);
        if (conductor == null) {
            throw new IllegalArgumentException("El conductor con licencia " + licencia + " no existe.");
        }
        
        Vehiculo vehiculoElegido = vehiculosRegistrados.get(patente);
        if (vehiculoElegido == null) {
            throw new IllegalArgumentException("El vehículo con patente " + patente + " no existe.");
        }

        // Validar que la categoría que elige no sea mayor a la del vehículo que va a utilizar 
        if (categoriaVehiculoActivo.getValor() > vehiculoElegido.getCategoriaVehiculo().getValor()) {
            throw new IllegalArgumentException("No podés operar con una categoría mayor a la de tu vehículo.");
        }
        
        //Asignar el vehículo 
        conductor.setVehiculoActivo(vehiculoElegido); 
        
        //Asignar la categoría que elegio
        conductor.setCategoriaVehiculoActivo(categoriaVehiculoActivo);
        
        //Cambiar su estado a DISPONIBLE
        conductor.setEstado(EstadoConductor.DISPONIBLE);
    }
    /**
     * Finaliza el turno del conductor, pasándolo a fuera de servicio.
     * @param licencia del conductor que termina su jornada
     */
    public void finalizarTurnoConductor(String licencia) {
    	 // Buscar al conductor y al vehículo
        Conductor conductor = conductoresRegistrados.get(licencia);
        if (conductor == null) {
            throw new IllegalArgumentException("El conductor con licencia " + licencia + " no existe.");
        }
        //verificar que el conductor no este en viaje
        if (conductor.enViaje()) {
            throw new IllegalStateException("No podés finalizar tu turno mientras estás en un viaje.");
        }
     // Lo pasamos a fuera de servicio y le "desvinculamos" el auto de la sesión actual
        conductor.setEstado(EstadoConductor.FUERA_DE_SERVICIO);
        conductor.setVehiculoActivo(null);
    }
    
    /**
     * solicita un vaije 
     * @param emailCliente
     * @param origen
     * @param destino
     * @param servicio
     * @return nuevo viaje
     */
    public Viaje solicitarViaje(String emailCliente, Ubicacion origen, Ubicacion destino, Servicio servicio){
    	//Buscar al usuario y verificar que exista
        Usuario cliente = usuariosRegistrados.get(emailCliente);
        if (cliente == null) {
            throw new IllegalArgumentException("El usuario con email " + emailCliente + " no existe.");
        }
    	
        //Instanciamos el viaje
        Viaje nuevoViaje = new Viaje(cliente, origen, destino, servicio);
        
        //Registramos el primer paso en el historial con la hora exacta
        nuevoViaje.solicitar(java.time.LocalDateTime.now());
        
        viajesRegistrados.put(nuevoViaje.getId(), nuevoViaje);
        
        List<Conductor> conductoresNotificados = buscarConductoresDisponibles(servicio);
        
        if (conductoresNotificados.isEmpty()) {
            throw new IllegalStateException("Lo sentimos, no hay conductores disponibles para tu viaje en este momento.");
        }
        
        // Si todo sale bien, retornamos el viaje para que la consola se lo muestre al cliente
        // y a los conductores para que alguno lo acepte.
        return nuevoViaje;
      
    }

    /**
     * METODO PRIVADO: Filtra a los conductores que esten disponibles
     */
    private List<Conductor> buscarConductoresDisponibles(Servicio servicioSolicitado) {
        
        List<Conductor> conductoresAptos = new ArrayList<>();
        
        // Recorremos todos los conductores registrados en el sistema
        for (Conductor conductor : conductoresRegistrados.values()) {
            
            //El conductor debe estar DISPONIBLE
            if (conductor.getEstado() != EstadoConductor.DISPONIBLE) {
                continue; 
            }
            
            Vehiculo auto = conductor.getVehiculoActivo();
            
            //El vehículo debe ser del mismo TIPO (ej: MOTO o AUTO)
            if (auto.getTipoVehiculo() != servicioSolicitado.getTipoVehiculo()) {
                continue;
            }
            
            //El vehículo debe ofrecer el TIPO DE SERVICIO (ej: ENVIOS o PASAJEROS)
            // (Usamos .contains() porque en Vehiculo tenés un Set<TipoServicio>)
            if (!auto.getTipoServicios().contains(servicioSolicitado.getTipoServicio())) {
                continue;
            }
            
            //La categoría del viaje no puede ser menor a lo que el conductor exige.
            // Ej: Si el conductor solo acepta PREMIUM (3), no le mandamos un viaje ESTANDAR (1).
            if (servicioSolicitado.getCategoriaVehiculo().getValor() < conductor.getCategoriaVehiculoActivo().getValor()) {
                continue;
            }
            
            // Si superó todas las pruebas, lo agregamos a la lista de notificados
            conductoresAptos.add(conductor);
        }
        
        return conductoresAptos;
    }
    
}