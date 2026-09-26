package movilidadUrbanaDigital;

public enum Calificacion {
    // valores posibles del enum
    MALO(1),
    REGULAR(2),
    BUENO(3),
    MUY_BUENO(4),
    EXCELENTE(5);

    private final int puntaje;

    // constructor, por defecto es privado
    Calificacion(int puntaje) {
        this.puntaje = puntaje;
    }

    // getter del puntaje
    public int getPuntaje() {
        return puntaje;
    }
}
