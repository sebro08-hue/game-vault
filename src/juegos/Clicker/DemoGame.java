package juegos.Clicker;

import gestor.api.GameFunction;
import gestor.api.Stat;
import sonidos.Efectos;
import sonidos.Efectos.Sonido;


import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * Juego de prueba de la plataforma: hacer la mayor cantidad de clics en 10 segundos.
 * Es un Singleton y un JInternalFrame que implementa {@link GameFunction}.
 */
public class DemoGame extends JInternalFrame implements GameFunction {

    private static final int SEGUNDOS = 10;
    private static DemoGame instancia;

    private final JLabel lblInfo = new JLabel("", SwingConstants.CENTER);
    private final JButton btnClick = new JButton("Clic para empezar");
    private Timer reloj;
    private int clics;
    private int restantes;
    private boolean enCurso;
    private Stat ultimoResultado;

    private DemoGame() {
        super("Click Rapido", true, true, false, false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        lblInfo.setFont(lblInfo.getFont().deriveFont(Font.BOLD, 16f));
        btnClick.setFont(btnClick.getFont().deriveFont(Font.BOLD, 18f));
        btnClick.addActionListener(e -> clic());
        add(lblInfo, BorderLayout.NORTH);
        add(btnClick, BorderLayout.CENTER);
        setSize(320, 220);
        setLocation(60, 60);
    }

    /** @return la unica instancia del juego (se crea la primera vez que se pide). */
    public static synchronized DemoGame getInstance() {
        if (instancia == null) {
            instancia = new DemoGame();
        }
        return instancia;
    }

    @Override
    public void iniciar() {
        if (reloj != null) {
            reloj.stop();
        }
        clics = 0;
        restantes = SEGUNDOS;
        enCurso = false;
        ultimoResultado = null;
        btnClick.setEnabled(true);
        btnClick.setText("Clic para empezar");
        actualizarInfo();
    }

    @Override
    public Stat getStats() {
        return ultimoResultado;
    }

    private void clic() {
        if (!enCurso) {
            enCurso = true;
            reloj = new Timer(1000, e -> tick());
            reloj.start();
        }
        clics++;
        if (clics % 10 == 0) {
            Efectos.reproducir(Sonido.CLICK_DIEZ);
        }
        btnClick.setText("Clic! (" + clics + ")");
        actualizarInfo();
    }

    private void tick() {
        restantes--;
        actualizarInfo();
        if (restantes <= 0) {
            reloj.stop();
            Efectos.reproducir(Sonido.CLICK_FIN);
            enCurso = false;
            btnClick.setEnabled(false);
            // el nombre queda null: la plataforma pone el del jugador de la sesion
            ultimoResultado = new Stat("Clics", null, clics);
            lblInfo.setText("Fin! " + clics + " clics. Cierra la ventana para guardar.");
        }
    }

    private void actualizarInfo() {
        lblInfo.setText("Tiempo: " + restantes + " s   |   Clics: " + clics);
    }
}
