package md.datos.dao;

import md.datos.dto.VehiculoDTO;
import net.datastructures.ProbeHashMap;

public interface VehiculoDAO {
    /**
     * Obtiene todos los vehículos registrados desde la persistencia.
     *
     * @return mapa con los vehículos como objetos de transferencia
     *         ({@link VehiculoDTO}),
     *         indexados por su clave.
     * @throws DAOException si ocurre un error al acceder a la fuente de datos.
     */
    ProbeHashMap<String, VehiculoDTO> obtenerTodos() throws DAOException;

}
