package md.interfaz;

import java.util.Scanner; 

import md.aplicacion.OpcionMenu;

public class Interfaz {
	
	private static final Scanner           SC  = new Scanner(System.in);
	
	/**
     * Muestra el menú principal y retorna la opción elegida por el usuario.
     */
    public static OpcionMenu menu() {
        System.out.println("\n===== SISTEMA DE GESTIÓN DE VIAJES =====");
        for (OpcionMenu opcion : OpcionMenu.values()) {
            System.out.println(opcion.getNumero() + ". " + opcion.getDescripcion());
        }

        while (true) {
            System.out.print("Ingrese una opción: ");
            String entrada = SC.nextLine();

            try {
                OpcionMenu opcion = OpcionMenu.desdeNumero(Integer.parseInt(entrada));
                if (opcion != null) {
                    return opcion;
                }
                mostrarError("Opción fuera de rango.");
            } catch (NumberFormatException e) {
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
