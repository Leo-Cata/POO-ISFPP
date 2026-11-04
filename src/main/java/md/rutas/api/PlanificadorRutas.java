package md.rutas.api;

import java.util.Optional;

import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;

/**
 * Contrato del motor de rutas: QUE se puede pedir, sin decir COMO se resuelve.
 * Cualquier motor (GraphHopper u otro) que lo cumpla puede reemplazar a otro.
 */
public interface PlanificadorRutas extends AutoCloseable {

    /**
     * Calcula el recorrido por calles entre dos ubicaciones.
     *
     * @throws RutaNoDisponibleException si no existe un camino entre los dos puntos
     */
    RutaCalculada calcular(Ubicacion origen, Ubicacion destino, TipoVehiculo vehiculo)
            throws RutaNoDisponibleException;

    /**
     * Busca la posicion transitable mas cercana a una coordenada ("snapping").
     *
     * @return la ubicacion sobre una calle, o vacio si no hay ninguna dentro de la distancia maxima
     */
    Optional<Ubicacion> ajustarACalle(Ubicacion candidata, double maxDistanciaMetros);

    // Se redeclara SIN "throws Exception" para que try-with-resources no obligue a capturarla
    @Override
    void close();
}
