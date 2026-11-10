package juegos.snake;

/** Una celda de la cuadricula. */
public record Punto(int x, int y) {

    public Punto mover(Direccion d) {
        return new Punto(x + d.getDx(), y + d.getDy());
    }
}
