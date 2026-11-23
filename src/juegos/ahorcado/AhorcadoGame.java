package juegos.ahorcado;

import gestor.api.GameFunction;
import gestor.api.Stat;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Juego Ahorcado compatible con el gestor de juegos. Instancia unica (Singleton). */
public final class AhorcadoGame extends JInternalFrame implements GameFunction {
    private static final int ESPERA_SIGUIENTE_MS = 1300;
    private static final String[] FILAS_TECLADO = {"QWERTYUIOP", "ASDFGHJKLÑ", "ZXCVBNM"};
    private static final Color VERDE = new Color(39, 174, 96);
    private static final Color ROJO = new Color(192, 57, 43);
    private static AhorcadoGame instancia;

    private final PanelHorca panelHorca = new PanelHorca();
    private final JLabel lblPalabra = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblMensaje = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblEstado = new JLabel();
    private final Map<Character, JButton> teclas = new HashMap<>();
    private final JPanel contenido = new JPanel(new BorderLayout(10, 10));
    private final Timer temporizadorSiguiente = new Timer(ESPERA_SIGUIENTE_MS, e -> siguientePalabra());
    private LogicaAhorcado logica;
    private Stat ultimoResultado;

    private AhorcadoGame() {
        super("Ahorcado", false, true, false, true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        temporizadorSiguiente.setRepeats(false);
        construirInterfaz();
        configurarTecladoFisico();
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                contenido.requestFocusInWindow();
            }

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                temporizadorSiguiente.stop();
            }
        });
        iniciar();
        pack();
    }

    public static synchronized AhorcadoGame getInstance() {
        if (instancia == null) {
            instancia = new AhorcadoGame();
        }
        return instancia;
    }

    @Override
    public void setVisible(boolean visible) {
        // El gestor agrega primero el juego al escritorio y luego lo hace visible.
        if (visible) {
            centrarEnEscritorio();
        }
        super.setVisible(visible);
    }

    private void centrarEnEscritorio() {
        Container contenedor = getParent();
        if (contenedor == null) {
            return;
        }
        setLocation(Math.max(0, (contenedor.getWidth() - getWidth()) / 2),
                    Math.max(0, (contenedor.getHeight() - getHeight()) / 2));
    }

    @Override
    public void iniciar() {
        temporizadorSiguiente.stop();
        logica = new LogicaAhorcado(new Random());
        ultimoResultado = null;
        prepararPalabra();
        contenido.requestFocusInWindow();
    }

    @Override
    public Stat getStats() {
        return ultimoResultado;
    }

    private void construirInterfaz() {
        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD, 13f));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        lblPalabra.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        lblPalabra.setAlignmentX(CENTER_ALIGNMENT);
        lblMensaje.setFont(lblMensaje.getFont().deriveFont(Font.BOLD, 14f));
        lblMensaje.setAlignmentX(CENTER_ALIGNMENT);
        JLabel lblInstruccion = new JLabel("Use el teclado o haga clic en las letras");
        lblInstruccion.setForeground(Color.GRAY);
        lblInstruccion.setAlignmentX(CENTER_ALIGNMENT);

        JPanel derecha = new JPanel();
        derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS));
        derecha.setPreferredSize(new Dimension(340, 260));
        derecha.add(Box.createVerticalGlue());
        derecha.add(lblPalabra);
        derecha.add(Box.createVerticalStrut(25));
        derecha.add(lblMensaje);
        derecha.add(Box.createVerticalStrut(10));
        derecha.add(lblInstruccion);
        derecha.add(Box.createVerticalGlue());

        JPanel centro = new JPanel(new BorderLayout(12, 0));
        centro.add(panelHorca, BorderLayout.WEST);
        centro.add(derecha, BorderLayout.CENTER);

        JPanel teclado = new JPanel(new GridLayout(FILAS_TECLADO.length, 1, 0, 5));
        for (String fila : FILAS_TECLADO) {
            JPanel panelFila = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            for (char letra : fila.toCharArray()) {
                JButton tecla = new JButton(String.valueOf(letra));
                tecla.setFont(tecla.getFont().deriveFont(Font.BOLD, 13f));
                tecla.setMargin(new Insets(2, 2, 2, 2));
                tecla.setPreferredSize(new Dimension(50, 34));
                tecla.setFocusable(false);
                tecla.addActionListener(e -> probarLetra(letra));
                teclas.put(letra, tecla);
                panelFila.add(tecla);
            }
            teclado.add(panelFila);
        }

        contenido.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
        contenido.setFocusable(true);
        contenido.add(lblEstado, BorderLayout.NORTH);
        contenido.add(centro, BorderLayout.CENTER);
        contenido.add(teclado, BorderLayout.SOUTH);
        setContentPane(contenido);
    }

    // Permite escribir las letras con el teclado de la computadora
    private void configurarTecladoFisico() {
        InputMap entradas = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap acciones = getRootPane().getActionMap();
        for (String fila : FILAS_TECLADO) {
            for (char letra : fila.toCharArray()) {
                String accion = "letra" + letra;
                entradas.put(KeyStroke.getKeyStroke(Character.valueOf(Character.toLowerCase(letra)), 0), accion);
                entradas.put(KeyStroke.getKeyStroke(Character.valueOf(letra), 0), accion);
                entradas.put(KeyStroke.getKeyStroke(Character.valueOf(letra), InputEvent.SHIFT_DOWN_MASK), accion);
                acciones.put(accion, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        probarLetra(letra);
                    }
                });
            }
        }
    }

    private void siguientePalabra() {
        logica.siguientePalabra();
        prepararPalabra();
    }

    private void prepararPalabra() {
        for (JButton tecla : teclas.values()) {
            tecla.setEnabled(true);
        }
        panelHorca.setErrores(0, false);
        lblPalabra.setForeground(Color.BLACK);
        lblMensaje.setText(" ");
        actualizar();
    }

    private void probarLetra(char letra) {
        if (logica == null) {
            return;
        }
        LogicaAhorcado.Resultado resultado = logica.probar(letra);
        if (resultado == LogicaAhorcado.Resultado.IGNORADA) {
            return;
        }
        teclas.get(letra).setEnabled(false);
        panelHorca.setErrores(logica.getErrores(), logica.isTerminado());

        if (resultado == LogicaAhorcado.Resultado.PALABRA_COMPLETA) {
            habilitarTeclas(false);
            lblPalabra.setForeground(VERDE);
            lblMensaje.setForeground(VERDE);
            lblMensaje.setText("¡Correcto! +" + logica.getUltimosPuntosGanados() + " puntos");
            temporizadorSiguiente.restart();
        } else if (resultado == LogicaAhorcado.Resultado.PERDIO) {
            habilitarTeclas(false);
            lblPalabra.setForeground(ROJO);
            lblMensaje.setForeground(ROJO);
            lblMensaje.setText("<html><center>FIN - " + logica.getPuntos() + " puntos<br>"
                    + "Cierra para guardar</center></html>");
            // El gestor completa el nombre al cerrar el Internal Frame.
            ultimoResultado = new Stat("Puntos", null, logica.getPuntos());
        }
        actualizar();
    }

    private void habilitarTeclas(boolean habilitar) {
        for (JButton tecla : teclas.values()) {
            tecla.setEnabled(habilitar);
        }
    }

    private void actualizar() {
        String visible = logica.getPalabraVisible();
        lblPalabra.setFont(lblPalabra.getFont().deriveFont(logica.getPalabra().length() > 10 ? 22f : 28f));
        lblPalabra.setText(visible);
        lblEstado.setText("Puntos: " + logica.getPuntos()
                + "     Palabras: " + logica.getPalabrasAdivinadas()
                + "     Vidas: " + logica.getVidas() + "/" + LogicaAhorcado.MAX_ERRORES);
    }
}
