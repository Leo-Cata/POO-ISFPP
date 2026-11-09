package md.ui.impl.cli;

import java.util.ArrayList; 
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

import md.aplicacion.CargadorModelo.DatosIniciales;
import md.aplicacion.ContextoAplicacion;
import md.logica.CalificacionServicio;
import md.logica.GestorViajes;
import md.modelo.RolUsuario;
import md.modelo.Usuario;
import md.modelo.Viaje;
import md.ui.api.UIEngine;
import net.datastructures.Entry;
import md.logica.ValidacionUbicaciones;
import md.modelo.Servicio;
import md.modelo.Ubicacion;
import md.rutas.api.RutaNoDisponibleException;
import md.modelo.CalificacionViaje;
import md.modelo.EstadoViaje;

public class CommandLineInterface implements UIEngine {

    private final Scanner scanner = new Scanner(System.in);
    private final GestorViajes gestorViajes = new GestorViajes();
    private final CalificacionServicio calificacionServicio =
            new CalificacionServicio();

    @Override
    public void start() {

        DatosIniciales datos = ContextoAplicacion.getDatosIniciales();
        ResourceBundle msj = ContextoAplicacion.getMensajes();

        if (datos == null) {
            System.out.println(msj.getString("app.error.no_datos"));
            return;
        }

        if (datos.getUsuarios().isEmpty()) {
            System.out.println(msj.getString("app.error.sin_usuarios"));
            return;
        }

        boolean salir = false;

        while (!salir) {

            Usuario usuario = seleccionarUsuario(datos, msj);

            if (usuario == null) {
                salir = true;
            } else {

                ContextoAplicacion.setUsuarioLogeado(usuario);
                boolean cerrarSesion = false;

                while (!cerrarSesion) {

                    if (usuario.getRolActivo() == RolUsuario.CLIENTE) {
                        cerrarSesion = mostrarMenuCliente(usuario, msj);
                    } else {
                        cerrarSesion = mostrarMenuConductor(usuario, msj);
                    }
                }
                ContextoAplicacion.setUsuarioLogeado(null);
            }
        }

        System.out.println(msj.getString("app.farewell"));
    }

    /**
     * Permite seleccionar un usuario aplicando internacionalización (i18n).
     */
    private Usuario seleccionarUsuario(DatosIniciales datos, ResourceBundle msj) {
        List<Usuario> usuarios = new ArrayList<>();

        System.out.println("\n===== " + msj.getString("login.app.title").toUpperCase() + " =====");
        System.out.println("===== " + msj.getString("login.user.select").toUpperCase() + " =====");

        int numero = 1;

        for (Entry<String, Usuario> entrada : datos.getUsuarios().entrySet()) {
            Usuario usuario = entrada.getValue();
            usuarios.add(usuario);

            String perfil = (usuario.getConductor() != null) ? msj.getString("login.profile.driver") 
                            : msj.getString("login.profile.client");

            System.out.println(numero + ". " + usuario.getNombre() + " - " + usuario.getEmail() + " " + perfil);
            numero++;
        }

        System.out.println("0. " + msj.getString("login.action.exit"));

        while (true) {
            int opcion = leerOpcion(msj.getString("login.action.login") + " (0-" + usuarios.size() + "): ", msj);

            if (opcion == 0) {
                return null;
            }

            if (opcion >= 1 && opcion <= usuarios.size()) {
                Usuario seleccionado = usuarios.get(opcion - 1);
                
                if (seleccionado.getRolActivo() == RolUsuario.CONDUCTOR && seleccionado.getConductor() == null) {
                    System.out.println("\n[" + msj.getString("login.alert.attention") + "]");
                    System.out.println(msj.getString("login.error.notRegisteredDriver"));
                    continue; 
                }
                
                return seleccionado;
            }

            System.out.println("\n[" + msj.getString("login.alert.signInError") + "]");
            System.out.println(msj.getString("login.error.noUserSelected") + ". " + msj.getString("menu.error.intente_nuevamente"));
        }
    }

