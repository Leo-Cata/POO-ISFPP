package md.interfaz;

import java.util.Scanner;

import md.aplicacion.Constante;

public class Interfaz {
	
	private static final Scanner           SC  = new Scanner(System.in);
	
	/**
     * Muestra el menú principal y retorna la opción elegida por el usuario.
     */
    public static int menu() {
        System.out.println("\n===== SISTEMA DE GESTIÓN DE BIBLIOTECA =====");
        System.out.println(Constante.OPCION_REGISTRAR_USUARIO       + ". Registrar usuario.");
        System.out.println(Constante.OPCION_DAR_DE_ALTA_CONDUCTOR   + ". Dar de alta conductor.");
        System.out.println(Constante.OPCION_SOLICITAR_VIAJE   		+ ". Solicitar viaje.");
        System.out.println(Constante.OPCION_CANCELAR_VIAJE 			+ ". Cancelar viaje.");
        System.out.println(Constante.OPCION_SALIR         			+ ". Salir.");
        

        // TODO: validar que la entrada sea un número dentro del rango válido
        
        while (true) {

        	System.out.print("Ingrese una opción: ");

            String entrada = SC.nextLine();

            try {

                int opcion = Integer.parseInt(entrada);

                if(opcion >= Constante.OPCION_SALIR && opcion <= Constante.OPCION_CANCELAR_VIAJE)
                    return opcion;

                mostrarError("Opción fuera de rango.");

            }
            catch(NumberFormatException e){
                mostrarError("Debe ingresar un número.");
            }
        }
    }
    public static void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    public static void mostrarError(String mensaje) {
        System.err.println("ERROR: " + mensaje);
    }
}
