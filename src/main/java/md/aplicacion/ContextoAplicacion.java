package md.aplicacion;

import java.io.FileNotFoundException; 

import md.datos.dao.DAOException;
import md.datos.dao.DAOFactory;
import md.modelo.Usuario;
import md.rutas.api.PlanificadorRutas;
import md.rutas.impl.PlanificadorRutasFactory;
import md.simulacion.GeneradorUbicacionesViales;
import md.simulacion.InicializadorEscenario;

/**
 * Mantiene disponibles los datos iniciales de la aplicación para que los
 * distintos componentes puedan acceder a ellos.
 */
public class ContextoAplicacion {
	private static CargadorModelo.DatosIniciales datos;
	private static Usuario usuarioLogeado; // mantiene el usuario logeado
	private static PlanificadorRutas motorVial; //conserva la instancia del motor de rutas que creamos mediante PlanificadorRutasFactory
	private static GeneradorUbicacionesViales generadorUbicaciones; //conserva el generador que utiliza ese motor para producir ubicaciones válidas

	
    /**
     * Carga los datos iniciales y prepara los recursos viales
     * necesarios para iniciar la aplicación.
     */
	public static void inicializar(DAOFactory persistencia) throws DAOException, FileNotFoundException {

		  if (persistencia == null) {throw new IllegalArgumentException("La persistencia no puede ser null.");
	        }

	        if (motorVial != null) {
	            throw new IllegalStateException("El contexto ya fue inicializado.");
	        }
		
	        CargadorModelo.DatosIniciales nuevosDatos = new CargadorModelo(persistencia).cargar();
	        PlanificadorRutas nuevoMotor = null;
		
	        try {

	            // Creamos el motor vial configurado.
	            nuevoMotor = PlanificadorRutasFactory.crear();

	            // Creamos el generador utilizando el motor compartido.
	            GeneradorUbicacionesViales nuevoGenerador =
	                    new GeneradorUbicacionesViales(nuevoMotor);

	            // Preparamos las ubicaciones y el estado de los conductores.
	            InicializadorEscenario inicializador =
	                    new InicializadorEscenario(nuevoGenerador);

	            inicializador.inicializar(nuevosDatos.getUsuarios());

	            // Conservamos los recursos para el resto de la aplicación.
	            datos = nuevosDatos;
	            motorVial = nuevoMotor;
	            generadorUbicaciones = nuevoGenerador;

	        } catch (RuntimeException e) {

	            // Si falla la inicialización, liberamos el motor creado.
	            if (nuevoMotor != null) {
	                try {
	                    nuevoMotor.close();
	                } catch (RuntimeException errorCierre) {
	                    e.addSuppressed(errorCierre);
	                }
	            }

	            throw e;
	        }
	}

	public static CargadorModelo.DatosIniciales getDatosIniciales() {
        return datos;
    }

    public static Usuario getUsuarioLogeado() {
        return usuarioLogeado;
    }

    public static void setUsuarioLogeado(Usuario usuario) {
        usuarioLogeado = usuario;
    }

    public static PlanificadorRutas getMotorVial() {

        if (motorVial == null) {
            throw new IllegalStateException("El motor vial todavía no fue inicializado.");
        }

        return motorVial;
    }

    public static GeneradorUbicacionesViales getGeneradorUbicaciones() {

        if (generadorUbicaciones == null) {
            throw new IllegalStateException("El generador de ubicaciones todavía no fue inicializado.");
        }

        return generadorUbicaciones;
    }

    /**
     * Cierra el motor vial y libera sus recursos.
     */
    public static void cerrar() {

        if (motorVial != null) {
            try {
                motorVial.close();
            } finally {
                motorVial = null;
                generadorUbicaciones = null;
            }
        }
    }
}
