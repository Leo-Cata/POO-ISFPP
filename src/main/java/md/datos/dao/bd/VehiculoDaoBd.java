package md.datos.dao.bd;

import java.sql.Connection; 
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import md.datos.dao.DAOException;
import md.datos.dao.VehiculoDAO;
import md.datos.dto.VehiculoDTO;
import net.datastructures.ProbeHashMap;

public class VehiculoDaoBd implements VehiculoDAO{
	
	private static final String SQL = "SELECT patente, modelo, capacidad_pasajeros, tipo_vehiculo, categoria_vehiculo, tipo_servicios FROM vehiculo";
	private final ConexionBD conexion;
	
	public VehiculoDaoBd(ConexionBD conexion) {
		this.conexion = conexion;
	}
	
	@Override
    public ProbeHashMap<String, VehiculoDTO> obtenerTodos() throws DAOException {
        ProbeHashMap<String, VehiculoDTO> vehiculos = new ProbeHashMap<>();

        try (Connection con = conexion.obtener();
        		PreparedStatement consulta = con.prepareStatement(SQL);
        		ResultSet fila = consulta.executeQuery()) {
        		
        		while(fila.next()) {
        			String patente = fila.getString("patente");
        			vehiculos.put(patente, new VehiculoDTO(
        					patente,
                            fila.getString("modelo"),
                            fila.getInt("capacidad_pasajeros"),
                            MapeoBD.tipoVehiculo(fila.getInt("tipo_vehiculo")),
                            MapeoBD.categoria(fila.getInt("categoria_vehiculo")),
                            MapeoBD.tiposDeServicio(fila.getInt("tipo_servicios"))));
        		}
        		} catch (SQLException e) {
        			throw new DAOException("Error al leer los vehiculos de la base de datos.", e);
                }
           return vehiculos;
        }
}
