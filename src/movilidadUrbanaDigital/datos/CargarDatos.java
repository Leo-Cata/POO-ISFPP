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

public class CargarDatos {
	
	public static List<Servicio> cargarServicios(String fileName) throws FileNotFoundException {
	    
	    List<Servicio> servicios = new ArrayList<Servicio>();
	    File archivo = new File(fileName);
	    Scanner lector = new Scanner(archivo);
	    
	    while(lector.hasNextLine()) { 
	        String linea = lector.nextLine();  
	        
	        // Evitar procesar líneas en blanco al final del archivo
	        if (linea.trim().isEmpty()) {
	            continue; 
	        }
	        
	        String[] partes = linea.split(";");
	        
	        if (partes.length < 7) {
	            // Cerramos el lector antes de lanzar la excepción para no dejar el archivo abierto
	            lector.close();
	            throw new IllegalArgumentException("Línea inválida: " + linea);
	        }
	        
	        // Aplicamos trim() por si el txt tiene espacios en blanco " Perdidos "
	        String nombre = partes[0].trim();
	        double tarifaBase = Double.parseDouble(partes[1].trim()); 
	        double precioPorKm = Double.parseDouble(partes[2].trim()); 
	        double precioPorMinuto = Double.parseDouble(partes[3].trim()); 
	        
	        // Convertimos a mayúsculas correctamente
	        String tipoServicioStr = partes[4].trim().toUpperCase();
	        String categoriaVehiculoStr = partes[5].trim().toUpperCase();
	        String tipoVehiculoStr = partes[6].trim().toUpperCase();
	        
	        // CONVERSIÓN A ENUMS:
	        TipoServicio tipoServicio = TipoServicio.valueOf(tipoServicioStr);
	        CategoriaVehiculo categoriaVehiculo = CategoriaVehiculo.valueOf(categoriaVehiculoStr);
	        TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(tipoVehiculoStr);
	        
	        // Instanciamos pasándole los objetos Enum
	        Servicio servicio = new Servicio(nombre, tarifaBase, precioPorKm, precioPorMinuto, tipoServicio, tipoVehiculo, categoriaVehiculo);
	            
	        
	        servicios.add(servicio);
	    }
	    
	    lector.close();
	    return servicios;
	}

	public static ProbeHashMap<String,Vehiculo> cargarVehiculos(String fileName) throws FileNotFoundException {
	    
	    ProbeHashMap<String,Vehiculo> vehiculos = new ProbeHashMap<String,Vehiculo>();
	    File archivo = new File(fileName);
	    Scanner lector = new Scanner(archivo);
	    
	    while(lector.hasNextLine()) { 
	        String linea = lector.nextLine(); 
	        
	        if (linea.trim().isEmpty()) {
	            continue; 
	        }
	        
	        String[] partes = linea.split(";");
	        
	        // Un vehículo válido debe tener al menos 6 campos (índices 0 al 5)
	        if (partes.length < 6) {
	            lector.close();
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
	        
	        // Instanciamos el vehículo cumpliendo con tu constructor
	        Vehiculo vehiculo = new Vehiculo(patente, modelo, ubicacionInicial, tipoVehiculo, categoriaVehiculo, tipoServicioPrincipal, capacidadPasajeros);
	        
	        // Verificamos SI el array tiene una 7ma posición (índice 6) antes de intentar leerla
	        if (partes.length > 6 && !partes[6].trim().isEmpty()) {
	            // AHORA SÍ leemos el índice 6
	            TipoServicio tipoServicioSecundario = TipoServicio.valueOf(partes[6].trim().toUpperCase());
	            vehiculo.agregarTipoServicio(tipoServicioSecundario);
	        }
	          
	        vehiculos.put(patente, vehiculo);
	    }
	    
	    lector.close();
	    return vehiculos;
	}

	public static ProbeHashMap<String,Usuario> cargarUsuarios(String fileName,ProbeHashMap<String, Vehiculo> vehiculosCargados) throws FileNotFoundException{
		
		ProbeHashMap<String,Usuario> usuarios = new ProbeHashMap<String,Usuario>();
		 File archivo = new File(fileName);
		    Scanner lector = new Scanner(archivo);
		    
		    while(lector.hasNextLine()) { 
		        String linea = lector.nextLine(); 
		        
		        if (linea.trim().isEmpty()) {
		            continue; 
		        }
		        
		        String[] partes = linea.split(";");
		        
		        if (partes.length < 3) {
		            lector.close();
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
		                lector.close();
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
		        
		        // Guardamos el usuario en el mapa usando su email como clave
		        usuarios.put(email, usuario);
		    }
		
		    lector.close();
		return usuarios;
		
	}
	
}
