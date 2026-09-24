package movilidadUrbanaDigital;

public class Cliente extends Usuario {
    private final int limiteCalificaciones = 100;

    public Cliente(String nombre, String documento, String telefono, String email) {
        super(nombre, documento, telefono, email);
    }

    public int getLimiteCalificaciones() {
        return limiteCalificaciones;
    }

    // equals se hereda de usuario

    // to string
    @Override
    public String toString() {
        return "Cliente [limiteCalificaciones=" + limiteCalificaciones + "]";
    }
}
