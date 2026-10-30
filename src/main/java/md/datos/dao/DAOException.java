package md.datos.dao;

/**
 * Error al acceder a los datos, sin importar de donde vengan (archivo, base de datos, etc.).
 * Las interfaces DAO lanzan esta excepcion para no exponer detalles de la tecnologia usada
 * (FileNotFoundException en txt, SQLException en BD).
 */
public class DAOException extends Exception {

    private static final long serialVersionUID = 1L;

    public DAOException(String mensaje) {
        super(mensaje);
    }

    public DAOException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
