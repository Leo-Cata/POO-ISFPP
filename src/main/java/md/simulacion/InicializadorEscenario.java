
package md.simulacion;

import md.modelo.Conductor;
import md.modelo.RolUsuario;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import net.datastructures.ProbeHashMap;

public class InicializadorEscenario {

    private final GeneradorUbicacionesViales generador;

    public InicializadorEscenario(GeneradorUbicacionesViales generador) {

        if (generador == null) {
            throw new IllegalArgumentException("El generador de ubicaciones no puede ser null.");
        }

        this.generador = generador;
    }

    /**
     * Prepara el estado inicial y la ubicación de los conductores
     * cargados en el sistema.
     *
     * @param usuarios usuarios cargados desde los DAO.
     */
    public void inicializar(ProbeHashMap<String, Usuario> usuarios) {

        if (usuarios == null) {
            throw new IllegalArgumentException("El conjunto de usuarios no puede ser null.");
        }

        for (Usuario usuario : usuarios.values()) {

            if (usuario == null) {
                throw new IllegalStateException("Se encontró un usuario null en los datos iniciales.");
            }

            Conductor conductor = usuario.getConductor();

            // Los usuarios sin perfil de conductor no se inicializan.
            if (conductor == null) {
                continue;
            }

            if (conductor.getVehiculos().isEmpty()) {
                throw new IllegalStateException("El conductor " + usuario.getEmail()
                        + " no tiene vehículos registrados.");
            }

            // Inicializamos la ubicación de todos los vehículos del conductor.
            for (Vehiculo vehiculo : conductor.getVehiculos()) {

                if (vehiculo == null) {
                    throw new IllegalStateException("El conductor " + usuario.getEmail()
                            + " tiene un vehículo null.");
                }

                Ubicacion ubicacion = generador.generar();
                vehiculo.setUbicacion(ubicacion);
            }

            // Activamos el rol de conductor.
            // El cambio de rol también lo pone en estado DISPONIBLE.
            usuario.cambiarRolActivo(RolUsuario.CONDUCTOR);
        }
    }
}
