package md.datos;

import md.datos.DAOFactory;
import md.datos.ServicioDAO;
import md.datos.UsuarioDAO;
import md.datos.VehiculoDAO;

public class TxtDAOFactory implements DAOFactory {
    private final String rutaServicios;
    private final String rutaVehiculos;
    private final String rutaUsuarios;

    public TxtDAOFactory(String rutaServicios, String rutaVehiculos, String rutaUsuarios) {
        this.rutaServicios = rutaServicios;
        this.rutaVehiculos = rutaVehiculos;
        this.rutaUsuarios = rutaUsuarios;
    }

    @Override
    public ServicioDAO getServicioDAO() {
        return new ServicioDaoTxt(this.rutaServicios);
    }

    @Override
    public VehiculoDAO getVehiculoDAO() {
        return new VehiculoDaoTxt(this.rutaVehiculos);
    }

    @Override
    public UsuarioDAO getUsuarioDAO() {
        return new UsuarioDaoTxt(this.rutaUsuarios);
    }
}