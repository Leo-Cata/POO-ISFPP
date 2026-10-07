package md.test;

import java.util.List;

import md.datos.CargarDatos;
import md.datos.CargarParametros;
import md.modelo.Servicio;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import net.datastructures.ProbeHashMap;

public class PruebaCargarDatos {

	private static int aprobadas = 0;
	private static int falladas = 0;

	/** Registra el resultado de una verificacion y la muestra por consola. */
	private static void verificar(String descripcion, boolean condicion) {
		if (condicion) {
			aprobadas++;
			System.out.println("  OK     " + descripcion);
		} else {
			falladas++;
			System.out.println("  FALLO  " + descripcion);
		}
	}

	public static void main(String[] args) throws Exception {
		CargarParametros.parametros();

		System.out.println("== Servicios");
		List<Servicio> servicios = CargarDatos.cargarServicios(CargarParametros.getArchivoServicios());
		verificar("se cargan 6 servicios", servicios.size() == 6);

		System.out.println("== Vehiculos");
		ProbeHashMap<String, Vehiculo> vehiculos = CargarDatos.cargarVehiculos(CargarParametros.getArchivoVehiculos());
		verificar("se cargan 15 vehiculos", vehiculos.size() == 15);
		verificar("AB456DE tiene 2 tipos de servicio", vehiculos.get("AB456DE").getTipoServicios().size() == 2);

		System.out.println("== Usuarios");
		ProbeHashMap<String, Usuario> usuarios = CargarDatos.cargarUsuarios(CargarParametros.getArchivoUsuarios(), vehiculos);
		verificar("se cargan 50 usuarios", usuarios.size() == 50);

		// --- ejemplo del patron "debe fallar" ---
		System.out.println("== Archivo inexistente");
		try {
			CargarDatos.cargarServicios("no_existe.txt");
			verificar("archivo inexistente lanza FileNotFoundException", false); // si llega acá, NO lanzó nada
		} catch (java.io.FileNotFoundException e) {
			verificar("archivo inexistente lanza FileNotFoundException", true);
		}

		System.out.println("\nRESULTADO: " + aprobadas + " OK, " + falladas + " fallos");
	}
}