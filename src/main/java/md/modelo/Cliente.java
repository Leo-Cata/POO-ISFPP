package md.modelo;

import java.util.ArrayList;
import java.util.UUID;

import net.datastructures.Entry;
import net.datastructures.ProbeHashMap;

public class Cliente {

	private static final int MAX_CALIFICACIONES = 100;

	private ArrayList<CalificacionViaje> calificaciones = new ArrayList<CalificacionViaje>();
	private ProbeHashMap<UUID, Viaje> viajes;

	public Cliente() {
		this.viajes = new ProbeHashMap<UUID, Viaje>();
	}

	public ProbeHashMap<UUID, Viaje> getViajes() {
		ProbeHashMap<UUID, Viaje> viajesCopia = new ProbeHashMap<UUID, Viaje>();

		for (Entry<UUID, Viaje> viaj : viajes.entrySet())
			viajesCopia.put(viaj.getKey(), viaj.getValue());
		// se devuelve una copia de viajes
		return viajesCopia;
	}

	/**
	 * agrega un viaje al mapa de viajes
	 * 
	 * @param viaje del cual se obtendra su UUID para ser usada como llave
	 */
	public void agregarViajes(Viaje viaje) {

		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser null");
		}

		if (viajes.get(viaje.getId()) != null) {
			throw new IllegalArgumentException("El viaje ya esta registrado.");
		}

		this.viajes.put(viaje.getId(), viaje);
	}

	/**
	 * @return true si el cliente tiene algun viaje en curso (SOLICITADO, ACEPTADO o
	 *         INICIADO)
	 */
	public boolean enViaje() {
		for (Viaje v : viajes.values()) {
			EstadoViaje estado = v.estadoActual();

			if (estado == EstadoViaje.SOLICITADO || estado == EstadoViaje.ACEPTADO || estado == EstadoViaje.INICIADO)
				return true;
		}

		return false;
	}

	/**
	 * Agrega una calificación recibida por el cliente. Se conservan como máximo
	 * 100; al superarse ese límite se descarta la más antigua.
	 *
	 * @param calificacion calificación a registrar.
	 * @throws IllegalArgumentException si la calificación es {@code null}.
	 */
	public void agregarCalificacion(CalificacionViaje calificacion) {
		if (calificacion == null) {
			throw new IllegalArgumentException("La calificación no puede ser null.");
		}

		if (calificaciones.size() == MAX_CALIFICACIONES) {
			calificaciones.remove(0);
		}

		calificaciones.addLast(calificacion);
		;
	}

	// to string
	@Override
	public String toString() {
		return "Cliente [viajes=" + viajes + "]";
	}

	public ArrayList<CalificacionViaje> getCalificaciones() {
		ArrayList<CalificacionViaje> copiaCalificaciones = new ArrayList<CalificacionViaje>();

		copiaCalificaciones.addAll(calificaciones);

		return copiaCalificaciones;
	}
}
