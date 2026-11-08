package md.ui.impl.javafx.map;

import java.net.URL;
import java.util.Locale;
import java.util.function.BiConsumer;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import md.modelo.Ubicacion;
import md.rutas.api.PuntoRuta;
import md.rutas.api.RutasViaje;
import md.ui.api.VisualizadorMapa;
import netscape.javascript.JSObject;

public final class VisualizadorRutasLeaflet implements VisualizadorMapa {
    private final WebView webView;
    private final WebEngine webEngine;
    private final PuenteSeleccionMapa puenteSeleccion;
    private boolean paginaLista = false;
    private String dibujoPendiente = null;
    private String modoSeleccionPendiente = null;

    public VisualizadorRutasLeaflet(WebView webView) {
        this.webView = webView;
        this.webEngine = webView.getEngine();
        this.puenteSeleccion = new PuenteSeleccionMapa();
        inicializar();
    }

    private void inicializar() {
        // Captura alertas o mensajes emergentes de JavaScript en la consola de Java
        webEngine.setOnAlert(event -> System.out.println("JS Alert: " + event.getData()));
        // Configura un User-Agent de navegador para evitar el bloqueo de OpenStreetMap

        // Identifica la aplicación ante el servidor de teselas de OpenStreetMap
        webEngine.setUserAgent("MovilidadDigitalApp/1.0 (contacto@midominio.edu.ar)");

        URL recurso = getClass().getResource("/ui/map/mapa.html");
        if (recurso == null) {
            System.err.println("Error: No se encontró el recurso /ui/map/mapa.html");
            return;
        }
        // Imprime excepciones de carga del WebEngine
        webEngine.getLoadWorker().exceptionProperty().addListener((obs, oldEx, newEx) -> {
            if (newEx != null) {
                System.err.println("Error en WebEngine: " + newEx.getMessage());
                newEx.printStackTrace();
            }
        });
        // Escucha el estado de carga del HTML
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                conectarPuente();
                paginaLista = true;
                aplicarPendientes();
            }
        });
        webEngine.load(recurso.toExternalForm());
    }

    private void conectarPuente() {
        try {
            // Expone el puente como "window.javaBridge" en JavaScript
            JSObject ventana = (JSObject) webEngine.executeScript("window");
            ventana.setMember("javaBridge", puenteSeleccion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void aplicarPendientes() {
        // Forza a Leaflet a recalcular su tamaño una vez que el HTML está listo
        ejecutarJS("if (window.refrescarTamano) window.refrescarTamano();");
        if (modoSeleccionPendiente != null) {
            ejecutarJS("window.activarSeleccion('" + modoSeleccionPendiente + "');");
            modoSeleccionPendiente = null;
        }
        if (dibujoPendiente != null) {
            ejecutarJS("window.mostrarRutas(" + dibujoPendiente + ");");
            dibujoPendiente = null;
        }
    }

    private void ejecutarJS(String script) {
        if (paginaLista) {
            Platform.runLater(() -> {
                try {
                    webEngine.executeScript(script);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void configurarSeleccion(BiConsumer<String, Ubicacion> receptor) {
        puenteSeleccion.registrarReceptor(receptor);
    }

    @Override
    public void activarSeleccion(String tipo) {
        if (!paginaLista) {
            modoSeleccionPendiente = tipo;
        } else {
            ejecutarJS("window.activarSeleccion('" + tipo + "');");
        }
    }

    @Override
    public void mostrarMarcadorSeleccion(String tipo, Ubicacion ubicacion) {
        if (ubicacion != null) {
            String script = String.format(Locale.US, "window.mostrarMarcadorSeleccion('%s', [%f, %f]);",
                    tipo, ubicacion.getLatitud(), ubicacion.getLongitud());
            ejecutarJS(script);
        }
    }

    @Override
    public void cancelarSeleccion() {
        ejecutarJS("window.cancelarSeleccion();");
    }

    @Override
    public void limpiarSeleccion() {
        ejecutarJS("window.limpiarSeleccion();");
    }

    @Override
    public void limpiarRutas() {
        ejecutarJS("window.limpiarRutas();");
    }

    @Override
    public void mostrar(RutasViaje rutas, Ubicacion conductor, Ubicacion origen, Ubicacion destino) {
        String json = aJson(rutas, conductor, origen, destino);
        if (!paginaLista) {
            dibujoPendiente = json;
        } else {
            ejecutarJS("window.mostrarRutas(" + json + ");");
        }
    }

    private String aJson(RutasViaje rutas, Ubicacion conductor, Ubicacion origen, Ubicacion destino) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"acercamiento\":[");
        if (rutas != null && rutas.acercamiento() != null) {
            boolean primero = true;
            for (PuntoRuta p : rutas.acercamiento().geometria()) {
                if (!primero)
                    sb.append(",");
                sb.append(String.format(Locale.US, "[%f,%f]", p.latitud(), p.longitud()));
                primero = false;
            }
        }
        sb.append("],");
        sb.append("\"traslado\":[");
        if (rutas != null && rutas.traslado() != null) {
            boolean primero = true;
            for (PuntoRuta p : rutas.traslado().geometria()) {
                if (!primero)
                    sb.append(",");
                sb.append(String.format(Locale.US, "[%f,%f]", p.latitud(), p.longitud()));
                primero = false;
            }
        }
        sb.append("],");
        sb.append(String.format(Locale.US, "\"conductor\":[%f,%f],",
                conductor != null ? conductor.getLatitud() : 0, conductor != null ? conductor.getLongitud() : 0));
        sb.append(String.format(Locale.US, "\"origen\":[%f,%f],",
                origen != null ? origen.getLatitud() : 0, origen != null ? origen.getLongitud() : 0));
        sb.append(String.format(Locale.US, "\"destino\":[%f,%f]",
                destino != null ? destino.getLatitud() : 0, destino != null ? destino.getLongitud() : 0));
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void refrescarTamano() {
        if (paginaLista) {
            webEngine.executeScript("window.refrescarTamano();");
        }
    }

    @Override
    public void limpiarMarcador(String tipo) {
        ejecutarJS("window.limpiarMarcador('" + tipo + "');");
    }
}