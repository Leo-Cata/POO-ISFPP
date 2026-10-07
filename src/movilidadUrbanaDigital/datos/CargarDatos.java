package movilidadUrbanaDigital.datos;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import movilidadUrbanaDigital.CategoriaVehiculo;
import movilidadUrbanaDigital.Servicio;
import movilidadUrbanaDigital.TipoServicio;
import movilidadUrbanaDigital.TipoVehiculo;
import movilidadUrbanaDigital.Ubicacion;
import movilidadUrbanaDigital.Usuario;
import movilidadUrbanaDigital.Vehiculo;
import net.datastructures.ProbeHashMap;

/**
 * Clase encargada de la lectura y carga de datos iniciales del sistema.
 * Procesa archivos de texto plano para instanciar y poblar las colecciones
 * de Servicios, Vehículos y Usuarios.
 */
public class CargarDatos {
    
    /**
     * Lee un archivo de texto y carga una lista de servicios disponibles.
     * 
     * @param fileName La ruta y nombre del archivo de texto de servicios.
     * @return Una lista (List) de objetos Servicio instanciados.
     * @throws FileNotFoundException Si no se encuentra el archivo en la ruta especificada.
     * @throws IllegalArgumentException Si una línea del archivo no tiene los datos suficientes o el formato es incorrecto.
     */
    public static List<Servicio> cargarServicios(String fileName) throws FileNotFoundException {
        List<Servicio> servicios = new ArrayList<Servicio>();
        File archivo = new File(fileName);
        
        // Uso de try-with-resources para asegurar el cierre automático del Scanner
        try (Scanner lector = new Scanner(archivo)) {
            while(lector.hasNextLine()) { 
                String linea = lector.nextLine();  
                
                // Evitar procesar líneas en blanco al final del archivo
                if (linea.trim().isEmpty()) {
                    continue; 
                }
                
                String[] partes = linea.split(";");
                
                if (partes.length < 7) {
                    throw new IllegalArgumentException("Línea inválida: " + linea);
                }
                
                // Aplicamos trim() por si el txt tiene espacios en blanco "Perdidos"
                String nombre = partes[0].trim();
                double tarifaBase = Double.parseDouble(partes[1].trim()); 
                double precioPorKm = Double.parseDouble(partes[2].trim()); 
                double precioPorMinuto = Double.parseDouble(partes[3].trim()); 
                
                // Convertimos a mayúsculas correctamente
             // Convertimos a mayúsculas correctamente
                String tipoVehiculoStr = partes[4].trim().toUpperCase();       
                String categoriaVehiculoStr = partes[5].trim().toUpperCase();
                String tipoServicioStr = partes[6].trim().toUpperCase();   
                
                // CONVERSIÓN A ENUMS:
                TipoServicio tipoServicio = TipoServicio.valueOf(tipoServicioStr);
                CategoriaVehiculo categoriaVehiculo = CategoriaVehiculo.valueOf(categoriaVehiculoStr);
                TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(tipoVehiculoStr);
                
                // Instanciamos pasándole los objetos Enum
                Servicio servicio = new Servicio(nombre, tarifaBase, precioPorKm, precioPorMinuto, tipoServicio, tipoVehiculo, categoriaVehiculo);
                
                servicios.add(servicio);
            }
        }
        
        return servicios;
    }

