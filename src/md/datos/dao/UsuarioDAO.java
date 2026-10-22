package md.datos.dao;

import java.io.FileNotFoundException;

import md.datos.dto.UsuarioDTO;
import net.datastructures.ProbeHashMap;

public interface UsuarioDAO {
    ProbeHashMap<String, UsuarioDTO> obtenerTodos()
            throws FileNotFoundException;
}
