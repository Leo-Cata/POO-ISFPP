package md.datos.dao.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Set;

import md.datos.dao.DAOException;
import md.datos.dao.VehiculoDAO;
import md.datos.dto.VehiculoDTO;
import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import net.datastructures.ProbeHashMap;

public class VehiculoDaoBd implements VehiculoDAO{
	
	private static final String SQL_SERVICIOS = "SELECT patente, tipo_servicios "
			+ "FROM vehiculo";
	private static final String SQL_VEHICULOS = "SELECT patente, modelo, capacidad_pasajeros, tipo_vehiculo, categoria_vehiculo "
			+ " FROM vehiculo";
	
	private final ConexionBD conexion;
	
	public VehiculoDaoBd(ConexionBD conexion) {
		this.conexion = conexion;
	}
	
	@Override
    public ProbeHashMap<String, VehiculoDTO> obtenerTodos() throws DAOException {
        ProbeHashMap<String, VehiculoDTO> vehiculos = new ProbeHashMap<>();

        // Una sola conexion para las dos consultas
        try (Connection con = conexion.obtener()) {

            //Cada patente con los tipos de servicio que ofrece (relacion 1 a N)
            ProbeHashMap<String, Set<TipoServicio>> serviciosPorPatente = new ProbeHashMap<>();
            try (PreparedStatement consulta = con.prepareStatement(SQL_SERVICIOS);
                    ResultSet fila = consulta.executeQuery()) {
                while (fila.next()) {
                    String patente = fila.getString("patente");
                    Set<TipoServicio> tipos = serviciosPorPatente.get(patente);
                    if (tipos == null) {
                        tipos = EnumSet.noneOf(TipoServicio.class);
                        serviciosPorPatente.put(patente, tipos);
                    }
                    
                    int valorServicio = fila.getInt("tipo_servicios");
                    
                    if (valorServicio == 1 || valorServicio == 3) {
                        tipos.add(TipoServicio.ENVIOS);
                    }
                    if (valorServicio == 2 || valorServicio == 3) {
                        tipos.add(TipoServicio.PASAJEROS);
                    }
                    //tipos.add(TipoServicio.valueOf(fila.getString("tipo_servicios").toUpperCase()));
                }
            }

            //Los vehiculos, completando cada uno con sus tipos de servicio
            try (PreparedStatement consulta = con.prepareStatement(SQL_VEHICULOS);
                    ResultSet fila = consulta.executeQuery()) {
                while (fila.next()) {
                    String patente = fila.getString("patente");
                    Set<TipoServicio> tipos = serviciosPorPatente.get(patente);
                    if (tipos == null) {
                        tipos = EnumSet.noneOf(TipoServicio.class);
                    }
                    
                    int idTipo = fila.getInt("tipo_vehiculo");
                    int idCategoria = fila.getInt("categoria_vehiculo");
                    
                    vehiculos.put(patente, new VehiculoDTO(
                            patente,
                            fila.getString("modelo"),
                            fila.getInt("capacidad_pasajeros"),
                            TipoVehiculo.values()[idTipo],
                            CategoriaVehiculo.values()[idCategoria],
                            tipos));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al leer los vehiculos de la base de datos.", e);
        }
        return vehiculos;
    }
}
