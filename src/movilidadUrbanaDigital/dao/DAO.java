package movilidadUrbanaDigital.dao;

import java.util.List;

/**
 * Interfaz generica de acceso a datos.
 * T  = tipo de la entidad (Cliente, Conductor, Vehiculo, etc.)
 * ID = tipo del identificador unico de esa entidad (en este proyecto, siempre String: documento o patente)
 */
public interface DAO<T, ID> {
    void guardar(T entidad);
    T buscarPorId(ID id);
    List<T> buscarTodos();
    void actualizar(T entidad);
    void eliminar(ID id);
}