    /**
     * Lee un archivo de texto y carga los vehículos del sistema en un mapa.
     * Verifica que no se ingresen vehículos con la misma patente.
     * 
     * @param fileName La ruta y nombre del archivo de texto de vehículos.
     * @return Un ProbeHashMap donde la clave es la patente y el valor es el Vehiculo.
     * @throws FileNotFoundException Si no se encuentra el archivo en la ruta especificada.
     * @throws IllegalArgumentException Si faltan datos en una línea o si se detecta una patente duplicada.
     */
    public static ProbeHashMap<String,Vehiculo> cargarVehiculos(String fileName) throws FileNotFoundException {
        ProbeHashMap<String,Vehiculo> vehiculos = new ProbeHashMap<String,Vehiculo>();
        File archivo = new File(fileName);
        
        try (Scanner lector = new Scanner(archivo)) {
            while(lector.hasNextLine()) { 
                String linea = lector.nextLine(); 
                
                if (linea.trim().isEmpty()) {
                    continue; 
                }
                
                String[] partes = linea.split(";");
                
                // Un vehículo válido debe tener al menos 6 campos (índices 0 al 5)
                if (partes.length < 6) {
                    throw new IllegalArgumentException("Línea inválida (faltan datos): " + linea);
                }
                
                String patente = partes[0].trim();
                String modelo = partes[1].trim();
                int capacidadPasajeros = Integer.parseInt(partes[2].trim());
                
                TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(partes[3].trim().toUpperCase());
                CategoriaVehiculo categoriaVehiculo = CategoriaVehiculo.valueOf(partes[4].trim().toUpperCase());
                
                // Tomamos el primer servicio obligatorio (índice 5)
                TipoServicio tipoServicioPrincipal = TipoServicio.valueOf(partes[5].trim().toUpperCase());
                
                Ubicacion ubicacionInicial = null; 
                
                // Instanciamos el vehículo cumpliendo con el constructor
                Vehiculo vehiculo = new Vehiculo(patente, modelo, ubicacionInicial, tipoVehiculo, categoriaVehiculo, tipoServicioPrincipal, capacidadPasajeros);
                
                // Verificamos SI el array tiene una 7ma posición (índice 6) antes de intentar leerla
                if (partes.length > 6 && !partes[6].trim().isEmpty()) {
                    TipoServicio tipoServicioSecundario = TipoServicio.valueOf(partes[6].trim().toUpperCase());
                    vehiculo.agregarTipoServicio(tipoServicioSecundario);
                }
                
                // Verificamos si la patente ya existe
                if (vehiculos.get(patente) != null) {
                    throw new IllegalArgumentException("Vehículo duplicado encontrado en el archivo con patente : " + patente);
                }
                  
                vehiculos.put(patente, vehiculo);
            }
        }
        
        return vehiculos;
    }

    /**
     * Lee un archivo de texto, carga los usuarios y vincula a aquellos que son conductores
     * con sus vehículos correspondientes. Verifica que no se dupliquen correos electrónicos.
     * 
     * @param fileName La ruta y nombre del archivo de texto de usuarios.
     * @param vehiculosCargados El mapa de vehículos previamente cargados en memoria.
     * @return Un ProbeHashMap donde la clave es el email y el valor es el Usuario.
     * @throws FileNotFoundException Si no se encuentra el archivo en la ruta especificada.
     * @throws IllegalArgumentException Si faltan datos básicos, si hay emails duplicados o si se asigna una patente inexistente a un conductor.
     */
    public static ProbeHashMap<String,Usuario> cargarUsuarios(String fileName, ProbeHashMap<String, Vehiculo> vehiculosCargados) throws FileNotFoundException{
        ProbeHashMap<String,Usuario> usuarios = new ProbeHashMap<String,Usuario>();
        File archivo = new File(fileName);
        
        try (Scanner lector = new Scanner(archivo)) {
            while(lector.hasNextLine()) { 
                String linea = lector.nextLine(); 
                
                if (linea.trim().isEmpty()) {
                    continue; 
                }
                
                String[] partes = linea.split(";");
                
                if (partes.length < 3) {
                    throw new IllegalArgumentException("Línea inválida (faltan datos básicos): " + linea);
                }
                
                String nombre = partes[0].trim();
                String telefono = partes[1].trim();
                String email = partes[2].trim();
                
                Usuario usuario = new Usuario(nombre,telefono,email);
                
                // Verificamos si la línea tiene datos de conductor (licencia y al menos 1 patente)
                // Índice 3: Licencia | Índice 4: Patente principal
                if (partes.length >= 5 && !partes[3].trim().isEmpty()) {
                    String licencia = partes[3].trim();
                    String patentePrincipal = partes[4].trim();
                    
                    // Buscamos el vehículo en el mapa que pasamos por parámetro
                    Vehiculo vehiculoPrincipal = vehiculosCargados.get(patentePrincipal);
                    
                    if (vehiculoPrincipal == null) {
                        throw new IllegalArgumentException("Vehículo no encontrado para la patente: " + patentePrincipal);
                    }
                    
                    // Damos de alta al conductor con su primer vehículo
                    usuario.altaConductor(licencia, vehiculoPrincipal);
                    
                    // Si el array tiene más posiciones (índice 5 en adelante), cargamos los vehículos extra
                    for (int i = 5; i < partes.length; i++) {
                        String patenteExtra = partes[i].trim();
                        
                        if (!patenteExtra.isEmpty()) {
                            Vehiculo vehiculoExtra = vehiculosCargados.get(patenteExtra);
                            if (vehiculoExtra != null) {
                                usuario.getConductor().agregarVehiculo(vehiculoExtra);
                            }
                        }
                    }
                }
                
                // Verificamos si el email ya existe
                if (usuarios.get(email) != null) {
                    throw new IllegalArgumentException("Usuario duplicado encontrado en el archivo: " + email);
                }
                
                // Guardamos el usuario en el mapa usando su email como clave
                usuarios.put(email, usuario);
            }
        }
        
        return usuarios;
    }
}