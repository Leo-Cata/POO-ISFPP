package md.datos.dao;

import md.datos.dto.UsuarioDTO;
import net.datastructures.ProbeHashMap;

public interface UsuarioDAO {
    /**
     * Obtiene todos los usuarios registrados desde la persistencia.
     *
     * @return mapa con los usuarios como objetos de transferencia
     *         ({@link UsuarioDTO}),
     *         indexados por su clave.
     * @throws DAOException si ocurre un error al acceder a la fuente de datos.
     */
    ProbeHashMap<String, UsuarioDTO> obtenerTodos()
            throws DAOException;
}
