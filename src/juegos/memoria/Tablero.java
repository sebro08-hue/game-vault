package juegos.memoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Tablero de memoria: contiene las cartas barajadas y las reglas de volteo.
 *
 * <p><b>Patron Iterador:</b> el tablero implementa {@link Iterable} y entrega un iterador
 * propio ({@link IteradorCartas}). Asi quien lo use (la interfaz grafica, la revision de
 * "partida completa") recorre las cartas sin conocer que estan en una lista, y la
 * estructura interna puede cambiar sin afectar a esos clientes.
 */
public class Tablero implements Iterable<Carta> {

    public static final String[] SIMBOLOS = {"★", "♥", "♦", "♣", "♠", "☀", "☂", "♫"};
    public static final int MIN_PARES = 2;

    /** Resultado de voltear una carta. */
    public enum Resultado { PRIMERA, ACIERTO, FALLO }

    private final List<Carta> cartas = new ArrayList<>();
    private Carta pendiente;      // primera carta volteada de la jugada en curso
    private Carta falla1;         // cartas falladas, descubiertas hasta ocultarFallidas()
    private Carta falla2;
    private int intentos;

    public Tablero(int pares) throws TableroInvalidoException {
        this(pares, new Random());
    }

    /** Constructor con generador de azar propio (permite pruebas reproducibles). */
    public Tablero(int pares, Random azar) throws TableroInvalidoException {
        if (pares < MIN_PARES || pares > SIMBOLOS.length) {
            throw new TableroInvalidoException("Las parejas deben estar entre " + MIN_PARES
                    + " y " + SIMBOLOS.length + " (se pidieron " + pares + ")");
        }
        for (int i = 0; i < pares; i++) {
            cartas.add(new Carta(SIMBOLOS[i]));
            cartas.add(new Carta(SIMBOLOS[i]));
        }
        Collections.shuffle(cartas, azar);
    }

    public int size() {
        return cartas.size();
    }

    public int getIntentos() {
        return intentos;
    }

    /**
     * Voltea la carta de la posicion indicada.
     *
     * @throws JugadaInvalidaException si la posicion no existe, la carta ya esta descubierta
     *                                 o aun no se han ocultado las cartas falladas
     */
    public Resultado voltear(int indice) throws JugadaInvalidaException {
        if (indice < 0 || indice >= cartas.size()) {
            throw new JugadaInvalidaException("La carta " + indice + " no existe");
        }
        if (falla1 != null) {
            throw new JugadaInvalidaException("Espera a que se oculten las cartas falladas");
        }
        Carta carta = cartas.get(indice);
        if (carta.getEstado() != Carta.Estado.OCULTA) {
            throw new JugadaInvalidaException("Esa carta ya esta descubierta");
        }
        carta.setEstado(Carta.Estado.VOLTEADA);
        if (pendiente == null) {
            pendiente = carta;
            return Resultado.PRIMERA;
        }
        intentos++;
        Carta primera = pendiente;
        pendiente = null;
        if (primera.getSimbolo().equals(carta.getSimbolo())) {
            primera.setEstado(Carta.Estado.EMPAREJADA);
            carta.setEstado(Carta.Estado.EMPAREJADA);
            return Resultado.ACIERTO;
        }
        falla1 = primera;
        falla2 = carta;
        return Resultado.FALLO;
    }

    /** Vuelve a ocultar las dos cartas de la ultima jugada fallida (si la hubo). */
    public void ocultarFallidas() {
        if (falla1 != null) {
            falla1.setEstado(Carta.Estado.OCULTA);
            falla2.setEstado(Carta.Estado.OCULTA);
            falla1 = null;
            falla2 = null;
        }
    }

    /** @return {@code true} si todas las cartas estan emparejadas. */
    public boolean estaCompleto() {
        for (Carta carta : this) {
            if (carta.getEstado() != Carta.Estado.EMPAREJADA) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Iterator<Carta> iterator() {
        return new IteradorCartas();
    }

    /** Iterador propio sobre las cartas, en el orden en que quedaron en el tablero. */
    private class IteradorCartas implements Iterator<Carta> {

        private int posicion = 0;

        @Override
        public boolean hasNext() {
            return posicion < cartas.size();
        }

        @Override
        public Carta next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No hay mas cartas en el tablero");
            }
            return cartas.get(posicion++);
        }

        // remove() no se soporta: las cartas no se quitan del tablero
        // (la implementacion por defecto lanza UnsupportedOperationException).
    }
}