    /**
     * Muestra las opciones disponibles para un cliente.
     */
    private boolean mostrarMenuCliente(Usuario usuario, ResourceBundle msj) {

        System.out.println("\n===== " + msj.getString("menu.cliente.titulo") + " =====");
        System.out.println(msj.getString("menu.usuario") + ": " + usuario.getNombre());
        System.out.println("1. " + msj.getString("menu.cliente.opcion.solicitar"));
        System.out.println("2. " + msj.getString("menu.cliente.opcion.cancelar"));
        System.out.println("3. " + msj.getString("menu.cliente.opcion.calificar"));

        boolean tienePerfilConductor = usuario.getConductor() != null;

        if (tienePerfilConductor) {
            System.out.println("3. " + msj.getString("menu.cliente.opcion.cambiar_conductor"));
        }

        System.out.println("0. " + msj.getString("menu.opcion.cerrar_sesion"));

        int opcion = leerOpcion(msj.getString("menu.seleccione_opcion") + ": ", msj);

        switch (opcion) {

            case 1:
            	solicitarViaje(usuario, msj);
                break;

            case 2:
            	cancelarViaje(usuario, msj);
                break;
                
            case 3:
                calificarViaje(usuario, msj);
                break;
                
            case 4:
                if (tienePerfilConductor) {
                    cambiarRol(usuario, RolUsuario.CONDUCTOR, msj);
                } else {
                    System.out.println(msj.getString("menu.error.opcion_invalida"));
                }
                break;

            case 0:
                return true;

            default:
                System.out.println(msj.getString("menu.error.opcion_invalida"));
        }

        return false;
    }

    /**
     * Muestra las opciones disponibles para un conductor.
     */
private boolean mostrarMenuConductor(Usuario usuario, ResourceBundle msj) {
        
        System.out.println("\n===== " + msj.getString("menu.conductor.titulo") + " =====");
        System.out.println(msj.getString("menu.usuario") + ": " + usuario.getNombre());
        System.out.println("1. " + msj.getString("menu.conductor.opcion.iniciar_viaje"));
        System.out.println("2. " + msj.getString("menu.conductor.opcion.finalizar_viaje"));
        System.out.println("3. " + msj.getString("menu.cliente.opcion.calificar"));
        System.out.println("4. " + msj.getString("menu.conductor.opcion.cambiar_cliente"));
        System.out.println("0. " + msj.getString("menu.opcion.cerrar_sesion"));

        int opcion = leerOpcion(msj.getString("menu.seleccione_opcion") + ": ", msj);

        switch (opcion) {
            case 1:
                iniciarViajeConductor(usuario, msj);
                break;

            case 2:
                finalizarViajeConductor(usuario, msj);
                break;
                
            case 3:
                calificarViaje(usuario, msj);
                break;

            case 4:
                cambiarRol(usuario, RolUsuario.CLIENTE, msj);
                break;

            case 0:
                return true;

            default:
                System.out.println(msj.getString("menu.error.opcion_invalida"));
        }

        return false;
    }
    /**
     * Solicita el cambio de rol al objeto Usuario.
     */
    private void cambiarRol(Usuario usuario, RolUsuario rolNuevo, ResourceBundle msj) {
        try {
            usuario.cambiarRolActivo(rolNuevo);
            System.out.println(msj.getString("menu.mensaje.rol_cambiado") + ": " + usuario.getRolActivo());
        } catch (IllegalStateException e) {
            System.out.println(msj.getString("menu.error.cambio_rol"));
        }
    }

    /**
     * Lee una opción numérica desde teclado controlando errores.
     */
    private int leerOpcion(String mensaje, ResourceBundle msj) {
        while (true) {
            System.out.print(mensaje);
            String entrada = scanner.nextLine().trim();

            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println(msj.getString("menu.error.ingresar_numero"));
            }
        }
    }
    

