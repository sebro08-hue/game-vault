package juegos.snake;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Reglas del juego, separadas de la interfaz grafica: mover la serpiente, comer,
 * crecer, detectar choques contra la pared o contra si misma.
 */
public class LogicaSnake {

    public static final int MIN_LADO = 5;
    private static final int LARGO_INICIAL = 3;

    public enum Estado { EN_CURSO, PERDIO, GANO }

    private final int ancho;
    private final int alto;
    private final Random azar;
    private final Serpiente serpiente;
    private Direccion direccion = Direccion.DERECHA;
    private Direccion pendiente = Direccion.DERECHA;
    private Punto comida;
    private int manzanas;
    private Estado estado = Estado.EN_CURSO;

    /**
     * @throws IllegalArgumentException si el tablero es menor a {@value #MIN_LADO} x {@value #MIN_LADO}
     */
    public LogicaSnake(int ancho, int alto, Random azar) {
        if (ancho < MIN_LADO || alto < MIN_LADO) {
            throw new IllegalArgumentException("El tablero debe ser de al menos "
                    + MIN_LADO + "x" + MIN_LADO + " (se pidio " + ancho + "x" + alto + ")");
        }
        this.ancho = ancho;
        this.alto = alto;
        this.azar = azar;
        this.serpiente = new Serpiente(new Punto(ancho / 2, alto / 2), LARGO_INICIAL, Direccion.IZQUIERDA);
        colocarComida();
    }

    /**
     * Pide un cambio de direccion para el proximo paso.
     *
     * @throws MovimientoInvalidoException si la partida termino o si se intenta girar 180 grados
     */
    public void cambiarDireccion(Direccion nueva) throws MovimientoInvalidoException {
        if (estado != Estado.EN_CURSO) {
            throw new MovimientoInvalidoException("La partida ya termino");
        }
        // se compara con la direccion ya aplicada para que dos teclas rapidas no inviertan la serpiente
        if (nueva.esOpuesta(direccion)) {
            throw new MovimientoInvalidoException("No se puede girar en sentido contrario");
        }
        pendiente = nueva;
    }

    /**
     * Avanza un paso.
     *
     * @return el estado despues del paso
     * @throws IllegalStateException si se llama con la partida ya terminada
     */
    public Estado avanzar() {
        if (estado != Estado.EN_CURSO) {
            throw new IllegalStateException("No se puede avanzar: la partida ya termino");
        }
        direccion = pendiente;
        Punto nueva = serpiente.getCabeza().mover(direccion);
        if (!dentro(nueva)) {
            estado = Estado.PERDIO;
            return estado;
        }
        boolean come = nueva.equals(comida);
        // moverse a la celda de la cola es valido si la cola se desplaza en este mismo paso
        boolean choca = serpiente.contiene(nueva) && (come || !nueva.equals(serpiente.getCola()));
        if (choca) {
            estado = Estado.PERDIO;
            return estado;
        }
        serpiente.mover(nueva, come);
        if (come) {
            manzanas++;
            colocarComida();
        }
        return estado;
    }

    private boolean dentro(Punto p) {
        return p.x() >= 0 && p.x() < ancho && p.y() >= 0 && p.y() < alto;
    }

    /** Pone la comida en una celda libre al azar; si no queda ninguna, la partida se gana. */
    private void colocarComida() {
        List<Punto> libres = new ArrayList<>();
        for (int x = 0; x < ancho; x++) {
            for (int y = 0; y < alto; y++) {
                Punto p = new Punto(x, y);
                if (!serpiente.contiene(p)) {
                    libres.add(p);
                }
            }
        }
        if (libres.isEmpty()) {
            comida = null;
            estado = Estado.GANO;
        } else {
            comida = libres.get(azar.nextInt(libres.size()));
        }
    }

    /** Solo para pruebas: fija la posicion de la comida. */
    void setComida(Punto p) {
        comida = p;
    }

    public int getAncho() {
        return ancho;
    }

    public int getAlto() {
        return alto;
    }

    public Serpiente getSerpiente() {
        return serpiente;
    }

    public Punto getComida() {
        return comida;
    }

    public int getManzanas() {
        return manzanas;
    }

    public Estado getEstado() {
        return estado;
    }
}
