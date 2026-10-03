package movilidadUrbanaDigital;

import java.util.UUID;

import net.datastructures.ProbeHashMap;

public class Cliente{
	
	private ProbeHashMap<UUID,Viaje> viajes;

    public Cliente() {
		this.viajes = new ProbeHashMap<UUID,Viaje>();
	}

	public ProbeHashMap<UUID, Viaje> getViajes() {
		return viajes;
	}

	public void agregarViajes(Viaje viaje) {
		this.viajes.put(viaje.getId(), viaje);
	}
    
	/**
	 * @return true si el cliente tiene algun viaje en curso (SOLICITADO, ACEPTADO o INICIADO)
	 */
	public boolean enViaje() {
		for (Viaje v : viajes.values()) {
			EstadoViaje estado = v.estadoActual();
			
			if(estado == EstadoViaje.SOLICITADO || estado == EstadoViaje.ACEPTADO || estado == EstadoViaje.INICIADO)
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
