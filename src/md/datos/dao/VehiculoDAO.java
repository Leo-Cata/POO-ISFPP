package md.datos.dao;

import md.datos.dto.VehiculoDTO; 
import net.datastructures.ProbeHashMap;

import java.io.FileNotFoundException;

public interface VehiculoDAO {
    ProbeHashMap<String, VehiculoDTO> obtenerTodos() throws FileNotFoundException;

}
