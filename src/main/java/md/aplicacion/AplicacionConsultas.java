package md.aplicacion;

import java.io.IOException; 
import java.util.List;

import md.aplicacion.CargadorModelo.DatosIniciales;
import md.config.DAOLoader;
import md.datos.dao.DAOException;
import md.interfaz.Interfaz;
import md.modelo.Servicio;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import net.datastructures.ProbeHashMap;

public class AplicacionConsultas {

	public static void main(String[] args) throws IOException, DAOException {
		
		DatosIniciales datos;
		try {
			datos = new CargadorModelo(DAOLoader.getFactory()).cargar();
		}catch(DAOException e) {
			Interfaz.mostrarError("No se pudieron cargar los datos: " + e.getMessage());
            return;
		}
		
		//Cargar datos
		
		List<Servicio> servicios = datos.getServicios();
		ProbeHashMap<String, Vehiculo> vehiculos = datos.getVehiculos();
		ProbeHashMap<String, Usuario> usuarios = datos.getUsuarios();

		 OpcionMenu opcion;
	     do {
	    	 	opcion = Interfaz.menu();

	            switch (opcion) {
	            case REGISTRAR_USUARIO:
	                break;
	            case DAR_DE_ALTA_CONDUCTOR:
	                break;
	            case SOLICITAR_VIAJE:
	                break;
	            case CANCELAR_VIAJE:
	                break;
	            case SALIR:
	                break;
	            }
	        } while (opcion != OpcionMenu.SALIR);
	}
}
