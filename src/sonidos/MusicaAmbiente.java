package sonidos;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Banda sonora original arcade de 8 bits (90 segundos por vuelta).
 * Funciona desde el menu y durante los juegos, sin bloquear el hilo Swing.
 * El reproductor de efectos es independiente.
 */
public final class MusicaAmbiente {
    private static final int FRECUENCIA = 22050;
    private static final int BPM = 128;
    private static final int COMPASES = 48;
    private static final int PASOS_COMPAS = 16;
    private static final int PASOS = COMPASES * PASOS_COMPAS;
    private static final double SEGUNDOS_PASO = 60.0 / BPM / 4.0;
    private static final AudioFormat FORMATO = new AudioFormat(FRECUENCIA, 16, 1, true, false);
    private static final AtomicBoolean ACTIVA = new AtomicBoolean(false);

    // Cada grupo de 4 compases tiene una identidad musical diferente.
    // La progresion se mantiene tonal para que el final enlace con el inicio.
    private static final int[] RAICES = {60, 57, 53, 55}; // Do, La menor, Fa, Sol.
    private static final int[] SECCIONES = {
            0, 0, 1, 1, 2, 2, 3, 3, 4, 2, 1, 0
    };
    // Intervalos en semitonos sobre la raiz del acorde, -1 = silencio.
    private static final int[][] MOTIVOS = {
            {12,-1,-1,-1, 16,-1,19,-1, 16,-1,-1,-1, 14,-1,12,-1},
            {19,-1,16,-1, 12,-1,14,-1, 16,-1,19,-1, 21,-1,19,-1},
            {12,-1,16,19, -1,16,-1,14, 12,-1, 7,-1, 12,14,16,-1},
            {-1,-1, 7,-1, 12,-1,14,-1, 16,-1,-1,-1, 14,-1,12,-1},
            {24,-1,-1,21, 19,-1,16,-1, 17,-1,16,-1, 14,-1,12,-1},
            {12,-1,14,-1, 16,19,-1,-1, 21,-1,19,-1, 16,14,12,-1},
            {-1,-1,-1,-1, 12,-1,-1,-1, 16,-1,-1,-1, 19,-1,-1,-1},
            {19,-1,21,-1, 24,-1,21,-1, 19,-1,16,-1, 14,-1,12,-1}
    };
    private static volatile Clip reproductor;
    private static byte[] pista; // se compone una sola vez (unos 2 s) y se reutiliza

    private MusicaAmbiente() { }

    /** Inicia una sola pista y la repite hasta cerrar la plataforma. */
    public static void iniciar() {
        if (ControlAudio.isSilenciado() || !ACTIVA.compareAndSet(false, true)) {
            return;
        }
        Thread hilo = new Thread(() -> {
            Clip nuevo = null;
            try {
                byte[] audio = obtenerPista();
                nuevo = AudioSystem.getClip();
                nuevo.open(FORMATO, audio, 0, audio.length);
                synchronized (MusicaAmbiente.class) {
                    if (!ACTIVA.get()) {
                        nuevo.close();
                        return;
                    }
                    reproductor = nuevo;
                    nuevo.loop(Clip.LOOP_CONTINUOUSLY);
                }
            } catch (Exception | LinkageError error) {
                if (nuevo != null) {
                    nuevo.close();
                }
                ACTIVA.set(false);
                // La ausencia de audio no debe impedir usar Game Vault.
            }
        }, "game-vault-musica-ambiente");
        hilo.setDaemon(true);
        hilo.start();
    }

    /** Para la musica y libera el dispositivo al cerrar el gestor. */
    public static synchronized void detener() {
        ACTIVA.set(false);
        Clip actual = reproductor;
        reproductor = null;
        if (actual != null) {
            actual.stop();
            actual.close();
        }
    }

    private static synchronized byte[] obtenerPista() {
        if (pista == null) {
            pista = componer();
        }
        return pista;
    }

