package movilidadUrbanaDigital;

import java.util.UUID;

import net.datastructures.Entry;
import net.datastructures.ProbeHashMap;

public class Cliente {

	private ProbeHashMap<UUID, Viaje> viajes;

	public Cliente() {
		this.viajes = new ProbeHashMap<UUID, Viaje>();
	}

	public ProbeHashMap<UUID, Viaje> getViajes() {
		ProbeHashMap<UUID, Viaje> viajesCopia = new ProbeHashMap<UUID, Viaje>();
		
		for(Entry<UUID, Viaje> viaj : viajes.entrySet())
			viajesCopia.put(viaj.getKey(), viaj.getValue());
		//se devuelve una copia de viajes
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

		if(viajes.get(viaje.getId()) != null) {
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

	// to string
	@Override
	public String toString() {
		return "Cliente [viajes=" + viajes + "]";
	}

}
