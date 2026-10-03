package movilidadUrbanaDigital;

public enum CalificacionViaje {
    // valores posibles del enum
	NO_CALIFICADO(0),
    MALO(1),
    REGULAR(2),
    BUENO(3),
    MUY_BUENO(4),
    EXCELENTE(5);

    private final int puntaje;

    // constructor, por defecto es privado
    CalificacionViaje(int puntaje) {
        this.puntaje = puntaje;
    }

    // getter del puntaje
    public int getPuntaje() {
        return puntaje;
    }
}