/**
 * Coordina la interacción de consola para solicitar un viaje.
 *
 * La interfaz obtiene los datos y presenta los resultados.
 * Las reglas del caso de uso quedan delegadas en GestorViajes.
 */
private void solicitarViaje(Usuario usuario, ResourceBundle msj) {

    if (usuario.getCliente().enViaje()) {
        System.out.println("\n[" + msj.getString("login.alert.attention")
                + "] " + msj.getString("viaje.error.ya_en_viaje"));
        return;
    }

    System.out.println("\n--- " + msj.getString("viaje.titulo.solicitar").toUpperCase()+ " ---");

    try {
        //Generar y validar las ubicaciones de esta primera versión.
        System.out.println(msj.getString("viaje.simulando_ubicaciones") + "...");

        Ubicacion origen = ValidacionUbicaciones.validarYAjustar(
                ContextoAplicacion.getGeneradorUbicaciones().generar(),
                ContextoAplicacion.getMotorVial());

        Ubicacion destino = ValidacionUbicaciones.validarYAjustar(
                ContextoAplicacion.getGeneradorUbicaciones().generar(),
                ContextoAplicacion.getMotorVial());

        //Obtener los servicios disponibles.
        List<Servicio> servicios = ContextoAplicacion.getDatosIniciales().getServicios();

        if (servicios == null || servicios.isEmpty()) {
            System.out.println(msj.getString("app.error.inesperado")
                    + ": No hay servicios configurados.");
            return;
        }

        System.out.println("\n" + msj.getString("viaje.seleccione_servicio") + ":");

        for (int i = 0; i < servicios.size(); i++) {
            Servicio servicio = servicios.get(i);

            String claveServicio = "servicio." + servicio.getNombre().toLowerCase()
                            					.replace("á", "a")
                            					.replace("é", "e")
                            					.replace("í", "i")
                            					.replace("ó", "o")
                            					.replace("ú", "u");

            String nombre = msj.containsKey(claveServicio)
                    ? msj.getString(claveServicio)
                    : servicio.getNombre();

            System.out.println((i + 1) + ". " + nombre
                    + " (" + msj.getString("viaje.servicio.base")
                    + " $" + servicio.getTarifaBase() + ")");
        }

        int opcionServicio = leerOpcionRango(
                msj.getString("menu.seleccione_opcion") + ": ",
                1,
                servicios.size(),
                msj);

        Servicio servicioSeleccionado = servicios.get(opcionServicio - 1);

        //Calcular la cotización comercial.
        GestorViajes.Cotizacion cotizacion = gestorViajes.calcularCotizacion(servicioSeleccionado,origen,destino);

        //Presentar el resumen antes de confirmar.
        System.out.println("\n--- "
                + msj.getString("viaje.confirmacion.resumen") + " ---");

        System.out.printf("%s: %.5f, %.5f%n",
                msj.getString("viaje.resumen.origen"),
                origen.getLatitud(),
                origen.getLongitud());

        System.out.printf("%s: %.5f, %.5f%n",
                msj.getString("viaje.resumen.destino"),
                destino.getLatitud(),
                destino.getLongitud());

        System.out.println(msj.getString("viaje.resumen.servicio")
                + ": " + servicioSeleccionado.getNombre());

        System.out.printf("%s: %.2f %s%n",
                msj.getString("viaje.costo_estimado"),
                cotizacion.costoTotal(),
                "");

        System.out.printf("%s: %.2f %s%n",
                msj.getString("viaje.cotizacion.distancia"),
                cotizacion.distanciaKm(),
                msj.getString("viaje.unidad.km"));

        System.out.printf("%s: %.0f %s%n",
                msj.getString("viaje.cotizacion.duracion"),
                cotizacion.duracionMinutos(),
                msj.getString("viaje.unidad.min"));

        int confirmar = leerOpcionRango(
                msj.getString("viaje.confirmar_solicitud") + " "
                        + msj.getString("viaje.confirmar.opciones") + ": ",
                1,
                2,
                msj);

        if (confirmar == 2) {
            System.out.println("\n" + msj.getString("viaje.cancelado"));
            return;
        }

        // 5. Confirmada la solicitud: buscar conductor y calcular rutas.
        System.out.println("\n" + msj.getString("viaje.buscando_conductor") + "...");

        GestorViajes.ResultadoSolicitud resultado =
                gestorViajes.procesarSolicitudViaje(usuario,servicioSeleccionado,origen,destino);

        if (resultado == null) {
            System.out.println(msj.getString("viaje.error.sin_conductores"));
            return;
        }

        // 6. Mostrar la asignación y los dos tramos viales.
        System.out.println("\n" + msj.getString("viaje.conductor_encontrado")
                + ": " + resultado.conductor().getNombre());

        String km = msj.getString("viaje.unidad.km");
        String min = msj.getString("viaje.unidad.min");

        System.out.println("\n--- "
                + msj.getString("viaje.resultados_ruta") + " ---");

        System.out.printf("%s: %.2f %s (%.0f %s)%n",
                msj.getString("viaje.ruta.acercamiento"),
                resultado.rutas().acercamiento().distanciaKm(),
                km,
                Math.ceil(resultado.rutas().acercamiento().duracionMinutos()),
                min);

        System.out.printf("%s: %.2f %s (%.0f %s)%n",
                msj.getString("viaje.ruta.traslado"),
                resultado.rutas().traslado().distanciaKm(),
                km,
                Math.ceil(resultado.rutas().traslado().duracionMinutos()),
                min);

        System.out.printf("%s: $%.2f%n",
                msj.getString("viaje.costo_estimado"),
                resultado.cotizacion().costoTotal());

        System.out.println(msj.getString("viaje.estado") + ": " + resultado.viaje().estadoActual());

    } catch (RutaNoDisponibleException e) {
        System.out.println("\n" + msj.getString("viaje.error.ruta_no_disponible"));
    } catch (IllegalArgumentException | IllegalStateException e) {
        System.out.println("\n[" + msj.getString("login.alert.attention")
                + "] " + e.getMessage());
    } catch (Exception e) {
        System.out.println("\n" + msj.getString("app.error.inesperado")
                + ": " + e.getMessage());
    }
}


    /*permite al cliente cancelar su viaje actual*/
    private void cancelarViaje(Usuario usuario, ResourceBundle msj) {
        System.out.println("\n--- " + msj.getString("viaje.titulo.cancelar") + " ---");

        if (!usuario.getCliente().enViaje()) {
            System.out.println("[" + msj.getString("login.alert.attention") + "] " + msj.getString("viaje.error.sin_viaje_activo"));
            return;
        }

        int confirmar = leerOpcionRango(msj.getString("viaje.confirmar_cancelacion") + " " + msj.getString("viaje.confirmar.opciones") + ": ", 1, 2, msj);

        if (confirmar == 1) {
            try {
                String motivo = msj.containsKey("viaje.motivo.default") ? msj.getString("viaje.motivo.default") : "Cancelado por el cliente";
                boolean exito = gestorViajes.procesarCancelacionViaje(usuario, motivo);

                if (exito) {
                    System.out.println("\n" + msj.getString("viaje.cancelacion.exitoso"));
                } else {
                    System.out.println("\n[" + msj.getString("login.alert.attention") + "] " + msj.getString("viaje.error.sin_viaje_activo"));
                }
            } catch (Exception e) {
                System.out.println("\n[" + msj.getString("login.alert.attention") + "] " + e.getMessage());
            }
        } else {
            System.out.println("\n" + msj.getString("viaje.cancelacion.abortada"));
        }
    }

    /**
     * Lee una opción numérica forzando un rango válido.
     */
    private int leerOpcionRango(String mensaje, int min, int max, ResourceBundle msj) {
        while (true) {
            int opcion = leerOpcion(mensaje, msj);
            if (opcion >= min && opcion <= max) {
                return opcion;
            }
            System.out.println(msj.getString("menu.error.opcion_invalida"));
        }
    }
    private void iniciarViajeConductor(Usuario usuario, ResourceBundle msj) {
        System.out.println("\n--- " + msj.getString("viaje.titulo.iniciar") + " ---");
        try {
            boolean exito = gestorViajes.procesarInicioViaje(usuario);
            if (exito) {
                System.out.println(msj.getString("viaje.inicio.exitoso"));
            } else {
                System.out.println("[" + msj.getString("login.alert.attention") + "] " + msj.getString("viaje.error.sin_viaje_aceptado"));
            }
        } catch (Exception e) {
            System.out.println("[" + msj.getString("login.alert.attention") + "] " + e.getMessage());
        }
    }


