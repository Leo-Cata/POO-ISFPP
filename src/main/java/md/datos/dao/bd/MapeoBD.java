package md.datos.dao.bd;

import java.util.EnumSet;
import java.util.Set;

import md.datos.dao.DAOException;
import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;

/**
 * Traduce los codigos numericos guardados en la base de datos a los enums del modelo.
 * Es el unico lugar que conoce esa numeracion: si la base usa otros numeros, se cambia aca.
 */
final class MapeoBD {

    private MapeoBD() {
    }

    static TipoVehiculo tipoVehiculo(int codigo) throws DAOException {
        switch (codigo) {
        case 0:
            return TipoVehiculo.AUTO;
        case 1:
            return TipoVehiculo.MOTO;
        default:
            throw desconocido("tipo de vehiculo", codigo);
        }
    }

    static CategoriaVehiculo categoria(int codigo) throws DAOException {
        switch (codigo) {
        case 0:
            return CategoriaVehiculo.ESTANDAR;
        case 1:
            return CategoriaVehiculo.CONFORT;
        case 2:
            return CategoriaVehiculo.PREMIUM;
        default:
            throw desconocido("categoria de vehiculo", codigo);
        }
    }

    static TipoServicio tipoServicio(int codigo) throws DAOException {
        switch (codigo) {
        case 0:
            return TipoServicio.PASAJEROS;
        case 1:
            return TipoServicio.ENVIOS;
        default:
            throw desconocido("tipo de servicio", codigo);
        }
    }

    /** Servicios que ofrece un vehiculo: 1 = ENVIOS, 2 = PASAJEROS, 3 = ambos. */
    static Set<TipoServicio> tiposDeServicio(int codigo) {
        Set<TipoServicio> tipos = EnumSet.noneOf(TipoServicio.class);
        if (codigo == 1 || codigo == 3) {
            tipos.add(TipoServicio.ENVIOS);
        }
        if (codigo == 2 || codigo == 3) {
            tipos.add(TipoServicio.PASAJEROS);
        }
        return tipos;
    }

    private static DAOException desconocido(String que, int codigo) {
        return new DAOException("Codigo de " + que + " desconocido en la base de datos: " + codigo);
    }
}
