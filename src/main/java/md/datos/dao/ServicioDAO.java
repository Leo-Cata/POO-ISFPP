package md.datos.dao;

import java.util.List;

import md.datos.dto.ServicioDTO;

public interface ServicioDAO {
    /**
     * Obtiene todos los servicios disponibles desde la persistencia.
     *
     * @return lista de servicios como objetos de transferencia
     *         ({@link ServicioDTO}).
     * @throws DAOException si ocurre un error al acceder a la fuente de datos.
     */
    List<ServicioDTO> obtenerTodos() throws DAOException;
}
