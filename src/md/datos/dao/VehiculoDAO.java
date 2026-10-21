package md.datos.dao;

import md.modelo.Vehiculo;
import net.datastructures.ProbeHashMap;

import java.io.FileNotFoundException;

public interface VehiculoDAO {
    ProbeHashMap<String, Vehiculo> obtenerTodos() throws FileNotFoundException;

}
