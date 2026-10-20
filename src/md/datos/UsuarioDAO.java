package md.datos;

import java.io.FileNotFoundException;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import net.datastructures.ProbeHashMap;

public interface UsuarioDAO {
    ProbeHashMap<String, Usuario> obtenerTodos(ProbeHashMap<String, Vehiculo> vehiculosCargados)
            throws FileNotFoundException;
}
