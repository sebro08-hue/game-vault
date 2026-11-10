package gestor;

import gestor.api.GameFunction;
import juegos.demo.DemoGame;
import juegos.memoria.MemoriaGame;
import juegos.snake.SnakeGame;

import java.util.List;
import java.util.function.Supplier;

/**
 * Registro de los juegos disponibles. Para agregar un juego (propio o de otro grupo)
 * basta con anadir una entrada en {@link #JUEGOS} con su nombre y la forma de obtener
 * su instancia unica (normalmente {@code MiJuego::getInstance}).
 */
public final class CargadorJuegos {

    /** Un juego registrado: nombre visible (tambien usado como llave de records) y su fabrica. */
    public record JuegoInfo(String nombre, Supplier<GameFunction> fabrica) {
        public GameFunction obtener() {
            return fabrica.get();
        }
    }

    private static final List<JuegoInfo> JUEGOS = List.of(
            new JuegoInfo("Click Rapido", DemoGame::getInstance),
            new JuegoInfo("Memoria", MemoriaGame::getInstance),
            new JuegoInfo("Snake", SnakeGame::getInstance)
    );

    private CargadorJuegos() {
    }

    public static List<JuegoInfo> getJuegos() {
        return JUEGOS;
    }
}
