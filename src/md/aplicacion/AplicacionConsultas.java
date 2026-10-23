package md.aplicacion;

import java.io.IOException;
import java.util.List;

import md.aplicacion.CargadorModelo.DatosIniciales;
import md.datos.dao.DAOFactory;
import md.datos.dao.DAOLoader;
import md.interfaz.Interfaz;
import md.modelo.Servicio;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import net.datastructures.ProbeHashMap;

public class AplicacionConsultas {

	public static void main(String[] args) throws IOException {
		DAOFactory fabrica = DAOLoader.getFactory();
		CargadorModelo cargaModelo = new CargadorModelo(fabrica);
		
		//Cargar datos
		DatosIniciales datos = cargaModelo.cargar();
		List<Servicio> servicios = datos.getServicios();
		ProbeHashMap<String, Vehiculo> vehiculos = datos.getVehiculos();
		ProbeHashMap<String, Usuario> usuarios = datos.getUsuarios();

		int opcion;
		do {
			opcion = Interfaz.menu();
			
			switch(opcion) {
				case Constante.OPCION_REGISTRAR_USUARIO:{
					break;
				}
				case Constante.OPCION_DAR_DE_ALTA_CONDUCTOR:{
					break;
				}
				case Constante.OPCION_SOLICITAR_VIAJE:{
					break;
				}
				case Constante.OPCION_CANCELAR_VIAJE:{
					break;
				}
				case Constante.OPCION_SALIR:{
					break;
				}
				default:
                    Interfaz.mostrarError("Opción no válida.");
				
			}
		}while(opcion != Constante.OPCION_SALIR);

		
		 
	}

}
