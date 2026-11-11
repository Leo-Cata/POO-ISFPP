
package md;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VisualizadorRutasLeafletTest {

    @Test
    @DisplayName("1. El HTML del mapa está disponible como recurso")
    void testRecursoMapaExiste() {

        assertNotNull(
                getClass().getResource("/ui/map/mapa.html"),
                "Debe existir src/main/resources/ui/map/mapa.html."
        );
    }

    @Test
    @DisplayName("2. El HTML expone las funciones que invoca Java")
    void testFuncionesJavascriptDelContrato() throws IOException {

        String html = leerMapaHtml();

        assertTrue(html.contains("window.activarSeleccion = function"));
        assertTrue(html.contains("window.cancelarSeleccion = function"));
        assertTrue(html.contains("window.mostrarMarcadorSeleccion = function"));
        assertTrue(html.contains("window.mostrarRutas = function"));
        assertTrue(html.contains("window.limpiarSeleccion = function"));
        assertTrue(html.contains("window.limpiarRutas = function"));
        assertTrue(html.contains("window.limpiarMarcador = function"));
    }

    @Test
    @DisplayName("3. El HTML envía el clic del mapa mediante javaBridge")
    void testPuenteJavaScriptHaciaJava() throws IOException {

        String html = leerMapaHtml();

        assertTrue(html.contains("window.javaBridge"));
        assertTrue(html.contains("seleccionarPunto"));
        assertTrue(html.contains("e.latlng.lat"));
        assertTrue(html.contains("e.latlng.lng"));
    }


    @Test
    @DisplayName("4. Los archivos locales de Leaflet están incluidos")
    void testRecursosLocalesLeafletExisten() {

    assertNotNull(
            getClass().getResource("/ui/map/leaflet/leaflet.js"),
            "No se encontró leaflet.js en /ui/map/leaflet/"
    );

    assertNotNull(
            getClass().getResource("/ui/map/leaflet/leaflet.css"),
            "No se encontró leaflet.css en /ui/map/leaflet/"
    );
}


    private String leerMapaHtml() throws IOException {

        try (InputStream entrada =
                getClass().getResourceAsStream("/ui/map/mapa.html")) {

            if (entrada == null) {
                throw new IllegalStateException(
                        "No se encontró el recurso /ui/map/mapa.html."
                );
            }

            return new String(
                    entrada.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }
    }
}
