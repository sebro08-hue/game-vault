package juegos.snake;

/** Cambio de direccion no permitido (girar 180 grados o mover con la partida terminada). */
public class MovimientoInvalidoException extends Exception {

    public MovimientoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
