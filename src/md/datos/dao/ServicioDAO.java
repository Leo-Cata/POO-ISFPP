package md.datos.dao;

import java.io.FileNotFoundException; 
import java.util.List;

import md.datos.dto.ServicioDTO;

public interface ServicioDAO {
    List<ServicioDTO> obtenerTodos() throws FileNotFoundException;
}
