package md.logica;

import java.util.ArrayList;
import java.util.List;

import md.modelo.Conductor;
import md.modelo.EstadoConductor;
import md.modelo.RolUsuario;
import md.modelo.Servicio;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import net.datastructures.ProbeHashMap;

public class Movilidad {

    private final ProbeHashMap<String, Usuario> usuariosRegistrados;

    public Movilidad(ProbeHashMap<String, Usuario> usuariosRegistrados) {
        if (usuariosRegistrados == null) {
            throw new IllegalArgumentException("Lista de usuarios no puede ser null.");
        }
        this.usuariosRegistrados = usuariosRegistrados;
    }

    /**
     * Obtiene los clientes que pueden solicitar un viaje.
     *
     * @return clientes disponibles
     */
    public List<Usuario> obtenerClientesDisponibles() {

        List<Usuario> clientesDisponibles = new ArrayList<Usuario>();

        for (Usuario u : usuariosRegistrados.values()) {
            if (u.getRolActivo() == RolUsuario.CLIENTE && u.getCliente() != null
                    && !u.getCliente().enViaje()) {
                clientesDisponibles.add(u);
            }
        }

        return clientesDisponibles;
    }

    /**
     * Obtiene los usuarios que tienen un perfil de conductor disponible
     * y cumplen con los requisitos del servicio solicitado.
     *
     * @param servicioSolicitado servicio que necesita el cliente.
     * @return lista de usuarios con conductores compatibles.
     * @throws IllegalArgumentException si el servicio es null.
     */
    public List<Usuario> obtenerConductoresCompatibles(Servicio servicioSolicitado) {

        if (servicioSolicitado == null) {
            throw new IllegalArgumentException("El servicio no puede ser null.");
        }

        List<Usuario> conductoresCompatibles = new ArrayList<Usuario>();
        for (Usuario u : usuariosRegistrados.values()) {
            if (esConductorCompatible(u, servicioSolicitado)) {
                conductoresCompatibles.add(u);
            }
        }

        return conductoresCompatibles;
    }

    // Las mismas condiciones que Viaje.aceptar exige para aceptar un viaje
    private boolean esConductorCompatible(Usuario u, Servicio servicioSolicitado) {

        if (u.getRolActivo() != RolUsuario.CONDUCTOR) {
            return false;
        }

        Conductor conductor = u.getConductor();

        if (conductor == null || conductor.getEstadoConductor() != EstadoConductor.DISPONIBLE) {
            return false;
        }

        Vehiculo vehiculo = conductor.getVehiculoActivo();

        if (vehiculo == null) {
            return false;
        }

        boolean tipoVehiculoCoincide = vehiculo.getTipoVehiculo() == servicioSolicitado.getTipoVehiculo();

        boolean tipoServicioCoincide = vehiculo.getTipoServicios().contains(servicioSolicitado.getTipoServicio());

        int categoriaSolicitada = servicioSolicitado.getCategoriaVehiculo().getValor();
        int categoriaMinima = conductor.getCategoriaVehiculoActivo().getValor();
        int categoriaMaxima = vehiculo.getCategoriaVehiculo().getValor();

        boolean categoriaCompatible = categoriaSolicitada >= categoriaMinima && categoriaSolicitada <= categoriaMaxima;

        return tipoVehiculoCoincide && tipoServicioCoincide && categoriaCompatible;
    }

    /**
     * Selecciona el conductor compatible más cercano al origen solicitado.
     * Si dos conductores están exactamente a la misma distancia gana el de email menor,
     * para que el resultado no dependa del orden en que el mapa recorre los usuarios.
     *
     * @param servicioSolicitado servicio requerido.
     * @param origen ubicación de recogida del cliente.
     * @return usuario del conductor seleccionado, o null si no hay candidatos.
     * @throws IllegalArgumentException si algún parámetro es null.
     * @throws IllegalStateException si un vehículo candidato no tiene ubicación.
     */
    public Usuario seleccionarConductor(Servicio servicioSolicitado, Ubicacion origen) {

        if (servicioSolicitado == null) {
            throw new IllegalArgumentException("El servicio no puede ser null.");
        }

        if (origen == null) {
            throw new IllegalArgumentException("La ubicacion de origen no puede ser null.");
        }

        List<Usuario> conductoresCandidatos = obtenerConductoresCompatibles(servicioSolicitado);

        Usuario conductorSeleccionado = null;
        double menorDistancia = Double.POSITIVE_INFINITY;

        for (Usuario u : conductoresCandidatos) {

            Vehiculo vehiculo = u.getConductor().getVehiculoActivo();

            Ubicacion ubicacionConductor = vehiculo.getUbicacion();

            if (ubicacionConductor == null) {
                throw new IllegalStateException("El vehículo del conductor " + u.getEmail()
                        + " no tiene una ubicación inicial asignada.");
            }

            double distancia = ubicacionConductor.calcularDistanciaA(origen);

            if (conductorSeleccionado == null || distancia < menorDistancia
                    || (distancia == menorDistancia && u.getEmail().compareTo(conductorSeleccionado.getEmail()) < 0)) {
                menorDistancia = distancia;
                conductorSeleccionado = u;
            }
        }

        return conductorSeleccionado;
    }
}
