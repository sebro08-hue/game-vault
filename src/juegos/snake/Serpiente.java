package juegos.snake;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;

/** Cuerpo de la serpiente: la cabeza es el primer elemento y la cola el ultimo. */
public class Serpiente implements Iterable<Punto> {

    private final Deque<Punto> cuerpo = new ArrayDeque<>();

    /** Crea una serpiente recta de {@code largo} celdas, con la cola hacia {@code haciaCola}. */
    Serpiente(Punto cabeza, int largo, Direccion haciaCola) {
        Punto actual = cabeza;
        for (int i = 0; i < largo; i++) {
            cuerpo.addLast(actual);
            actual = actual.mover(haciaCola);
        }
    }

    public Punto getCabeza() {
        return cuerpo.peekFirst();
    }

    public Punto getCola() {
        return cuerpo.peekLast();
    }

    public int largo() {
        return cuerpo.size();
    }

    public boolean contiene(Punto p) {
        return cuerpo.contains(p);
    }

    /** Avanza la cabeza a {@code nueva}; si no crece, se quita la ultima celda de la cola. */
    void mover(Punto nueva, boolean crecer) {
        cuerpo.addFirst(nueva);
        if (!crecer) {
            cuerpo.removeLast();
        }
    }

    /** Recorre el cuerpo de la cabeza a la cola (solo lectura). */
    @Override
    public Iterator<Punto> iterator() {
        return Collections.unmodifiableCollection(cuerpo).iterator();
    }
}
