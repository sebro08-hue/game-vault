package juegos.snake;

import gestor.api.GameFunction;
import gestor.api.Stat;
import sonidos.Efectos;
import sonidos.Efectos.Sonido;


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
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

/**
 * Snake clasico: comer la mayor cantidad de manzanas sin chocar contra la pared ni contra
 * el propio cuerpo. Singleton, JInternalFrame y {@link GameFunction}.
 *
 * <p>Controles: flechas o WASD para moverse, P o espacio para pausar.
 * Estadistica (mas alto es mejor): cantidad de manzanas comidas.
 */
public class SnakeGame extends JInternalFrame implements GameFunction {

    private static final int ANCHO = 20;
    private static final int ALTO = 20;
    private static final int CELDA = 22;
    private static final int MARGEN_TABLERO = 16;
    private static final int DELAY_INICIAL = 140;
    private static final int DELAY_MINIMO = 60;
    private static final int ACELERACION = 4;   // ms menos por manzana

    private static final Color FONDO = new Color(25, 30, 35);
    private static final Color CUERPO = new Color(80, 190, 90);
    private static final Color CABEZA = new Color(40, 140, 60);
    private static final Color COMIDA = new Color(220, 60, 60);
    private static final Color BORDE = new Color(125, 165, 190);
    private static final Color CUADRICULA = new Color(39, 47, 53);

    private static SnakeGame instancia;

    private LogicaSnake logica;
    private boolean iniciado;
    private boolean pausado;
    private String mensaje;
    private Stat ultimoResultado;

    private final JLabel lblEstado = new JLabel("", SwingConstants.CENTER);
    private final PanelJuego panel = new PanelJuego();
    private final Timer reloj = new Timer(DELAY_INICIAL, e -> paso());

