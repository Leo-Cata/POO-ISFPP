package md.datos.dao.bd;

import java.sql.Connection; 
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import md.datos.dao.DAOException;
import md.datos.dao.UsuarioDAO;
import md.datos.dto.UsuarioDTO;
import net.datastructures.ProbeHashMap;

public class UsuarioDaoBd implements UsuarioDAO{
	
	private static final String SQL_ADICIONALES = "SELECT email, patente "
			+ "FROM conductor_vehiculo";
	private static final String SQL_USUARIOS = "SELECT email, nombre, telefono, licencia_conducir "
			+ "FROM usuario";
	private final ConexionBD conexion;
	
	public UsuarioDaoBd(ConexionBD conexion) {
		this.conexion = conexion;
	}
	
	@Override
    public ProbeHashMap<String, UsuarioDTO> obtenerTodos() throws DAOException {
		ProbeHashMap<String, UsuarioDTO> usuarios = new ProbeHashMap<>();

        try (Connection con = conexion.obtener()) {

            // Guardamos todas las patentes temporales por email
            ProbeHashMap<String, Set<String>> patentesPorEmail = new ProbeHashMap<>();
            
            try (PreparedStatement consulta = con.prepareStatement(SQL_ADICIONALES);
                    ResultSet fila = consulta.executeQuery()) {
                   while (fila.next()) {
                       String email = fila.getString("email");
                       String patente = fila.getString("patente");
                       
                       Set<String> patentes = patentesPorEmail.get(email);
                       if (patentes == null) {
                           patentes = new HashSet<>();
                           patentesPorEmail.put(email, patentes);
                       }
                       patentes.add(patente);
                   }
               }

            try (PreparedStatement consulta = con.prepareStatement(SQL_USUARIOS);
                    ResultSet fila = consulta.executeQuery()) {
                   while (fila.next()) {
                       String email = fila.getString("email");
                       String licencia = fila.getString("licencia_conducir");
                       
                       Set<String> todasSusPatentes = patentesPorEmail.get(email);
                       Set<String> patentesAdicionales = new HashSet<>();
                       String patentePrincipal = null;
                       
                       // Copiamos todas las patentes que encontramos
                       if (todasSusPatentes != null) {
                           patentesAdicionales.addAll(todasSusPatentes);
                       }

                       // Si el usuario tiene al menos un vehículo registrado
                       if (!patentesAdicionales.isEmpty()) {
                           // Extraemos el primero y lo hacemos principal
                           patentePrincipal = patentesAdicionales.iterator().next(); 
                           
                           // Lo eliminamos del conjunto para que queden SOLO los adicionales
                           patentesAdicionales.remove(patentePrincipal);
                       }
                    
                    usuarios.put(email, new UsuarioDTO(
                            fila.getString("nombre"),
                            fila.getString("telefono"),
                            email,
                            licencia, 
                            patentePrincipal,
                            patentesAdicionales 
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al leer los usuarios de la base de datos.", e);
        }
        return usuarios;
    }
}
