package md.simulacion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import md.aplicacion.ContextoAplicacion;
import md.config.DAOLoader;
import md.datos.dao.DAOException;
import md.datos.dao.DAOFactory;
import md.logica.GeneradorUbicacionesViales;
import md.logica.GestorViajes;
import md.modelo.Usuario;

/**
 * Punto de entrada de la simulación por consola.
 *
 * Lanza un hilo por conductor y un hilo generador de pasajeros, deja correr la
 * simulación durante el tiempo configurado y, al final, muestra las
 * estadísticas.
 * Se ejecuta como cualquier otro main, por separado de la interfaz gráfica.
 */
public class AplicacionSimulacion {
    /**
     * Punto de entrada de la simulación por consola.
     * <p>
     * Carga los datos, separa a los usuarios en conductores y pasajeros, lanza un
     * hilo virtual por conductor y un hilo generador de pasajeros, deja correr la
     * simulación el tiempo configurado y al final cierra el {@link Despachador}
     * e imprime el resumen de {@link Estadisticas}.
     *
     * @param args argumentos de línea de comandos (no se utilizan).
     * @throws IOException           si falla la lectura de la configuración o de
     *                               los datos.
     * @throws DAOException          si ocurre un error al cargar los datos.
     * @throws InterruptedException  si el hilo principal es interrumpido mientras
     *                               espera.
     * @throws IllegalStateException si no hay conductores, pasajeros o servicios
     *                               para simular.
     */
    public static void main(String[] args) throws IOException, DAOException, InterruptedException {
        try {
            DAOFactory persistencia = DAOLoader.getFactory();
            ContextoAplicacion.inicializar(persistencia);

            var datos = ContextoAplicacion.getDatosIniciales();
            // Preparamos el estado de los conductores para la simulación.
            // Las ubicaciones de sus vehículos ya fueron asignadas por ContextoAplicacion.
            GeneradorUbicacionesViales generadorUbicaciones = ContextoAplicacion.getGeneradorUbicaciones();

            InicializadorEscenario inicializador = new InicializadorEscenario();
            inicializador.inicializar(datos.getUsuarios());

            GestorViajes gestorViajes = ContextoAplicacion.getGestorViajes();

            // Conductores: usuarios con perfil de conductor. Pasajeros: el resto.
            List<Usuario> conductores = new ArrayList<>();
            List<Usuario> pasajeros = new ArrayList<>();
            for (Usuario usuario : datos.getUsuarios().values()) {
                if (usuario.getConductor() != null) {
                    conductores.add(usuario);
                } else {
                    pasajeros.add(usuario);
                }
            }
            if (conductores.isEmpty() || pasajeros.isEmpty() || datos.getServicios().isEmpty()) {
                throw new IllegalStateException("Hacen falta conductores, pasajeros y servicios para simular.");
            }
            /**
             * Obtiene los parámetros necesarios para ejecutar la simulación.
             */
            long duracionMs = ConfiguracionSimulacion.getDuracionSegundos() * 1000L;

            double factor = ConfiguracionSimulacion.getFactorAceleracion();

            PoliticaAsignacion politica = PoliticaAsignacion.valueOf(
                    ConfiguracionSimulacion.getPolitica());

            long esperaMaximaMs = ConfiguracionSimulacion.getEsperaMaximaSegundos() * 1000L;

            long intervaloMs = ConfiguracionSimulacion.getIntervaloSolicitudesMs();

            Estadisticas estadisticas = new Estadisticas();
            Despachador despachador = new Despachador(datos.getUsuarios(), ContextoAplicacion.getMotorVial(),
                    politica, esperaMaximaMs, estadisticas);

            System.out.println("====================================================");
            System.out.println("           SIMULACIÓN DE MOVILIDAD DIGITAL");
            System.out.println("====================================================");
            System.out.println("Conductores: " + conductores.size());
            System.out.println("Pasajeros: " + pasajeros.size());
            System.out.println("Duración: " + duracionMs / 1000 + " segundos");
            System.out.println("Política de asignación: " + politica);
            System.out.println("Factor de aceleración: " + factor);
            System.out.println("----------------------------------------------------");

            despachador.mostrarEstadoInicial();

            // Un hilo virtual por tarea: un hilo por conductor, más el generador de
            // pasajeros
            ExecutorService hilos = Executors.newVirtualThreadPerTaskExecutor();
            try {
                hilos.execute(new GeneradorPasajeros(
                        pasajeros,
                        datos.getServicios(),
                        despachador,
                        generadorUbicaciones,
                        intervaloMs,
                        gestorViajes));
                for (Usuario conductor : conductores) {
                    hilos.execute(new ConductorSimulado(conductor, despachador, factor));
                }

                Thread.sleep(duracionMs);
            } finally {
                // Primero se corta el despachador, después se espera a los hilos
                despachador.cerrar();
                hilos.shutdown();
                if (!hilos.awaitTermination(30, TimeUnit.SECONDS)) {
                    hilos.shutdownNow();
                    hilos.awaitTermination(10, TimeUnit.SECONDS);
                }
            }

            System.out.println();
            System.out.println("================ RESUMEN FINAL ================");
            System.out.println(estadisticas.resumen());
            System.out.println("Cancelaciones registradas: " + despachador.getCantidadCancelaciones());
            System.out.println("================================================");
        } finally {
            ContextoAplicacion.cerrar();
        }
    }
}
