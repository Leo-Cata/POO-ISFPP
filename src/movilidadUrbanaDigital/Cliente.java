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
    
	public boolean enViaje() {
		return false;
		
	}
	// to string
	@Override
	public String toString() {
		return "Cliente [viajes=" + viajes + "]";
	}
    
}
