package md.simulacion;

import md.modelo.RolUsuario;
import md.modelo.Usuario;
import net.datastructures.ProbeHashMap;

/**
 * Prepara el estado inicial de los conductores para la simulación.
 */
public class InicializadorEscenario {

    /**
     * Activa el rol de conductor para los usuarios que tienen ese perfil.
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

            if (usuario.getConductor() == null) {
                continue;
            }

            usuario.cambiarRolActivo(RolUsuario.CONDUCTOR);
        }
    }
}
