package md;

import md.aplicacion.CargadorModelo;
import md.aplicacion.CargadorModelo.DatosIniciales;
import md.datos.dao.DAOFactory;
import md.datos.dao.DAOLoader;
import md.modelo.Conductor;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import net.datastructures.Entry;

/**
 * Clase de prueba para verificar el correcto funcionamiento del proceso de
 * carga del modelo a través de {@link CargadorModelo}.
 */
public class CargadorModeloTest {

	/**
	 * Punto de entrada de la prueba. Inicializa la fábrica de DAOs, ejecuta la
	 * carga del modelo y muestra por consola un resumen de los objetos construidos,
	 * incluyendo servicios, vehículos, usuarios y conductores con sus datos
	 * asociados. En caso de error, imprime el rastro de la excepción por la salida
	 * de error estándar.
	 *
	 * @param args argumentos de línea de comandos (no se utilizan en esta prueba).
	 */
	public static void main(String[] args) {

		System.out.println("=== PRUEBA DE CARGA DEL MODELO ===");

		try {
			DAOFactory fabrica = DAOLoader.getFactory();

			CargadorModelo cargador = new CargadorModelo(fabrica);

			DatosIniciales datos = cargador.cargar();

			System.out.println("Servicios construidos: " + datos.getServicios().size());

			System.out.println("Vehículos construidos: " + datos.getVehiculos().size());

			System.out.println("Usuarios construidos: " + datos.getUsuarios().size());

			int cantidadConductores = 0;

			for (Entry<String, Usuario> entrada : datos.getUsuarios().entrySet()) {

				Usuario usuario = entrada.getValue();
				Conductor conductor = usuario.getConductor();

				if (conductor != null) {
					cantidadConductores++;

					Vehiculo principal = conductor.getVehiculoActivo();

					System.out.println("Conductor: " + usuario.getEmail());

					System.out.println("  Licencia: " + conductor.getLicenciaConducir());

					System.out.println("  Vehículo activo: " + principal.getPatente());

					System.out.println("  Vehículos registrados: " + conductor.getVehiculos().size());
				}
			}

			System.out.println("Conductores construidos: " + cantidadConductores);

			System.out.println("=== PRUEBA FINALIZADA ===");

		} catch (Exception e) {
			System.err.println("Error durante la carga del modelo:");
			e.printStackTrace();
		}
	}
}