    private SnakeGame() {
        super("Snake", true, true, false, true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD, 14f));
        add(lblEstado, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
        configurarTeclas();
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                panel.requestFocusInWindow();
            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                // si el jugador cambia de ventana, la partida se pausa sola
                if (enMarcha()) {
                    alternarPausa();
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
        if (visible) {
            ajustarAlEscritorio();
        }
        super.setVisible(visible);
    }

    /** Mantiene la ventana dentro del escritorio y la centra al abrirse. */
    private void ajustarAlEscritorio() {
        Container escritorio = getParent();
        if (escritorio == null) {
            return;
        }
        int margen = 12;
        int ancho = Math.min(520, Math.max(1, escritorio.getWidth() - margen * 2));
        int alto = Math.min(555, Math.max(1, escritorio.getHeight() - margen * 2));
        setSize(ancho, alto);
        setLocation(Math.max(0, (escritorio.getWidth() - ancho) / 2),
                    Math.max(0, (escritorio.getHeight() - alto) / 2));
        revalidate();
        repaint();
    }

    /** @return la unica instancia del juego (se crea la primera vez que se pide). */
    public static synchronized SnakeGame getInstance() {
        if (instancia == null) {
            instancia = new SnakeGame();
        }
        return instancia;
    }

    @Override
    public void iniciar() {
        reloj.stop();
        reloj.setDelay(DELAY_INICIAL);
        ultimoResultado = null;
        iniciado = false;
        pausado = false;
        try {
            logica = new LogicaSnake(ANCHO, ALTO, new Random());
            mensaje = "Presiona una flecha\npara empezar";
        } catch (IllegalArgumentException e) {
            // no deberia ocurrir con las constantes, pero si pasa el juego no se cae
            logica = null;
            mensaje = "No se pudo crear el tablero:\n" + e.getMessage();
        }
        actualizarEstado();
        panel.repaint();
    }

    @Override
    public Stat getStats() {
        return ultimoResultado;
    }

    private void configurarTeclas() {
        enlazar("UP", "arriba", () -> girar(Direccion.ARRIBA));
        enlazar("W", "arriba", null);
        enlazar("DOWN", "abajo", () -> girar(Direccion.ABAJO));
        enlazar("S", "abajo", null);
        enlazar("LEFT", "izquierda", () -> girar(Direccion.IZQUIERDA));
        enlazar("A", "izquierda", null);
        enlazar("RIGHT", "derecha", () -> girar(Direccion.DERECHA));
        enlazar("D", "derecha", null);
        enlazar("P", "pausa", this::alternarPausa);
        enlazar("SPACE", "pausa", null);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                panel.requestFocusInWindow();
            }
        });
    }

    /** Asocia una tecla a una accion; con {@code accion == null} reutiliza una ya registrada. */
    private void enlazar(String tecla, String nombre, Runnable accion) {
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

    private boolean enMarcha() {
        return iniciado && !pausado && logica != null && logica.getEstado() == LogicaSnake.Estado.EN_CURSO;
    }

    private void girar(Direccion d) {
        if (logica == null || logica.getEstado() != LogicaSnake.Estado.EN_CURSO) {
            return;
        }
        try {
            logica.cambiarDireccion(d);
        } catch (MovimientoInvalidoException e) {
            // girar 180 grados no esta permitido: se ignora la tecla y se sigue igual
        }
        if (!iniciado) {
            iniciado = true;
            mensaje = null;
            reloj.start();
        } else if (pausado) {
            alternarPausa();
        }
        panel.repaint();
    }

    private void alternarPausa() {
        if (!iniciado || logica == null || logica.getEstado() != LogicaSnake.Estado.EN_CURSO) {
            return;
        }
        pausado = !pausado;
        if (pausado) {
            reloj.stop();
            mensaje = "Pausa\n(P o una flecha para seguir)";
        } else {
            mensaje = null;
            reloj.start();
        }
        panel.repaint();
    }

    private void paso() {
        int manzanasPrevias = logica.getManzanas();
        LogicaSnake.Estado estado;
        try {
            estado = logica.avanzar();
        } catch (IllegalStateException e) {
            // el reloj disparo despues de terminar la partida: solo se detiene
            reloj.stop();
            return;
        }
        if (logica.getManzanas() > manzanasPrevias) {
            Efectos.reproducir(Sonido.MANZANA);
        }
        reloj.setDelay(Math.max(DELAY_MINIMO, DELAY_INICIAL - ACELERACION * logica.getManzanas()));
        if (estado != LogicaSnake.Estado.EN_CURSO) {
            terminar(estado);
        }
        actualizarEstado();
        panel.repaint();
    }

    private void terminar(LogicaSnake.Estado estado) {
        reloj.stop();
        Efectos.reproducir(estado == LogicaSnake.Estado.GANO
                ? Sonido.SNAKE_GANAR : Sonido.SNAKE_PERDER);
        int manzanas = logica.getManzanas();
        // el nombre queda null: la plataforma pone el del jugador de la sesion
        ultimoResultado = new Stat("Manzanas", null, manzanas);
        mensaje = (estado == LogicaSnake.Estado.GANO ? "Ganaste! " : "Fin del juego: ")
                + manzanas + " manzanas\nCierra la ventana para guardar";
    }

    private void actualizarEstado() {
        int manzanas = logica == null ? 0 : logica.getManzanas();
        lblEstado.setText("Manzanas: " + manzanas + "   |   Flechas/WASD: mover   P: pausa");
    }

    /** Dibuja el tablero, la comida, la serpiente y los mensajes. */
    private class PanelJuego extends JPanel {

        PanelJuego() {
            int lado = ANCHO * CELDA + MARGEN_TABLERO * 2;
            setPreferredSize(new Dimension(lado, lado));
            setBackground(FONDO);
            setFocusable(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Cada celda ocupa un numero entero de pixeles. El tablero cabe
                // siempre completo y permanece centrado al redimensionar la ventana.
                int celda = Math.max(1, Math.min(
                        (getWidth() - 2 * MARGEN_TABLERO) / ANCHO,
                        (getHeight() - 2 * MARGEN_TABLERO) / ALTO));
                int anchoTablero = ANCHO * celda;
                int altoTablero = ALTO * celda;
                int origenX = (getWidth() - anchoTablero) / 2;
                int origenY = (getHeight() - altoTablero) / 2;

                g2.setColor(CUADRICULA);
                for (int x = 1; x < ANCHO; x++) {
                    int lineaX = origenX + x * celda;
                    g2.drawLine(lineaX, origenY, lineaX, origenY + altoTablero);
                }
                for (int y = 1; y < ALTO; y++) {
                    int lineaY = origenY + y * celda;
                    g2.drawLine(origenX, lineaY, origenX + anchoTablero, lineaY);
                }

                if (logica != null) {
                    Punto comida = logica.getComida();
                    int separacion = Math.max(1, celda / 10);
                    if (comida != null) {
                        g2.setColor(COMIDA);
                        g2.fillOval(origenX + comida.x() * celda + separacion,
                                origenY + comida.y() * celda + separacion,
                                Math.max(1, celda - 2 * separacion),
                                Math.max(1, celda - 2 * separacion));
                    }
                    Serpiente serpiente = logica.getSerpiente();
                    for (Punto p : serpiente) {
                        g2.setColor(p.equals(serpiente.getCabeza()) ? CABEZA : CUERPO);
                        g2.fillRoundRect(origenX + p.x() * celda + 1,
                                origenY + p.y() * celda + 1,
                                Math.max(1, celda - 2), Math.max(1, celda - 2), 6, 6);
                    }
                }

                // Limite real del mapa: cuatro lados visibles, incluso en los bordes.
                g2.setColor(BORDE);
                g2.setStroke(new java.awt.BasicStroke(3f));
                g2.drawRect(origenX, origenY, anchoTablero, altoTablero);

                if (mensaje != null) {
                    dibujarMensaje(g2, mensaje.split("\n"));
                }
            } finally {
                g2.dispose();
            }
        }

        private void dibujarMensaje(Graphics2D g2, String[] lineas) {
            g2.setFont(getFont().deriveFont(Font.BOLD, 16f));
            FontMetrics fm = g2.getFontMetrics();
            int altoLinea = fm.getHeight();
            int anchoMax = 0;
            for (String l : lineas) {
                anchoMax = Math.max(anchoMax, fm.stringWidth(l));
            }
            int cajaAncho = anchoMax + 30;
            int cajaAlto = altoLinea * lineas.length + 20;
            int x = (getWidth() - cajaAncho) / 2;
            int y = (getHeight() - cajaAlto) / 2;
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(x, y, cajaAncho, cajaAlto, 12, 12);
            g2.setColor(Color.WHITE);
            for (int i = 0; i < lineas.length; i++) {
                int lx = (getWidth() - fm.stringWidth(lineas[i])) / 2;
                g2.drawString(lineas[i], lx, y + 10 + fm.getAscent() + i * altoLinea);
            }
        }
    }
}
