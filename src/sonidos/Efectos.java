package sonidos;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Efectos sintetizados originales, sin archivos de audio ni bibliotecas externas.
 * Se reproducen fuera del hilo de Swing para no bloquear el movimiento de los juegos.
 */
public final class Efectos {
    private static final int FRECUENCIA = 22050;
    private static final AudioFormat FORMATO = new AudioFormat(FRECUENCIA, 16, 1, true, false);
    private static final ThreadPoolExecutor REPRODUCTOR = new ThreadPoolExecutor(
            1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(12), tarea -> {
                Thread hilo = new Thread(tarea, "game-vault-audio");
                hilo.setDaemon(true);
                return hilo;
            }, new ThreadPoolExecutor.DiscardPolicy());

    public enum Sonido {
        CLICK_DIEZ, CLICK_FIN, CARTA, PAREJA, MEMORIA_GANAR,
        MANZANA, SNAKE_PERDER, SNAKE_GANAR,
        TETRIS_BAJAR, TETRIS_FIJAR, TETRIS_LINEAS,
        TETRIS_CUATRO_LINEAS, TETRIS_PERDER, TETRIS_CIEN
    }

    private Efectos() { }

    /** No lanza errores si el equipo no dispone de un dispositivo de audio. */
    public static void reproducir(Sonido sonido) {
        if (sonido == null || ControlAudio.isSilenciado()) {
            return;
        }
        REPRODUCTOR.execute(() -> {
            try {
                byte[] audio = generar(sonido);
                SourceDataLine salida = AudioSystem.getSourceDataLine(FORMATO);
                try {
                    salida.open(FORMATO);
                    salida.start();
                    salida.write(audio, 0, audio.length);
                    salida.drain();
                } finally {
                    salida.close();
                }
            } catch (Exception | LinkageError ignorado) {
                // El juego sigue funcionando incluso sin parlantes o sin sistema de audio.
            }
        });
    }

    private static byte[] generar(Sonido sonido) {
        return switch (sonido) {
            case CLICK_DIEZ -> secuencia(new int[]{880, 1175}, new int[]{75, 115}, 0.28, false);
            case CLICK_FIN -> secuencia(new int[]{784, 659, 523, 392}, new int[]{120, 120, 150, 260}, 0.28, false);
            case CARTA -> secuencia(new int[]{480}, new int[]{65}, 0.16, true);
            case PAREJA -> secuencia(new int[]{660, 990}, new int[]{75, 105}, 0.23, false);
            case MEMORIA_GANAR -> secuencia(new int[]{523, 659, 784, 1047}, new int[]{105, 105, 105, 240}, 0.30, false);
            case MANZANA -> secuencia(new int[]{740, 990}, new int[]{55, 90}, 0.23, false);
            case SNAKE_PERDER -> secuencia(new int[]{390, 310, 220}, new int[]{110, 120, 240}, 0.28, false);
            case SNAKE_GANAR -> secuencia(new int[]{523, 659, 784, 1047, 1319}, new int[]{90, 90, 90, 130, 270}, 0.29, false);
            case TETRIS_BAJAR -> secuencia(new int[]{230}, new int[]{38}, 0.45, false);
            case TETRIS_FIJAR -> secuencia(new int[]{185, 145}, new int[]{65, 95}, 0.18, false);
            case TETRIS_LINEAS -> secuencia(new int[]{523, 784, 1047}, new int[]{70, 75, 125}, 0.25, false);
            case TETRIS_CUATRO_LINEAS -> secuencia(new int[]{523, 659, 784, 1047, 1319}, new int[]{70, 70, 70, 100, 260}, 0.34, false);
            case TETRIS_PERDER -> secuencia(new int[]{440, 330, 220, 165}, new int[]{115, 115, 150, 260}, 0.27, false);
            case TETRIS_CIEN -> secuencia(new int[]{880, 1175, 1568}, new int[]{55, 55, 105}, 0.23, false);
        };
    }

    private static byte[] secuencia(int[] notas, int[] duracionesMs, double volumen, boolean ruido) {
        int totalMuestras = 0;
        for (int duracion : duracionesMs) {
            totalMuestras += duracion * FRECUENCIA / 1000;
        }
        byte[] resultado = new byte[totalMuestras * 2];
        int posicion = 0;
        int semilla = 1234567;
        for (int n = 0; n < notas.length; n++) {
            int muestras = duracionesMs[n] * FRECUENCIA / 1000;
            for (int i = 0; i < muestras; i++) {
                double t = (double) i / FRECUENCIA;
                double ataque = Math.min(1.0, i / (FRECUENCIA * 0.004));
                double salida = Math.pow(1.0 - (double) i / muestras, 1.8);
                double onda;
                if (ruido) {
                    semilla = semilla * 1664525 + 1013904223;
                    onda = ((semilla >>> 16) / 32767.5 - 1.0) * 0.55
                            + Math.sin(2 * Math.PI * notas[n] * t) * 0.45;
                } else {
                    onda = Math.sin(2 * Math.PI * notas[n] * t)
                            + 0.17 * Math.sin(4 * Math.PI * notas[n] * t);
                }
                short muestra = (short) (Math.max(-1.0, Math.min(1.0,
                        onda * volumen * ataque * salida)) * 32767);
                resultado[posicion++] = (byte) (muestra & 0xff);
                resultado[posicion++] = (byte) ((muestra >>> 8) & 0xff);
            }
        }
        return resultado;
    }
}
