package md;

import md.datos.dao.DAOFactory;
import md.datos.dao.DAOLoader;
import md.datos.dto.ServicioDTO;
import md.datos.dto.UsuarioDTO;
import md.datos.dto.VehiculoDTO;
import java.util.List;
import net.datastructures.ProbeHashMap;

public class DAOTest {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   INICIANDO PRUEBA DE LA CAPA DAO CON PRINTS     ");
        System.out.println("==================================================");

        try {
            // 1. Obtener la fábrica abstracta a través del DAOLoader (lee
            // config.properties)
            DAOFactory factory = DAOLoader.getFactory();
            System.out.println("[OK] Fábrica DAO instanciada correctamente.");

            // 2. Probar y mostrar la carga de Servicios
            System.out.println("\n--------------------------------------------------");
            System.out.println("--> 1. PROBANDO ServicioDAO (servicios.txt)");
            System.out.println("--------------------------------------------------");
            List<ServicioDTO> servicios = factory.getServicioDAO().obtenerTodos();
            System.out.println("Total de servicios leídos: " + servicios.size());

            for (int i = 0; i < servicios.size(); i++) {
                ServicioDTO s = servicios.get(i);
                System.out.printf("  [%d] Nombre: %s | Tarifa Base: $%.2f | Precio/Km: $%.2f | Precio/Min: $%.2f\n",
                        (i + 1), s.getNombre(), s.getTarifaBase(), s.getPrecioPorKm(), s.getPrecioPorMinuto());
            }

            // 3. Probar y mostrar la carga de Vehículos
            System.out.println("\n--------------------------------------------------");
            System.out.println("--> 2. PROBANDO VehiculoDAO (vehiculos.txt)");
            System.out.println("--------------------------------------------------");
            ProbeHashMap<String, VehiculoDTO> vehiculos = factory.getVehiculoDAO().obtenerTodos();
            System.out.println("Total de vehículos cargados en el mapa: " + vehiculos.size());
            // Nota: Aquí puedes imprimir detalles adicionales de los vehículos si tus
            // métodos getter lo permiten.

            // 4. Probar y mostrar la carga de Usuarios (pasando el mapa de vehículos)
            System.out.println("\n--------------------------------------------------");
            System.out.println("--> 3. PROBANDO UsuarioDAO (usuarios.txt)");
            System.out.println("--------------------------------------------------");
            ProbeHashMap<String, UsuarioDTO> usuarios = factory.getUsuarioDAO().obtenerTodos();
            System.out.println("Total de usuarios cargados en el mapa: " + usuarios.size());

            // 5. Verificar los parámetros geográficos levantados del config.properties
            System.out.println("\n--------------------------------------------------");
            System.out.println("--> 4. PARÁMETROS GEOGRÁFICOS DE CONFIGURACIÓN");
            System.out.println("--------------------------------------------------");
            System.out.printf("  - Latitud Mínima:  %.4f\n", DAOLoader.getLatitudMin());
            System.out.printf("  - Latitud Máxima:  %.4f\n", DAOLoader.getLatitudMax());
            System.out.printf("  - Longitud Mínima: %.4f\n", DAOLoader.getLongitudMin());
            System.out.printf("  - Longitud Máxima: %.4f\n", DAOLoader.getLongitudMax());

            System.out.println("\n==================================================");
            System.out.println("   ¡TODAS LAS PRUEBAS FINALIZARON CON ÉXITO!      ");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("\n[ERROR CRÍTICO] Ocurrió un error durante la ejecución:");
            e.printStackTrace();
        }
    }
}