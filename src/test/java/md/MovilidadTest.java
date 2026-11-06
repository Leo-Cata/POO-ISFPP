package md;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import md.logica.Movilidad;
import md.modelo.CategoriaVehiculo;
import md.modelo.EstadoConductor;
import md.modelo.RolUsuario;
import md.modelo.Servicio;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Viaje;
import net.datastructures.ProbeHashMap;

class MovilidadTest {

    private ProbeHashMap<String, Usuario> usuariosRegistrados;
    private ProbeHashMap<String, Vehiculo> vehiculosRegistrados;

    private Servicio servicioSolicitado;
    private Ubicacion origen;
    private Movilidad movilidad;

    @BeforeEach
    void setUp() {

        usuariosRegistrados = new ProbeHashMap<>();
        vehiculosRegistrados = new ProbeHashMap<>();

        servicioSolicitado = new Servicio(
                "Confort",
                2000,
                300,
                100,
                TipoVehiculo.AUTO,
                CategoriaVehiculo.CONFORT,
                TipoServicio.PASAJEROS
        );


        origen = new Ubicacion(-42.76, -65.03);

        movilidad = new Movilidad(usuariosRegistrados);
    }

    /**
     * Crea un conductor y lo registra en las colecciones del sistema.
     */
    private Usuario crearConductor(
            String email,
            String patente,
            TipoVehiculo tipoVehiculo,
            CategoriaVehiculo categoriaMaxima,
            CategoriaVehiculo categoriaMinima,
            TipoServicio tipoServicio,
            EstadoConductor estado,
            Ubicacion ubicacion) {

        Vehiculo vehiculo = new Vehiculo(
                patente,
                "Vehiculo de prueba",
                4,
                tipoVehiculo,
                categoriaMaxima,
                tipoServicio
        );

        vehiculo.setUbicacion(ubicacion);

        Usuario usuario = new Usuario("Conductor de prueba","2800000000",email);

        usuario.altaConductor("LIC-" + email, vehiculo);
        usuario.cambiarRolActivo(RolUsuario.CONDUCTOR);

        usuario.getConductor().setEstado(estado);
        usuario.getConductor().setCategoriaVehiculoActivo(categoriaMinima);

        usuariosRegistrados.put(email, usuario);
        vehiculosRegistrados.put(patente, vehiculo);

        return usuario;
    }

    @Test
    @DisplayName("Obtiene únicamente los clientes disponibles")
    void testObtenerClientesDisponibles() {

        Usuario clienteDisponible = new Usuario(
                "Ana",
                "2800000001",
                "ana@email.com"
        );

        Usuario clienteOcupado = new Usuario(
                "Pedro",
                "2800000002",
                "pedro@email.com"
        );

        // El cliente tiene un viaje activo.
        Viaje viaje = new Viaje(
                clienteOcupado,
                origen,
                new Ubicacion(-42.78, -65.05),
                servicioSolicitado
        );

        viaje.solicitar(LocalDateTime.now());

        // Este usuario tiene perfil de conductor y rol activo CONDUCTOR.
        crearConductor(
                "conductor@email.com",
                "AA111AA",
                TipoVehiculo.AUTO,
                CategoriaVehiculo.PREMIUM,
                CategoriaVehiculo.ESTANDAR,
                TipoServicio.PASAJEROS,
                EstadoConductor.DISPONIBLE,
                origen
        );

        usuariosRegistrados.put(clienteDisponible.getEmail(), clienteDisponible);
        usuariosRegistrados.put(clienteOcupado.getEmail(), clienteOcupado);

        List<Usuario> resultado = movilidad.obtenerClientesDisponibles();

        assertEquals(1, resultado.size());
        assertSame(clienteDisponible, resultado.get(0));
    }

