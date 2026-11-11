package md;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import md.aplicacion.ContextoAplicacion;
import md.datos.dao.DAOFactory;
import md.datos.dao.ServicioDAO;
import md.datos.dao.UsuarioDAO;
import md.datos.dao.VehiculoDAO;
import md.datos.dto.ServicioDTO;
import md.datos.dto.UsuarioDTO;
import md.datos.dto.VehiculoDTO;
import md.modelo.CategoriaVehiculo;
import md.modelo.EstadoConductor;
import md.modelo.RolUsuario;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import md.modelo.Usuario;
import net.datastructures.ProbeHashMap;

class ContextoAplicacionVialTest {

    @BeforeEach
    void limpiarContextoAntesDeCadaPrueba() {
        // El contexto es estático y puede conservar recursos entre pruebas.
        ContextoAplicacion.cerrar();
    }

    @AfterEach
    void cerrarContextoDespuesDeCadaPrueba() {
        ContextoAplicacion.cerrar();
    }

    @Test
    @DisplayName("1. No permite inicializar el contexto con una persistencia nula")
    void testRechazaPersistenciaNula() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ContextoAplicacion.inicializar(null)
        );
    }

    @Test
    @DisplayName("2. No permite obtener el motor vial antes de inicializar")
    void testMotorVialNoInicializado() {
        assertThrows(IllegalStateException.class,ContextoAplicacion::getMotorVial);
    }

    @Test
    @DisplayName("3. No permite obtener el generador antes de inicializar")
    void testGeneradorNoInicializado() {
        assertThrows(IllegalStateException.class,ContextoAplicacion::getGeneradorUbicaciones);
    }

    @Test
    @EnabledIfSystemProperty(named = "md.integration.contexto", matches = "true")
    @DisplayName("4. Inicializa los datos, el motor vial y el escenario de conductores")
    void testInicializacionCompletaDelContexto() throws Exception {
        Path osm = Path.of("mapas", "puerto_madryn_ampliado.osm");
        Path cache = Path.of("mapas", "graphhopper-cache-madryn-ampliado");

        assumeTrue(Files.isRegularFile(osm), "Falta el archivo OSM del proyecto.");
        assumeTrue(Files.isDirectory(cache), "Falta la caché GraphHopper preparada.");

        ContextoAplicacion.inicializar(new DAOFactoryFalso());

        assertNotNull(ContextoAplicacion.getDatosIniciales());
        assertNotNull(ContextoAplicacion.getMotorVial());
        assertNotNull(ContextoAplicacion.getGeneradorUbicaciones());

        Usuario conductor = ContextoAplicacion.getDatosIniciales()
                .getUsuarios().get("conductor@test.com");

        assertNotNull(conductor);
        assertEquals(RolUsuario.CONDUCTOR, conductor.getRolActivo());
        assertEquals(EstadoConductor.DISPONIBLE,
                conductor.getConductor().getEstadoConductor());
        assertNotNull(conductor.getConductor().getVehiculoActivo().getUbicacion());
    }

    @Test
    @DisplayName("5. Cerrar un contexto no inicializado es seguro")
    void testCerrarContextoNoInicializado() {
        assertDoesNotThrow(ContextoAplicacion::cerrar);
        assertDoesNotThrow(ContextoAplicacion::cerrar);

        assertThrows(IllegalStateException.class,ContextoAplicacion::getMotorVial
        );
    }

    /** Fábrica mínima para comprobar el armado del contexto sin depender de DAO reales. */
    private static class DAOFactoryFalso implements DAOFactory {

        private final ServicioDAO servicioDAO = () -> List.of(
                new ServicioDTO("Estandar", 2000, 200, 100,TipoVehiculo.AUTO,CategoriaVehiculo.ESTANDAR,
                        TipoServicio.PASAJEROS
                )
        );

        private final ProbeHashMap<String, VehiculoDTO> vehiculos = new ProbeHashMap<>();
        private final ProbeHashMap<String, UsuarioDTO> usuarios = new ProbeHashMap<>();

        DAOFactoryFalso() {
            vehiculos.put("AA111AA", new VehiculoDTO("AA111AA", "Toyota Corolla", 4,TipoVehiculo.AUTO,
                    CategoriaVehiculo.ESTANDAR,
                    Set.of(TipoServicio.PASAJEROS)));

            usuarios.put("conductor@test.com", new UsuarioDTO(
                    "Conductor de prueba",
                    "+5491100000000",
                    "conductor@test.com",
                    "LIC-TEST",
                    "AA111AA",
                    Set.of()
            ));
        }

        @Override
        public ServicioDAO getServicioDAO() {
            return servicioDAO;
        }

        @Override
        public VehiculoDAO getVehiculoDAO() {
            return () -> vehiculos;
        }

        @Override
        public UsuarioDAO getUsuarioDAO() {
            return () -> usuarios;
        }
    }
}
