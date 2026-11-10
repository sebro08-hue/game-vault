package gestor.api;

/**
 * Contrato que debe cumplir todo juego de la plataforma.
 *
 * <p>Convenciones de la plataforma:
 * <ul>
 *   <li>Si la clase del juego extiende {@link javax.swing.JInternalFrame}, la ventana
 *       principal la agrega al escritorio y la muestra despues de llamar a
 *       {@link #iniciar()}. Un juego externo que no sea un JInternalFrame debe
 *       encargarse de mostrarse el mismo dentro de {@link #iniciar()}.</li>
 *   <li>{@link #iniciar()} deja el juego listo para una partida nueva (reinicia el estado).</li>
 *   <li>{@link #getStats()} devuelve el resultado de la ultima partida terminada, o
 *       {@code null} si no hay ninguna. La plataforma lo consulta cuando el jugador
 *       cierra la ventana del juego.</li>
 * </ul>
 */
public interface GameFunction {

    /** Prepara e inicia una partida nueva. */
    void iniciar();

    /** @return el resultado de la ultima partida terminada, o {@code null} si no hay. */
    Stat getStats();
}
