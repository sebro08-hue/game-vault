package juegos.ahorcado;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Reglas del Ahorcado independientes de Swing. Se adivinan palabras una tras otra;
 * cada palabra da 10 puntos por letra mas 5 por cada vida que sobro. La partida
 * termina con 6 errores en una misma palabra.
 */
public final class LogicaAhorcado {
    public static final int MAX_ERRORES = 6;

    private static final int PUNTOS_POR_LETRA = 10;
    private static final int PUNTOS_POR_VIDA = 5;

    /** Lo que paso al probar una letra. */
    public enum Resultado { IGNORADA, ACIERTO, PALABRA_COMPLETA, FALLO, PERDIO }

    private final BancoPalabras banco;
    private final Set<Character> letrasUsadas = new HashSet<>();
    private String palabra;
    private int errores;
    private int puntos;
    private int palabrasAdivinadas;
    private int ultimosPuntosGanados;
    private boolean palabraResuelta;
    private boolean terminado;

    public LogicaAhorcado(Random aleatorio) {
        banco = new BancoPalabras(aleatorio);
        siguientePalabra();
    }

    /** Pasa a una palabra nueva; los puntos se conservan. */
    public void siguientePalabra() {
        if (terminado) {
            return;
        }
        palabra = banco.siguiente();
        letrasUsadas.clear();
        errores = 0;
        palabraResuelta = false;
    }

    public Resultado probar(char letra) {
        char mayuscula = Character.toUpperCase(letra);
        if (terminado || palabraResuelta || letrasUsadas.contains(mayuscula)) {
            return Resultado.IGNORADA;
        }
        letrasUsadas.add(mayuscula);

        if (palabra.indexOf(mayuscula) >= 0) {
            if (!palabraCompleta()) {
                return Resultado.ACIERTO;
            }
            ultimosPuntosGanados = palabra.length() * PUNTOS_POR_LETRA
                    + (MAX_ERRORES - errores) * PUNTOS_POR_VIDA;
            puntos += ultimosPuntosGanados;
            palabrasAdivinadas++;
            palabraResuelta = true;
            return Resultado.PALABRA_COMPLETA;
        }

        errores++;
        if (errores >= MAX_ERRORES) {
            terminado = true;
            return Resultado.PERDIO;
        }
        return Resultado.FALLO;
    }

    private boolean palabraCompleta() {
        for (char c : palabra.toCharArray()) {
            if (!letrasUsadas.contains(c)) {
                return false;
            }
        }
        return true;
    }

    /** La palabra con guiones bajos en las letras que faltan, separada por espacios. */
    public String getPalabraVisible() {
        StringBuilder texto = new StringBuilder();
        for (char c : palabra.toCharArray()) {
            boolean mostrar = terminado || letrasUsadas.contains(c);
            texto.append(mostrar ? c : '_').append(' ');
        }
        return texto.toString().trim();
    }

    public boolean fueUsada(char letra) { return letrasUsadas.contains(Character.toUpperCase(letra)); }
    public String getPalabra() { return palabra; }
    public int getErrores() { return errores; }
    public int getVidas() { return MAX_ERRORES - errores; }
    public int getPuntos() { return puntos; }
    public int getPalabrasAdivinadas() { return palabrasAdivinadas; }
    public int getUltimosPuntosGanados() { return ultimosPuntosGanados; }
    public boolean isPalabraResuelta() { return palabraResuelta; }
    public boolean isTerminado() { return terminado; }
}
