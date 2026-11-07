package md.ui.api;

import java.util.function.BiConsumer;

import md.modelo.Ubicacion;
import md.rutas.api.RutasViaje;

public interface VisualizadorMapa {

    // dibuja los tramos del viaje y los marcadores del conductor, origen y destino
    void mostrar(RutasViaje rutas, Ubicacion conductor, Ubicacion origen, Ubicacion destino);

    // registra el metodo callback que recibe las coords al hacer click en el mapa
    void configurarSeleccion(BiConsumer<String, Ubicacion> receptor);

    // Solicita al visor esperar un clic del tipo ("origen" o "destino")
    void activarSeleccion(String tipo);

    // Dibuja el marcador individual confirmado tras ser validado
    void mostrarMarcadorSeleccion(String tipo, Ubicacion ubicacion);

    // Cancela la espera del clic en el mapa
    void cancelarSeleccion();

    // Elimina los marcadores temporales de origen y destino
    void limpiarSeleccion();

    // Elimina las líneas de ruta trazadas anteriormente
    void limpiarRutas();
}
