package juegos.tetris;

import java.util.Random;

/** Reglas del juego independientes de Swing. Coordenadas: x horizontal, y vertical. */
public final class LogicaTetris {
    public static final int ANCHO = 10;
    public static final int ALTO = 20;

    private static final int[] PUNTOS_LINEAS = {0, 100, 300, 500, 800};

    private final Pieza[][] tablero = new Pieza[ALTO][ANCHO];
    private final Random aleatorio;
    private Pieza piezaActual;
    private Pieza proximaPieza;
    private boolean[][] forma;
    private int piezaX;
    private int piezaY;
    private int puntos;
    private int lineas;
    private boolean terminado;

    public LogicaTetris(Random aleatorio) {
        if (aleatorio == null) {
            throw new IllegalArgumentException("El generador aleatorio es obligatorio");
        }
        this.aleatorio = aleatorio;
        proximaPieza = sortear();
        generarPieza();
    }

    private Pieza sortear() {
        Pieza[] piezas = Pieza.values();
        return piezas[aleatorio.nextInt(piezas.length)];
    }

    private void generarPieza() {
        piezaActual = proximaPieza;
        proximaPieza = sortear();
        forma = piezaActual.crearForma();
        piezaX = 3;
        piezaY = 0;
        if (!cabe(forma, piezaX, piezaY)) {
            terminado = true;
        }
    }

    private boolean cabe(boolean[][] candidata, int nuevoX, int nuevoY) {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                if (!candidata[y][x]) {
                    continue;
                }
                int tx = nuevoX + x;
                int ty = nuevoY + y;
                if (tx < 0 || tx >= ANCHO || ty >= ALTO ||
                        (ty >= 0 && tablero[ty][tx] != null)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void mover(int desplazamiento) {
        if (!terminado && cabe(forma, piezaX + desplazamiento, piezaY)) {
            piezaX += desplazamiento;
        }
    }

    /** Gira 90 grados en sentido horario; intenta un ajuste lateral sencillo. */
    public void girar() {
        if (terminado || piezaActual == Pieza.O) {
            return;
        }
        boolean[][] rotada = new boolean[4][4];
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                rotada[x][3 - y] = forma[y][x];
            }
        }
        for (int ajuste : new int[]{0, -1, 1, -2, 2}) {
            if (cabe(rotada, piezaX + ajuste, piezaY)) {
                forma = rotada;
                piezaX += ajuste;
                return;
            }
        }
    }

    /** Baja una fila; si no cabe, fija la pieza y genera otra. */
    public void bajar() {
        if (terminado) {
            return;
        }
        if (cabe(forma, piezaX, piezaY + 1)) {
            piezaY++;
        } else {
            fijarPieza();
        }
    }

    /** Deja caer la pieza hasta el fondo y concede 2 puntos por fila recorrida. */
    public void caidaRapida() {
        if (terminado) {
            return;
        }
        int recorrido = 0;
        while (cabe(forma, piezaX, piezaY + 1)) {
            piezaY++;
            recorrido++;
        }
        puntos += recorrido * 2;
        fijarPieza();
    }

    private void fijarPieza() {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                if (forma[y][x]) {
                    int ty = piezaY + y;
                    if (ty < 0) {
                        terminado = true;
                        return;
                    }
                    tablero[ty][piezaX + x] = piezaActual;
                }
            }
        }
        eliminarLineas();
        generarPieza();
    }

    private void eliminarLineas() {
        int eliminadas = 0;
        for (int y = ALTO - 1; y >= 0; y--) {
            boolean completa = true;
            for (int x = 0; x < ANCHO; x++) {
                if (tablero[y][x] == null) {
                    completa = false;
                    break;
                }
            }
            if (completa) {
                eliminadas++;
                for (int fila = y; fila > 0; fila--) {
                    System.arraycopy(tablero[fila - 1], 0, tablero[fila], 0, ANCHO);
                }
                tablero[0] = new Pieza[ANCHO];
                y++; // volver a revisar la fila que acaba de bajar
            }
        }
        lineas += eliminadas;
        puntos += PUNTOS_LINEAS[eliminadas];
    }

    public Pieza getCelda(int x, int y) {
        if (x < 0 || x >= ANCHO || y < 0 || y >= ALTO) {
            throw new IndexOutOfBoundsException("Celda fuera del tablero");
        }
        return tablero[y][x];
    }

    public boolean ocupaPiezaActual(int x, int y) {
        int relativoX = x - piezaX;
        int relativoY = y - piezaY;
        return relativoX >= 0 && relativoX < 4 && relativoY >= 0 && relativoY < 4
                && forma[relativoY][relativoX];
    }

    public Pieza getPiezaActual() { return piezaActual; }
    public Pieza getProximaPieza() { return proximaPieza; }
    public int getPuntos() { return puntos; }
    public int getLineas() { return lineas; }
    public boolean isTerminado() { return terminado; }
}
