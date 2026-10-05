package movilidadUrbanaDigital;

import java.util.EnumSet;
import java.util.Set;

public class Vehiculo {
    private String patente;
    private String modelo;
    private Ubicacion ubicacion;
    private TipoVehiculo tipoVehiculo;
    private CategoriaVehiculo categoriaVehiculo;
    // Set (conjunto) garantiza la propiedad 'unique' por definicion
    private Set<TipoServicio> tipoServicios;
    private int capacidadPasajeros;

    // constructor de la clase vehiculo
    /**
     * 
     * @param patente
     * @param modelo
     * @param ubicacion
     * @param tipoVehiculo
     * @param categoriaVehiculo
     * @param tipoServicio
     */
    public Vehiculo(String patente, String modelo, Ubicacion ubicacion, TipoVehiculo tipoVehiculo,
            CategoriaVehiculo categoriaVehiculo, TipoServicio tipoServicio, int capacidadPasajeros) {
        this.patente = patente;
        this.modelo = modelo;
        this.ubicacion = ubicacion;
        this.tipoVehiculo = tipoVehiculo;
        this.categoriaVehiculo = categoriaVehiculo;
        this.capacidadPasajeros = capacidadPasajeros;
		// es un conjunto que contiene solo valores de TipoServicio y no permite repetidos (empieza vacio)
		this.tipoServicios = EnumSet.noneOf(TipoServicio.class);
        agregarTipoServicio(tipoServicio);
    }

    /**
     * agrega un tipo de servicio a un vehiculo.
     * 
     * @param tipoServicio
     * @throws IllegalArgumentException si el tipo de servicio es repetido o invalido
     */
    public void agregarTipoServicio(TipoServicio tipoServicio) {

        if (tipoServicio == null) {
            throw new IllegalArgumentException("El tipo de servicio no puede ser null.");
        }

        if (tipoServicios.contains(tipoServicio)) {
            throw new IllegalArgumentException("El vehiculo ya posee el servicio: " + tipoServicio);
        }

        this.tipoServicios.add(tipoServicio);
    }

    public Set<TipoServicio> getTipoServicios() {
    	
    	Set<TipoServicio> copiaTipoServicio = EnumSet.noneOf(TipoServicio.class);
    	copiaTipoServicio.addAll(tipoServicios);
    	
        return copiaTipoServicio;
    }

    public TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(TipoVehiculo tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public CategoriaVehiculo getCategoriaVehiculo() {
        return categoriaVehiculo;
    }

    public void setCategoriaVehiculo(CategoriaVehiculo categoriaVehiculo) {
        this.categoriaVehiculo = categoriaVehiculo;
    }

    // getters y setters del objeto vehiculo
    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getCapacidadPasajeros() {
        return capacidadPasajeros;
    }

    public void setCapacidadPasajeros(int capacidadPasajeros) {
        this.capacidadPasajeros = capacidadPasajeros;
    }

    // hash y equals del objeto vehiculo
    // dos vehiculos son iguales si tienen la misma patente
    // no se si se deberia chequear por el tipo de servicio siguiendo la idea de que
    // los servicios serian carga y pasajeros
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((patente == null) ? 0 : patente.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vehiculo other = (Vehiculo) obj;
        if (patente == null) {
            if (other.patente != null)
                return false;
        } else if (!patente.equals(other.patente))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Vehiculo [patente=" + patente + ", modelo=" + modelo + ", ubicacion=" + ubicacion + ", tipoVehiculo="
                + tipoVehiculo + ", categoriaVehiculo=" + categoriaVehiculo + ", tipoServicios=" + tipoServicios + "]";
    }

}
