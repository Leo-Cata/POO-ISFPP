package md.datos.dao;

import java.io.FileNotFoundException;
import java.util.List;
import md.modelo.Servicio;

public interface ServicioDAO {
    List<Servicio> obtenerTodos() throws FileNotFoundException;
}
