package sonidos;

/** Interruptor global de sonido: silencia la musica de fondo y los efectos de los juegos. */
public final class ControlAudio {

    private static volatile boolean silenciado;

    private ControlAudio() { }

    public static boolean isSilenciado() {
        return silenciado;
    }

    /** Activa o desactiva el silencio; la musica se detiene o se reanuda al instante. */
    public static void setSilenciado(boolean valor) {
        silenciado = valor;
        if (valor) {
            MusicaAmbiente.detener();
        } else {
            MusicaAmbiente.iniciar();
        }
    }
}
