package md.datos.dao.bd;

import java.sql.Connection; 
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import md.datos.dao.DAOException;
import md.datos.dao.ServicioDAO;
import md.datos.dto.ServicioDTO;

public class ServicioDaoBd implements ServicioDAO{
	
	private static final String SQL = "SELECT nombre, tarifa_base, precio_km, precio_minuto,"
									+ "tipo_vehiculo, categoria_vehiculo, tipo_servicio "
									+ "FROM servicio";
	private final ConexionBD conexion;
	
	
	
	public ServicioDaoBd(ConexionBD conexion) {
		this.conexion = conexion;
	}
	
	@Override
    public List<ServicioDTO> obtenerTodos() throws DAOException {
        List<ServicioDTO> servicios = new ArrayList<>();

        // Se cierran solos, en orden inverso: ResultSet, PreparedStatement, Connection
        try (Connection con = conexion.obtener();
                PreparedStatement consulta = con.prepareStatement(SQL);
                ResultSet fila = consulta.executeQuery()) {

            while (fila.next()) {
                
                servicios.add(new ServicioDTO(
                		fila.getString("nombre"),
                        fila.getDouble("tarifa_base"),
                        fila.getDouble("precio_km"),
                        fila.getDouble("precio_minuto"),
                        MapeoBD.tipoVehiculo(fila.getInt("tipo_vehiculo")),
                        MapeoBD.categoria(fila.getInt("categoria_vehiculo")),
                        MapeoBD.tipoServicio(fila.getInt("tipo_servicio"))));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al leer los servicios de la base de datos.", e);
        }
        return servicios;
    }		


}
