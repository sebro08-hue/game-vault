package juegos.tetris;

import gestor.api.GameFunction;
import gestor.api.Stat;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

/** Juego Tetris compatible con Game Vault. Instancia unica (Singleton). */
public final class TetrisGame extends JInternalFrame implements GameFunction {
    private static final int CELDA = 25;
    private static final int RETARDO_INICIAL = 600;
    private static final int RETARDO_MINIMO = 130;
    private static final Color FONDO = new Color(22, 27, 35);
    private static TetrisGame instancia;

    private final JLabel estado = new JLabel("", SwingConstants.CENTER);
    private final JLabel controlesMovimiento = new JLabel(
            "Mover: \u2190 / \u2192 o A / D    Girar: \u2191 o W", SwingConstants.CENTER);
    private final JLabel controlesAccion = new JLabel(
            "Bajar: \u2193 o S    Caida: Espacio    Pausa: P", SwingConstants.CENTER);
    private final PanelTablero panel = new PanelTablero();
    private final Timer reloj = new Timer(RETARDO_INICIAL, e -> avanzar());
    private LogicaTetris logica;
    private Stat ultimoResultado;
    private boolean pausado;
    private boolean iniciado;

    private TetrisGame() {
        super("Tetris", true, true, false, true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        estado.setFont(estado.getFont().deriveFont(Font.BOLD, 13f));
        controlesMovimiento.setFont(controlesMovimiento.getFont().deriveFont(12f));
        controlesAccion.setFont(controlesAccion.getFont().deriveFont(12f));
        JPanel cabecera = new JPanel(new GridLayout(3, 1, 0, 2));
        cabecera.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 6, 5, 6));
        cabecera.add(estado);
        cabecera.add(controlesMovimiento);
        cabecera.add(controlesAccion);
        add(cabecera, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
        configurarTeclas();
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                panel.requestFocusInWindow();
            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                if (reloj.isRunning()) {
                    pausar();
                }
            }

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                reloj.stop();
            }
        });
        pack();

    }

    @Override
    public void setVisible(boolean visible) {
        // El gestor agrega primero el juego al escritorio y luego lo hace visible.
        if (visible) {
            ajustarAlEscritorio();
        }
        super.setVisible(visible);
    }

    private void ajustarAlEscritorio() {
        Container contenedor = getParent();
        if (contenedor == null) {
            return;
        }
        int margen = 12;
        int ancho = Math.min(420, Math.max(1, contenedor.getWidth() - 2 * margen));
        int alto = Math.min(625, Math.max(1, contenedor.getHeight() - 2 * margen));
        setSize(ancho, alto);
        setLocation(Math.max(0, (contenedor.getWidth() - ancho) / 2),
                    Math.max(0, (contenedor.getHeight() - alto) / 2));
        revalidate();
        repaint();
    }

    public static synchronized TetrisGame getInstance() {
        if (instancia == null) {
            instancia = new TetrisGame();
        }
        return instancia;
    }

    @Override
    public void iniciar() {
        reloj.stop();
        logica = new LogicaTetris(new Random());
        ultimoResultado = null;
        iniciado = false;
        pausado = false;
        actualizarEstado();
        panel.repaint();
    }

    @Override
    public Stat getStats() {
        return ultimoResultado;
    }

    private void configurarTeclas() {
        vincular("LEFT", "izquierda", () -> ejecutar(() -> logica.mover(-1)));
        vincular("A", "izquierda", null);
        vincular("RIGHT", "derecha", () -> ejecutar(() -> logica.mover(1)));
        vincular("D", "derecha", null);
        vincular("UP", "girar", () -> ejecutar(logica::girar));
        vincular("W", "girar", null);
        vincular("DOWN", "bajar", () -> ejecutar(logica::bajar));
        vincular("S", "bajar", null);
        vincular("SPACE", "caida", () -> ejecutar(logica::caidaRapida));
        vincular("P", "pausar", this::pausar);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                panel.requestFocusInWindow();
            }
        });
    }

    private void vincular(String tecla, String nombre, Runnable accion) {
        panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(tecla), nombre);
        if (accion != null) {
            panel.getActionMap().put(nombre, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    accion.run();
                }
            });
        }
    }

    private void ejecutar(Runnable accion) {
        if (logica == null || logica.isTerminado() || pausado) {
            return;
        }
        if (!iniciado) {
            iniciado = true;
            reloj.start();
        }
        accion.run();
        comprobarFin();
        actualizarEstado();
        panel.repaint();
    }

    private void avanzar() {
        if (logica == null || logica.isTerminado() || pausado) {
            reloj.stop();
            return;
        }
        logica.bajar();
        comprobarFin();
        reloj.setDelay(Math.max(RETARDO_MINIMO, RETARDO_INICIAL - (logica.getLineas() / 5) * 65));
        actualizarEstado();
        panel.repaint();
    }

    private void pausar() {
        if (logica == null || logica.isTerminado() || !iniciado) {
            return;
        }
        pausado = !pausado;
        if (pausado) {
            reloj.stop();
        } else {
            reloj.start();
        }
        actualizarEstado();
        panel.repaint();
    }

    private void comprobarFin() {
        if (logica.isTerminado()) {
            reloj.stop();
            // El gestor completa el nombre al cerrar el Internal Frame.
            ultimoResultado = new Stat("Puntos", null, logica.getPuntos());
        }
    }

    private void actualizarEstado() {
        if (logica != null) {
            estado.setText("Puntos: " + logica.getPuntos() + "     Lineas: " + logica.getLineas());
        }
    }

    private final class PanelTablero extends JPanel {
        PanelTablero() {
            setPreferredSize(new Dimension(LogicaTetris.ANCHO * CELDA, LogicaTetris.ALTO * CELDA));
            setBackground(FONDO);
            setFocusable(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                if (logica == null) {
                    return;
                }
                // Se ajusta la celda al espacio real sin recortar filas ni columnas.
                int celda = Math.max(1, Math.min(getWidth() / LogicaTetris.ANCHO,
                        getHeight() / LogicaTetris.ALTO));
                int anchoTablero = LogicaTetris.ANCHO * celda;
                int altoTablero = LogicaTetris.ALTO * celda;
                int desplazamientoX = (getWidth() - anchoTablero) / 2;
                int desplazamientoY = (getHeight() - altoTablero) / 2;
                g2.translate(desplazamientoX, desplazamientoY);
                for (int y = 0; y < LogicaTetris.ALTO; y++) {
                    for (int x = 0; x < LogicaTetris.ANCHO; x++) {
                        Pieza pieza = logica.getCelda(x, y);
                        if (logica.ocupaPiezaActual(x, y) && !logica.isTerminado()) {
                            pieza = logica.getPiezaActual();
                        }
                        g2.setColor(pieza != null ? pieza.getColor() : FONDO);
                        g2.fillRect(x * celda, y * celda, celda, celda);
                        g2.setColor(new Color(42, 47, 57));
                        g2.drawRect(x * celda, y * celda, celda - 1, celda - 1);
                    }
                }
                if (logica.isTerminado()) {
                    cartel(g2, anchoTablero, altoTablero, "FIN - " + logica.getPuntos() + " puntos", "Cierra para guardar");
                } else if (pausado) {
                    cartel(g2, anchoTablero, altoTablero, "PAUSA", "Presiona P para continuar");
                } else if (!iniciado) {
                    cartel(g2, anchoTablero, altoTablero, "TETRIS", "Usa una flecha para comenzar");
                }
            } finally {
                g2.dispose();
            }
        }

        private void cartel(Graphics2D g, int ancho, int alto, String titulo, String subtitulo) {
            g.setColor(new Color(8, 13, 20, 220));
            g.fillRect(0, alto / 2 - 54, ancho, 108);
            textoCentrado(g, ancho, titulo, alto / 2 - 8, 20, Font.BOLD);
            textoCentrado(g, ancho, subtitulo, alto / 2 + 24, 12, Font.PLAIN);
        }

        private void textoCentrado(Graphics2D g, int ancho, String texto, int y, int tamano, int estilo) {
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, estilo, tamano));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(texto, (ancho - fm.stringWidth(texto)) / 2, y);
        }
    }
}