    @Test
    @DisplayName("Obtiene únicamente los conductores compatibles")
    void testObtenerConductoresCompatibles() {

        Usuario compatible = crearConductor(
                "compatible@email.com",
                "AA111AA",
                TipoVehiculo.AUTO,
                CategoriaVehiculo.PREMIUM,
                CategoriaVehiculo.ESTANDAR,
                TipoServicio.PASAJEROS,
                EstadoConductor.DISPONIBLE,
                origen
        );

        // No está disponible.
        crearConductor(
                "ocupado@email.com",
                "AA222AA",
                TipoVehiculo.AUTO,
                CategoriaVehiculo.PREMIUM,
                CategoriaVehiculo.ESTANDAR,
                TipoServicio.PASAJEROS,
                EstadoConductor.FUERA_DE_SERVICIO,
                origen
        );

        // Tiene otro tipo de vehículo.
        crearConductor(
                "moto@email.com",
                "AA333AA",
                TipoVehiculo.MOTO,
                CategoriaVehiculo.PREMIUM,
                CategoriaVehiculo.ESTANDAR,
                TipoServicio.PASAJEROS,
                EstadoConductor.DISPONIBLE,
                origen
        );

        // Ofrece otro tipo de servicio.
        crearConductor(
                "envios@email.com",
                "AA444AA",
                TipoVehiculo.AUTO,
                CategoriaVehiculo.PREMIUM,
                CategoriaVehiculo.ESTANDAR,
                TipoServicio.ENVIOS,
                EstadoConductor.DISPONIBLE,
                origen
        );

        // La categoría máxima del vehículo es inferior a la solicitada.
        crearConductor(
                "categoriaMaxima@email.com",
                "AA555AA",
                TipoVehiculo.AUTO,
                CategoriaVehiculo.ESTANDAR,
                CategoriaVehiculo.ESTANDAR,
                TipoServicio.PASAJEROS,
                EstadoConductor.DISPONIBLE,
                origen
        );

        // El conductor no acepta categorías inferiores a PREMIUM.
        crearConductor(
                "categoriaMinima@email.com",
                "AA666AA",
                TipoVehiculo.AUTO,
                CategoriaVehiculo.PREMIUM,
                CategoriaVehiculo.PREMIUM,
                TipoServicio.PASAJEROS,
                EstadoConductor.DISPONIBLE,
                origen
        );

        List<Usuario> resultado =movilidad.obtenerConductoresCompatibles(servicioSolicitado);

        assertEquals(1, resultado.size());
        assertSame(compatible, resultado.get(0));
    }

    @Test
    @DisplayName("Selecciona al conductor compatible más cercano")
    void testSeleccionarConductorMasCercano() {

        crearConductor(
                "lejano@email.com",
                "AA111AA",
                TipoVehiculo.AUTO,
                CategoriaVehiculo.PREMIUM,
                CategoriaVehiculo.ESTANDAR,
                TipoServicio.PASAJEROS,
                EstadoConductor.DISPONIBLE,
                new Ubicacion(-42.77, -65.03)
        );

        Usuario conductorCercano = crearConductor(
                "cercano@email.com",
                "AA222AA",
                TipoVehiculo.AUTO,
                CategoriaVehiculo.PREMIUM,
                CategoriaVehiculo.ESTANDAR,
                TipoServicio.PASAJEROS,
                EstadoConductor.DISPONIBLE,
                new Ubicacion(-42.7601, -65.03)
        );

        Usuario resultado = movilidad.seleccionarConductor(servicioSolicitado,origen);

        assertSame(conductorCercano, resultado);
    }

    @Test
    @DisplayName("Devuelve null cuando no existen conductores compatibles")
    void testSeleccionarConductorSinCandidatos() {

        crearConductor(
                "moto@email.com",
                "AA111AA",
                TipoVehiculo.MOTO,
                CategoriaVehiculo.PREMIUM,
                CategoriaVehiculo.ESTANDAR,
                TipoServicio.PASAJEROS,
                EstadoConductor.DISPONIBLE,
                origen
        );

        Usuario resultado = movilidad.seleccionarConductor(servicioSolicitado,origen);

        assertNull(resultado);
    }

    @Test
    @DisplayName("Informa si un conductor compatible no tiene ubicación")
    void testSeleccionarConductorSinUbicacion() {

        crearConductor(
                "sinubicacion@email.com",
                "AA111AA",
                TipoVehiculo.AUTO,
                CategoriaVehiculo.PREMIUM,
                CategoriaVehiculo.ESTANDAR,
                TipoServicio.PASAJEROS,
                EstadoConductor.DISPONIBLE,
                null
        );

        assertThrows(IllegalStateException.class,() -> movilidad.seleccionarConductor(servicioSolicitado, origen));
    }

    @Test
    @DisplayName("Rechaza un servicio nulo al buscar conductores")
    void testConductoresCompatiblesServicioNulo() {

        assertThrows(IllegalArgumentException.class,() -> movilidad.obtenerConductoresCompatibles(null));
    }

    @Test
    @DisplayName("Rechaza un origen nulo al seleccionar conductor")
    void testSeleccionarConductorOrigenNulo() {

        assertThrows(IllegalArgumentException.class,() -> movilidad.seleccionarConductor(servicioSolicitado, null)
        );
    }
}
