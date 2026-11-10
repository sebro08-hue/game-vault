package juegos.memoria;

/** La cantidad de parejas pedida no se puede representar en un tablero. */
public class TableroInvalidoException extends Exception {

    public TableroInvalidoException(String mensaje) {
        super(mensaje);
    }
}
