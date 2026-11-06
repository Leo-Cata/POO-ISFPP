
package md.logica;

import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.modelo.Vehiculo;
import md.rutas.api.PlanificadorRutas;
import md.rutas.api.RutaCalculada;
import md.rutas.api.RutaNoDisponibleException;
import md.rutas.api.RutasViaje;

/**
 * Servicio encargado de calcular los dos tramos viales
 * correspondientes a un viaje.
 */
public class PlanificacionViajeService {

    private final PlanificadorRutas planificador;

    /**
     * Recibe el motor de rutas que realizará los cálculos.
     *
     * @param planificador contrato del motor de rutas.
     * @throws IllegalArgumentException si el planificador es null.
     */
    public PlanificacionViajeService(PlanificadorRutas planificador) {

        if (planificador == null) {
            throw new IllegalArgumentException("El planificador de rutas no puede ser null.");
        }

        this.planificador = planificador;
    }

    /**
     * Calcula el acercamiento del vehículo al origen y el traslado
     * desde el origen hasta el destino.
     *
     * @param vehiculo vehículo seleccionado para el viaje.
     * @param origen ubicación de recogida.
     * @param destino ubicación de destino.
     * @return ambos recorridos agrupados en RutasViaje.
     * @throws IllegalArgumentException si algún parámetro obligatorio es null.
     * @throws IllegalStateException si el vehículo no tiene ubicación o tipo.
     * @throws RutaNoDisponibleException si no puede calcularse alguno de los tramos.
     */
    public RutasViaje planificar(Vehiculo vehiculo,Ubicacion origen,Ubicacion destino) 
    		throws RutaNoDisponibleException {

        if (vehiculo == null || origen == null || destino == null) {
            throw new IllegalArgumentException(
                    "El vehículo, el origen y el destino no pueden ser null.");
        }

        Ubicacion ubicacionVehiculo = vehiculo.getUbicacion();

        if (ubicacionVehiculo == null) {
            throw new IllegalStateException("El vehículo no tiene una ubicación inicial asignada.");
        }

        TipoVehiculo tipoVehiculo = vehiculo.getTipoVehiculo();

        if (tipoVehiculo == null) {
            throw new IllegalStateException("El vehículo no tiene un tipo asignado.");
        }

        // Primer tramo: vehículo hasta el origen.
        RutaCalculada acercamiento = planificador.calcular(ubicacionVehiculo, origen, tipoVehiculo);

        // Segundo tramo: origen hasta el destino.
        RutaCalculada traslado = planificador.calcular(origen, destino, tipoVehiculo);

        return new RutasViaje(acercamiento, traslado);
    }
}
