package md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import md.ui.api.VisualizadorMapa;
import md.ui.impl.javafx.map.VisualizadorRutasLeaflet;

class VisualizadorMapaContratoTest {

    private static final Set<String> OPERACIONES_ESPERADAS = Set.of(
            "mostrar",
            "configurarSeleccion",
            "activarSeleccion",
            "mostrarMarcadorSeleccion",
            "cancelarSeleccion",
            "limpiarSeleccion",
            "limpiarRutas",
            "limpiarMarcador"
    );

    @Test
    @DisplayName("1. Leaflet implementa el contrato VisualizadorMapa")
    void testLeafletImplementaContrato() {
        assertTrue(
                VisualizadorMapa.class.isAssignableFrom(VisualizadorRutasLeaflet.class)
        );
    }

    @Test
    @DisplayName("2. La implementación Leaflet es final")
    void testImplementacionLeafletEsFinal() {
        assertTrue(Modifier.isFinal(VisualizadorRutasLeaflet.class.getModifiers()));
    }

    @Test
    @DisplayName("3. El contrato declara las ocho operaciones públicas del visor")
    void testOperacionesDelContrato() {
        Set<String> operacionesDeclaradas = Arrays.stream(
                        VisualizadorMapa.class.getDeclaredMethods())
                .map(method -> method.getName())
                .collect(Collectors.toSet());

        assertEquals(8, operacionesDeclaradas.size());
        assertEquals(OPERACIONES_ESPERADAS, operacionesDeclaradas);
    }
}
