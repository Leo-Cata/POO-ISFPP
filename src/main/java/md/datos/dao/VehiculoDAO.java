package md.datos.dao;

import md.datos.dto.VehiculoDTO;  
import net.datastructures.ProbeHashMap;

public interface VehiculoDAO {
    ProbeHashMap<String, VehiculoDTO> obtenerTodos() throws DAOException;

}
