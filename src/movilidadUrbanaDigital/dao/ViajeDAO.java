package movilidadUrbanaDigital.dao;

import movilidadUrbanaDigital.*;
import java.util.List;


/**
 * Viaje no tiene un atributo identificador propio, por eso 
 * no extiende de DAO<T, ID>. Para actualizar() se usa Viaje.equals()
 * (fechaHora + conductor + cliente) para encontrar cual registro reemplazar.
 */
public interface ViajeDAO {
    void guardar(Viaje viaje);
    List<Viaje> buscarTodos();
    void actualizar(Viaje viaje);
}
