package md.simulacion;

/**
 * Cómo elegir un conductor cuando hay más de uno compatible con el viaje.
 */
public enum PoliticaAsignacion {

    /** Sorteo: al azar entre los conductores compatibles. */
    SORTEO,

    /** Cercanía: el conductor más cerca del origen del pasajero. */
    CERCANIA,

    /**
     * Rotación: el conductor que menos viajes finalizados tiene. Si hay empate,
     * gana el más cercano.
     */
    ROTACION
}
