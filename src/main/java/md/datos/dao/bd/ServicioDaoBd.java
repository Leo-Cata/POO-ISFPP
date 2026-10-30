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
import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;

public class ServicioDaoBd implements ServicioDAO{
	
	private static final String SQL = "SELECT nombre, tarifa_base, precio_km, precio_minuto,"
			+ "tipo_vehiculo, categoria_vehiculo, tipo_servicio FROM servicio";
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
            	//Convertimos el número entero (0 o 1) de la BD al Enum usando .values()
                int indiceVehiculo = fila.getInt("tipo_vehiculo");
                TipoVehiculo tipoVehiculo = TipoVehiculo.values()[indiceVehiculo];
                
                //Resolvemos el resto de enums asumiendo que estos sí se guardan como TEXTO en la BD
                int indiceCategoria = fila.getInt("categoria_vehiculo");
                CategoriaVehiculo categoria = CategoriaVehiculo.values()[indiceCategoria];
                
                int indiceTipoServicio = fila.getInt("tipo_servicio");
                TipoServicio tipoServicio = TipoServicio.values()[indiceTipoServicio];
                
                servicios.add(new ServicioDTO(
                		fila.getString("nombre"),
                        fila.getDouble("tarifa_base"),
                        fila.getDouble("precio_km"),
                        fila.getDouble("precio_minuto"),
                        tipoVehiculo,
                        categoria,
                        tipoServicio));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al leer los servicios de la base de datos.", e);
        }
        return servicios;
    }		


}