private void finalizarViajeConductor(Usuario usuario, ResourceBundle msj) {

    System.out.println("\n--- " + msj.getString("viaje.titulo.finalizar") + " ---");

    try {
    	boolean exito = gestorViajes.procesarFinalizacionViaje(usuario);

        if (exito) {
            System.out.println(msj.getString("viaje.fin.exitoso"));
        } else {
            System.out.println("[" + msj.getString("login.alert.attention")
                    + "] " + msj.getString("viaje.error.sin_viaje_iniciado"));
        }

    } catch (Exception e) {
        System.out.println("[" + msj.getString("login.alert.attention")
                + "] " + msj.getString("viaje.error.finalizacion"));
    }
}

private void calificarViaje(Usuario usuario, ResourceBundle msj) {

    Viaje viaje = gestorViajes.consultarViajeActivo(usuario);

    if (viaje == null || viaje.estadoActual() != EstadoViaje.INICIADO) {
        System.out.println(msj.getString("viaje.error.sin_viaje_iniciado"));
        return;
    }

    // Comprobar si el usuario ya calificó este viaje.
    CalificacionViaje calificacionExistente;

    if (usuario.getRolActivo() == RolUsuario.CLIENTE) {
        calificacionExistente = viaje.getCalificacionCliente();
    } else {
        calificacionExistente = viaje.getCalificacionConductor();
    }

    if (calificacionExistente != null) {
        System.out.println(msj.getString("calificacion.error.ya_calificado"));
        return;
    }

    System.out.println("\n--- " + msj.getString("calificacion.titulo") + " ---");

    System.out.println("1. " + msj.getString("calificacion.opcion.malo"));
    System.out.println("2. " + msj.getString("calificacion.opcion.regular"));
    System.out.println("3. " + msj.getString("calificacion.opcion.bueno"));
    System.out.println("4. " + msj.getString("calificacion.opcion.muy_bueno"));
    System.out.println("5. " + msj.getString("calificacion.opcion.excelente"));
    System.out.println("0. " + msj.getString("calificacion.opcion.no_calificar"));

    int opcion = leerOpcionRango(
            msj.getString("calificacion.seleccione") + ": ",
            0,
            5,
            msj);

    if (opcion == 0) {
        System.out.println(msj.getString("calificacion.mensaje.omitida"));
        return;
    }

    CalificacionViaje calificacion = switch (opcion) {
        case 1 -> CalificacionViaje.MALO;
        case 2 -> CalificacionViaje.REGULAR;
        case 3 -> CalificacionViaje.BUENO;
        case 4 -> CalificacionViaje.MUY_BUENO;
        case 5 -> CalificacionViaje.EXCELENTE;
        default -> throw new IllegalStateException();
    };

    try {

        if (usuario.getRolActivo() == RolUsuario.CLIENTE) {

            calificacionServicio.calificarComoCliente(
                    usuario, viaje, calificacion);

        } else {

            calificacionServicio.calificarComoConductor(
                    usuario, viaje, calificacion);
        }

        System.out.println(msj.getString("calificacion.mensaje.registrada"));

    } catch (IllegalArgumentException | IllegalStateException e) {
        System.out.println(msj.getString("calificacion.error.registro"));
    }
}

}