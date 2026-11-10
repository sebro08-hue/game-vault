package juegos.memoria;

/** Una carta del juego de memoria: un simbolo y su estado actual. */
public class Carta {

    public enum Estado { OCULTA, VOLTEADA, EMPAREJADA }

    private final String simbolo;
    private Estado estado = Estado.OCULTA;

    Carta(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public Estado getEstado() {
        return estado;
    }

    /** Solo el tablero cambia el estado de las cartas. */
    void setEstado(Estado estado) {
        this.estado = estado;
    }
}
