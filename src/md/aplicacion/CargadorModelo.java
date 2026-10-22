package md.aplicacion;

import java.io.FileNotFoundException; 
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import md.datos.dao.DAOFactory;
import md.datos.dto.ServicioDTO;
import md.datos.dto.UsuarioDTO;
import md.datos.dto.VehiculoDTO;
import md.modelo.Servicio;
import md.modelo.TipoServicio;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import net.datastructures.Entry;
import net.datastructures.ProbeHashMap;

public class CargadorModelo {
	// 1. Declaramos la dependencia como un atributo inmutable (final)
    private final DAOFactory factory;

    // 2. Inyectamos la dependencia a través del constructor
    public CargadorModelo(DAOFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("El DAOFactory no puede ser nulo");
        }
        this.factory = factory;
    }

	/**
	 * Carga los datos desde los DAO y construye el modelo de dominio.
	 * @return
	 * @throws FileNotFoundException
	 */
	public DatosIniciales cargar() throws FileNotFoundException {
		// 1. Leemos los DTO desde los archivos.
		//Se leen los tres conjuntos de datos. Después construimos los objetos del modelo.
        List<ServicioDTO> serviciosDTO =this.factory.getServicioDAO().obtenerTodos();

        ProbeHashMap<String, VehiculoDTO> vehiculosDTO =this.factory.getVehiculoDAO().obtenerTodos();

        ProbeHashMap<String, UsuarioDTO> usuariosDTO = this.factory.getUsuarioDAO().obtenerTodos();
		
     // 2. Convertimos los DTO de servicios en objetos del modelo.
        List<Servicio> servicios =
                convertirServicios(serviciosDTO);

        // 3. Construimos todos los vehículos antes que los usuarios.
        ProbeHashMap<String, Vehiculo> vehiculos =
                convertirVehiculos(vehiculosDTO);

        // 4. Construimos usuarios y resolvemos sus vehículos.
        ProbeHashMap<String, Usuario> usuarios =
                convertirUsuarios(usuariosDTO, vehiculos);

        // 5. Entregamos los datos ya preparados para la aplicación.
        return new DatosIniciales(servicios, vehiculos, usuarios);
    }
	
	private List<Servicio> convertirServicios(
            List<ServicioDTO> serviciosDTO) {

        List<Servicio> servicios = new ArrayList<>();

        for (ServicioDTO dto : serviciosDTO) {

            Servicio servicio = new Servicio(
                    dto.getNombre(),
                    dto.getTarifaBase(),
                    dto.getPrecioPorKm(),
                    dto.getPrecioPorMinuto(),
                    dto.getTipoVehiculo(),
                    dto.getCategoriaVehiculo(),
                    dto.getTipoServicio()
            );

            servicios.add(servicio);
        }

        return servicios;
    }

    private ProbeHashMap<String, Vehiculo> convertirVehiculos(
            ProbeHashMap<String, VehiculoDTO> vehiculosDTO) {

        ProbeHashMap<String, Vehiculo> vehiculos =
                new ProbeHashMap<>();

        for (Entry<String, VehiculoDTO> entrada
                : vehiculosDTO.entrySet()) {

            VehiculoDTO dto = entrada.getValue();
            Set<TipoServicio> tiposServicio =
                    dto.getTipoServicios();

            if (tiposServicio.isEmpty()) {
                throw new IllegalArgumentException(
                        "El vehículo " + dto.getPatente()
                        + " no tiene tipos de servicio.");
            }

            // El constructor de Vehiculo recibe un tipo de servicio.
            TipoServicio primerTipo =
                    tiposServicio.iterator().next();

            Vehiculo vehiculo = new Vehiculo(
                    dto.getPatente(),
                    dto.getModelo(),
                    dto.getCapacidadPasajeros(),
                    dto.getTipoVehiculo(),
                    dto.getCategoriaVehiculo(),
                    primerTipo
            );

            // Incorporamos los otros tipos de servicio, si existen.
            for (TipoServicio tipo : tiposServicio) {
                if (tipo != primerTipo) {
                    vehiculo.agregarTipoServicio(tipo);
                }
            }

            vehiculos.put(dto.getPatente(), vehiculo);
        }

        return vehiculos;
    }

    private ProbeHashMap<String, Usuario> convertirUsuarios(
            ProbeHashMap<String, UsuarioDTO> usuariosDTO,
            ProbeHashMap<String, Vehiculo> vehiculos) {

        ProbeHashMap<String, Usuario> usuarios =
                new ProbeHashMap<>();

        for (Entry<String, UsuarioDTO> entrada
                : usuariosDTO.entrySet()) {

            UsuarioDTO dto = entrada.getValue();

            Usuario usuario = new Usuario(
                    dto.getNombre(),
                    dto.getTelefono(),
                    dto.getEmail()
            );

            // Si no tiene licencia, queda registrado como cliente.
            if (dto.getLicencia() != null
                    && !dto.getLicencia().isEmpty()) {

                String patentePrincipal =
                        dto.getPatentePrincipal();

                Vehiculo principal = buscarVehiculo(
                        vehiculos,
                        patentePrincipal,
                        dto.getEmail()
                );

                // El alta crea el Conductor y registra su vehículo principal.
                usuario.altaConductor(dto.getLicencia(), principal);

                // Registramos los vehículos adicionales.
                for (String patente :
                        dto.getPatentesAdicionales()) {

                    if (patente.equals(patentePrincipal)) {
                        throw new IllegalArgumentException(
                                "El usuario " + dto.getEmail()
                                + " tiene la patente principal "
                                + "repetida como adicional: " + patente);
                    }

                    Vehiculo adicional = buscarVehiculo(
                            vehiculos,
                            patente,
                            dto.getEmail()
                    );

                    usuario.getConductor()
                            .agregarVehiculo(adicional);
                }
            }

            usuarios.put(dto.getEmail(), usuario);
        }

        return usuarios;
    }

    private Vehiculo buscarVehiculo(
            ProbeHashMap<String, Vehiculo> vehiculos,
            String patente,
            String emailUsuario) {

        if (patente == null || patente.isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario " + emailUsuario
                    + " no tiene una patente válida.");
        }

        Vehiculo vehiculo = vehiculos.get(patente);

        if (vehiculo == null) {
            throw new IllegalArgumentException(
                    "No existe el vehículo con patente " + patente
                    + ", referenciado por el usuario "
                    + emailUsuario + ".");
        }

        return vehiculo;
    }

    /**
     * Agrupa los objetos del modelo construidos durante la carga.
     */
    public static class DatosIniciales {

        private final List<Servicio> servicios;
        private final ProbeHashMap<String, Vehiculo> vehiculos;
        private final ProbeHashMap<String, Usuario> usuarios;

        public DatosIniciales(
                List<Servicio> servicios,
                ProbeHashMap<String, Vehiculo> vehiculos,
                ProbeHashMap<String, Usuario> usuarios) {

            this.servicios = servicios;
            this.vehiculos = vehiculos;
            this.usuarios = usuarios;
        }

        public List<Servicio> getServicios() {
            return servicios;
        }

        public ProbeHashMap<String, Vehiculo> getVehiculos() {
            return vehiculos;
        }

        public ProbeHashMap<String, Usuario> getUsuarios() {
            return usuarios;
        }
    }
}