    /** Composicion sintetizada, PCM 16 bits mono. Exactamente 90 segundos a 128 BPM. */
    private static byte[] componer() {
        int total = (int) Math.round(COMPASES * 4 * 60.0 / BPM * FRECUENCIA);
        byte[] pcm = new byte[total * 2];
        double muestrasPorPaso = (double) total / PASOS;
        int[] notas = new int[PASOS];
        int[] bajos = new int[PASOS];
        double[] amplitudes = new double[PASOS];

        for (int paso = 0; paso < PASOS; paso++) {
            int compas = paso / PASOS_COMPAS;
            int pos = paso % PASOS_COMPAS;
            int grupo = compas / 4;
            int seccion = SECCIONES[grupo];
            int raiz = RAICES[compas % 4];
            int patron = (seccion + (compas % 4 == 3 ? 1 : 0)) % MOTIVOS.length;
            int intervalo = MOTIVOS[patron][pos];

            // Melodia: cambios de registro, espacios y pequenas respuestas.
            if (seccion == 0 && grupo == 0 && pos % 4 != 0) intervalo = -1;
            if (seccion == 3 && pos % 2 != 0) intervalo = -1;
            if (seccion == 4 && pos == 0) intervalo = 12;
            if (compas == 47 && pos >= 12) intervalo = -1;
            if (intervalo >= 0 && seccion == 4 && intervalo >= 19) intervalo -= 12;
            notas[paso] = intervalo < 0 ? -1 : raiz + intervalo;

            // El bajo alterna notas del acorde sin invadir los efectos del juego.
            bajos[paso] = raiz - 12 + ((pos / 4) % 2 == 1 ? 7 : 0);
            amplitudes[paso] = seccion == 3 ? 0.75 : (seccion == 1 || seccion == 2 ? 1.0 : 0.85);
        }

        for (int i = 0; i < total; i++) {
            int paso = Math.min(PASOS - 1, (int) (i / muestrasPorPaso));
            int inicioPaso = (int) Math.round(paso * muestrasPorPaso);
            double tiempoPaso = Math.max(0, (i - inicioPaso) / (double) FRECUENCIA);
            double tiempo = i / (double) FRECUENCIA;
            int pos = paso % PASOS_COMPAS;
            int grupo = (paso / PASOS_COMPAS) / 4;
            int seccion = SECCIONES[grupo];

            double envNota = Math.min(1, tiempoPaso / 0.008)
                    * Math.max(0, Math.min(1, (SEGUNDOS_PASO - tiempoPaso) / 0.028));
            double melodia = notas[paso] < 0 ? 0
                    : cuadrada(frecuenciaMidi(notas[paso]), tiempoPaso) * 0.31 * envNota * amplitudes[paso];

            double tiempoPulso = ((i - (int) Math.round((paso / 4) * 4 * muestrasPorPaso))
                    / (double) FRECUENCIA);
            double envBajo = Math.max(0, Math.min(1, tiempoPulso / 0.015))
                    * Math.max(0, 1 - tiempoPulso / 0.40);
            double bajo = triangular(frecuenciaMidi(bajos[paso]), tiempo) * 0.22 * envBajo;

            int raiz = RAICES[(paso / PASOS_COMPAS) % 4];
            int[] intervalos = {0, 7, 12, 7, 4, 7, 12, 7};
            double arpegio = (seccion == 3 ? 0.045 : 0.075)
                    * triangular(frecuenciaMidi(raiz + intervalos[pos % 8]), tiempo)
                    * envNota;

            // Percusion electronica discreta; mas presente en los estribillos.
            double percusion = 0;
            if (seccion != 3) {
                if (pos % 8 == 0 && tiempoPaso < 0.16) {
                    percusion += Math.sin(2 * Math.PI * (78 - 48 * tiempoPaso) * tiempoPaso)
                            * Math.exp(-35 * tiempoPaso) * 0.25;
                }
                if (pos % 4 == 2 && tiempoPaso < 0.065) {
                    percusion += ruido(i) * Math.exp(-60 * tiempoPaso) * 0.065;
                }
            }

            // Deja margen de volumen a los efectos y suaviza la union del loop.
            double mezcla = (melodia + bajo + arpegio + percusion) * 0.075;
            double segundosFinal = (total - 1 - i) / (double) FRECUENCIA;
            double fade = Math.min(1, Math.min(tiempo / 0.12, segundosFinal / 0.12));
            short valor = (short) Math.round(Math.max(-1, Math.min(1, mezcla * Math.max(0, fade))) * 32767);
            pcm[2 * i] = (byte) valor;
            pcm[2 * i + 1] = (byte) (valor >>> 8);
        }
        return pcm;
    }

    private static double frecuenciaMidi(int nota) {
        return 440.0 * Math.pow(2.0, (nota - 69) / 12.0);
    }

    private static double cuadrada(double hz, double t) {
        return Math.sin(2 * Math.PI * hz * t) >= 0 ? 1 : -1;
    }

    private static double triangular(double hz, double t) {
        double fase = (hz * t) % 1;
        return 1 - 4 * Math.abs(fase - 0.5);
    }

    private static double ruido(int muestra) {
        int n = muestra * 1664525 + 1013904223;
        n ^= n >>> 16;
        return (n & 0xFFFF) / 32767.5 - 1;
    }
}
