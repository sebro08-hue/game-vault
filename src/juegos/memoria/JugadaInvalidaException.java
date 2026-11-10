package juegos.memoria;

/** La jugada no es valida en el estado actual (carta inexistente, ya descubierta, etc.). */
public class JugadaInvalidaException extends Exception {

    public JugadaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
