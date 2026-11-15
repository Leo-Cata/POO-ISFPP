package md.logica;

import md.modelo.Conductor;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import net.datastructures.ProbeHashMap;

/**
 * Asigna ubicaciones iniciales a los vehículos de los conductores.
 */
public class InicializadorVehiculos {

    private final GeneradorUbicacionesViales generador;

    public InicializadorVehiculos(GeneradorUbicacionesViales generador) {
        if (generador == null) {
            throw new IllegalArgumentException("El generador de ubicaciones no puede ser null.");
        }

        this.generador = generador;
    }

    /**
     * Genera y asigna una ubicación inicial a cada vehículo registrado.
     *
     * @param usuarios usuarios cargados en el sistema.
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

            if (conductor == null) {
                continue;
            }

            if (conductor.getVehiculos().isEmpty()) {
                throw new IllegalStateException("El conductor " + usuario.getEmail()
                        + " no tiene vehículos registrados.");
            }

            for (Vehiculo vehiculo : conductor.getVehiculos()) {

                if (vehiculo == null) {
                    throw new IllegalStateException("El conductor " + usuario.getEmail()
                            + " tiene un vehículo null.");
                }

                Ubicacion ubicacion = generador.generar();
                vehiculo.setUbicacion(ubicacion);
            }
        }
    }
}
