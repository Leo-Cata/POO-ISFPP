package md.aplicacion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import md.config.CargarParametros;
import md.config.DAOLoader;
import md.datos.dao.DAOException;
import md.datos.dao.DAOFactory;
import md.modelo.Usuario;
import md.simulacion.ConductorSimulado;
import md.simulacion.Despachador;
import md.simulacion.Estadisticas;
import md.simulacion.GeneradorPasajeros;
import md.simulacion.PoliticaAsignacion;

/**
 * Punto de entrada de la simulación por consola.
 *
 * Lanza un hilo por conductor y un hilo generador de pasajeros, deja correr la
 * simulación durante el tiempo configurado y, al final, muestra las estadísticas.
 * Se ejecuta como cualquier otro main, por separado de la interfaz gráfica.
 */
public class AplicacionSimulacion {

    public static void main(String[] args) throws IOException, DAOException, InterruptedException {
        try {
            DAOFactory persistencia = DAOLoader.getFactory();
            ContextoAplicacion.inicializar(persistencia);

            var datos = ContextoAplicacion.getDatosIniciales();

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

            long duracionMs = CargarParametros.getSimulacionDuracionSegundos() * 1000L;
            double factor = CargarParametros.getSimulacionFactorAceleracion();
            PoliticaAsignacion politica = PoliticaAsignacion.valueOf(CargarParametros.getSimulacionPolitica());
            long esperaMaximaMs = CargarParametros.getSimulacionEsperaMaximaSegundos() * 1000L;
            long intervaloMs = CargarParametros.getSimulacionIntervaloSolicitudesMs();

            Estadisticas estadisticas = new Estadisticas();
            Despachador despachador = new Despachador(datos.getUsuarios(), ContextoAplicacion.getMotorVial(),
                    politica, esperaMaximaMs, estadisticas);

            // Un hilo virtual por tarea: un hilo por conductor, más el generador de pasajeros
            ExecutorService hilos = Executors.newVirtualThreadPerTaskExecutor();
            try {
                hilos.execute(new GeneradorPasajeros(pasajeros, datos.getServicios(), despachador,
                        ContextoAplicacion.getGeneradorUbicaciones(), intervaloMs));
                for (Usuario conductor : conductores) {
                    hilos.execute(new ConductorSimulado(conductor, despachador, factor));
                }

                System.out.println("Simulación en curso durante " + duracionMs / 1000 + " segundos...");
                Thread.sleep(duracionMs);
            } finally {
                // Primero se corta el despachador, después se espera a los hilos
                despachador.cerrar();
                hilos.shutdown();
                if (!hilos.awaitTermination(30, TimeUnit.SECONDS)) {
                    hilos.shutdownNow();
                }
            }

            System.out.println("--- Estadísticas ---");
            System.out.println(estadisticas.resumen());
        } finally {
            ContextoAplicacion.cerrar();
        }
    }
}
