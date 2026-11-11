package juegos.tetris;

import java.awt.Color;

/** Los siete tetrominos y sus formas iniciales en una matriz de 4 x 4. */
public enum Pieza {
    I(new Color(0, 210, 220), new String[]{"....", "####", "....", "...."}),
    O(new Color(235, 205, 40), new String[]{".##.", ".##.", "....", "...."}),
    T(new Color(160, 90, 220), new String[]{".#..", "###.", "....", "...."}),
    S(new Color(65, 195, 95), new String[]{".##.", "##..", "....", "...."}),
    Z(new Color(225, 65, 80), new String[]{"##..", ".##.", "....", "...."}),
    J(new Color(70, 110, 230), new String[]{"#...", "###.", "....", "...."}),
    L(new Color(240, 150, 50), new String[]{"..#.", "###.", "....", "...."});

    private final Color color;
    private final boolean[][] base;

    Pieza(Color color, String[] filas) {
        this.color = color;
        this.base = new boolean[4][4];
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                base[y][x] = filas[y].charAt(x) == '#';
            }
        }
    }

    public Color getColor() {
        return color;
    }

    /** Devuelve una copia para que nadie modifique la forma del enum. */
    public boolean[][] crearForma() {
        boolean[][] copia = new boolean[4][4];
        for (int y = 0; y < 4; y++) {
            System.arraycopy(base[y], 0, copia[y], 0, 4);
        }
        return copia;
    }
}
