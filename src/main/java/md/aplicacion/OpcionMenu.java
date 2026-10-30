package md.aplicacion;

/**
 * Opciones del menu principal. El orden de declaracion es el orden en que se muestran.
 */
public enum OpcionMenu {
    REGISTRAR_USUARIO(1, "Registrar usuario."),
    DAR_DE_ALTA_CONDUCTOR(2, "Dar de alta conductor."),
    SOLICITAR_VIAJE(3, "Solicitar viaje."),
    CANCELAR_VIAJE(4, "Cancelar viaje."),
    SALIR(0, "Salir.");

    private final int numero;
    private final String descripcion;

    private OpcionMenu(int numero, String descripcion) {
        this.numero = numero;
        this.descripcion = descripcion;
    }

    public int getNumero() {
        return numero;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** Devuelve la opcion con ese numero, o null si no existe. */
    public static OpcionMenu desdeNumero(int numero) {
        for (OpcionMenu opcion : values()) {
            if (opcion.numero == numero) {
                return opcion;
            }
        }
        return null;
    }
}
