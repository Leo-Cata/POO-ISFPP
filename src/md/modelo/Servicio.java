package md.modelo;

public class Servicio {
    private String nombre;
    private double tarifaBase;
    private double precioPorKm;
    private double precioPorMinuto;
    private TipoServicio tipoServicio;
    private TipoVehiculo tipoVehiculo;
    private CategoriaVehiculo categoriaVehiculo;

    // constructor
    public Servicio(String nombre, double tarifaBase, double precioPorKm, double precioPorMinuto,
            TipoVehiculo tipoVehiculo, CategoriaVehiculo categoriaVehiculo,TipoServicio tipoServicio) {
        this.nombre = nombre;
        this.tarifaBase = tarifaBase;
        this.precioPorKm = precioPorKm;
        this.precioPorMinuto = precioPorMinuto;
        this.tipoServicio = tipoServicio;
        this.tipoVehiculo = tipoVehiculo;
        this.categoriaVehiculo = categoriaVehiculo;
    }

    /**
     * 
     * @param km
     * @param minutos
     * @return el costo del servicio
     */
    public double calcularCosto(double km, double minutos) {

        return tarifaBase + (precioPorKm * km) + (precioPorMinuto * minutos);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getTarifaBase() {
        return tarifaBase;
    }

    public void setTarifaBase(double tarifaBase) {
        this.tarifaBase = tarifaBase;
    }

    public double getPrecioPorKm() {
        return precioPorKm;
    }

    public void setPrecioPorKm(double precioPorKm) {
        this.precioPorKm = precioPorKm;
    }

    public double getPrecioPorMinuto() {
        return precioPorMinuto;
    }

    public void setPrecioPorMinuto(double precioPorMinuto) {
        this.precioPorMinuto = precioPorMinuto;
    }

    public TipoServicio getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(TipoServicio tipoServicio) {
        this.tipoServicio = tipoServicio;
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

    // dos servicios son iguales si comparten el mismo nombre y el mismo tipo de
    // servicio
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nombre == null) ? 0 : nombre.hashCode());
        result = prime * result + ((tipoServicio == null) ? 0 : tipoServicio.hashCode());
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
        Servicio other = (Servicio) obj;
        if (nombre == null) {
            if (other.nombre != null)
                return false;
        } else if (!nombre.equals(other.nombre))
            return false;
        if (tipoServicio != other.tipoServicio)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Servicio [nombre=" + nombre + ", tarifaBase=" + tarifaBase + ", precioPorKm=" + precioPorKm
                + ", precioPorMinuto=" + precioPorMinuto + ", tipoServicio=" + tipoServicio + ", tipoVehiculo="
                + tipoVehiculo + ", categoriaVehiculo=" + categoriaVehiculo + "]";
    }

}
