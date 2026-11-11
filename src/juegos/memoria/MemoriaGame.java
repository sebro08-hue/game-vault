package juegos.memoria;

import gestor.api.GameFunction;
import gestor.api.Stat;
import sonidos.Efectos;
import sonidos.Efectos.Sonido;


import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Juego de memoria: encontrar todas las parejas de cartas con la menor cantidad de
 * intentos y de tiempo. Singleton, JInternalFrame y {@link GameFunction}.
 *
 * <p>Puntaje (mas alto es mejor): {@code 1000 - 25 * intentos - 5 * segundos}, minimo 0.
 */
public class MemoriaGame extends JInternalFrame implements GameFunction {

    private static final int PARES = 8;
    private static final int COLUMNAS = 4;
    private static final int MS_OCULTAR = 800;
    private static final Color COLOR_OCULTA = new Color(70, 110, 180);
    private static final Color COLOR_VOLTEADA = Color.WHITE;
    private static final Color COLOR_EMPAREJADA = new Color(150, 210, 150);

    private static MemoriaGame instancia;

    private Tablero tablero;
    private int segundos;
    private Stat ultimoResultado;

    private final JLabel lblEstado = new JLabel("", SwingConstants.CENTER);
    private final JPanel panelCartas = new JPanel(new GridLayout(0, COLUMNAS, 6, 6));
    private final List<JButton> botones = new ArrayList<>();
    private final Timer reloj = new Timer(1000, e -> {
        segundos++;
        actualizarEstado();
    });
    private final Timer ocultador = new Timer(MS_OCULTAR, e -> ocultarFallidas());

    private MemoriaGame() {
        super("Memoria", true, true, false, false);
        ocultador.setRepeats(false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD, 14f));
        panelCartas.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(lblEstado, BorderLayout.NORTH);
        add(panelCartas, BorderLayout.CENTER);
        setSize(380, 440);
        setLocation(80, 60);
        // al cerrar la ventana se detienen los relojes (la partida queda sin terminar)
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                detenerRelojes();
            }
        });
    }

    /** @return la unica instancia del juego (se crea la primera vez que se pide). */
    public static synchronized MemoriaGame getInstance() {
        if (instancia == null) {
            instancia = new MemoriaGame();
        }
        return instancia;
    }

    @Override
    public void iniciar() {
        detenerRelojes();
        ultimoResultado = null;
        segundos = 0;
        try {
            tablero = new Tablero(PARES);
        } catch (TableroInvalidoException e) {
            // no deberia ocurrir con la constante PARES, pero si pasa no se rompe la plataforma
            tablero = null;
            panelCartas.removeAll();
            lblEstado.setText("No se pudo crear el tablero");
            JOptionPane.showMessageDialog(this, e.getMessage(), "Memoria", JOptionPane.ERROR_MESSAGE);
            return;
        }
        construirBotones();
        refrescar();
        actualizarEstado();
        reloj.start();
    }

    @Override
    public Stat getStats() {
        return ultimoResultado;
    }

    private void construirBotones() {
        panelCartas.removeAll();
        botones.clear();
        for (int i = 0; i < tablero.size(); i++) {
            final int indice = i;
            JButton boton = new JButton();
            boton.setFont(boton.getFont().deriveFont(Font.BOLD, 26f));
            boton.setFocusPainted(false);
            boton.addActionListener(e -> voltear(indice));
            botones.add(boton);
            panelCartas.add(boton);
        }
        panelCartas.revalidate();
        panelCartas.repaint();
    }

    private void voltear(int indice) {
        if (tablero == null) {
            return;
        }
        Tablero.Resultado resultado;
        try {
            resultado = tablero.voltear(indice);
        } catch (JugadaInvalidaException e) {
            // jugada ignorada: se informa en la barra de estado sin interrumpir la partida
            lblEstado.setText(e.getMessage());
            return;
        }
        if (resultado == Tablero.Resultado.ACIERTO) {
            Efectos.reproducir(Sonido.PAREJA);
        } else {
            Efectos.reproducir(Sonido.CARTA);
        }
        refrescar();
        actualizarEstado();
        if (resultado == Tablero.Resultado.FALLO) {
            ocultador.restart();
        } else if (resultado == Tablero.Resultado.ACIERTO && tablero.estaCompleto()) {
            terminar();
        }
    }

    private void ocultarFallidas() {
        if (tablero != null) {
            tablero.ocultarFallidas();
            refrescar();
        }
    }

    private void terminar() {
        reloj.stop();
        Efectos.reproducir(Sonido.MEMORIA_GANAR);
        int puntaje = Math.max(0, 1000 - 25 * tablero.getIntentos() - 5 * segundos);
        // el nombre queda null: la plataforma pone el del jugador de la sesion
        ultimoResultado = new Stat("Puntaje", null, puntaje);
        lblEstado.setText("Completado! Puntaje: " + puntaje + ". Cierra la ventana para guardar.");
    }

    private void detenerRelojes() {
        reloj.stop();
        ocultador.stop();
    }

    /** Pinta cada boton segun el estado de su carta, recorriendo el tablero con su iterador. */
    private void refrescar() {
        int i = 0;
        for (Carta carta : tablero) {
            JButton boton = botones.get(i++);
            switch (carta.getEstado()) {
                case OCULTA -> {
                    boton.setText("");
                    boton.setBackground(COLOR_OCULTA);
                    boton.setEnabled(true);
                }
                case VOLTEADA -> {
                    boton.setText(carta.getSimbolo());
                    boton.setBackground(COLOR_VOLTEADA);
                    boton.setEnabled(true);
                }
                case EMPAREJADA -> {
                    boton.setText(carta.getSimbolo());
                    boton.setBackground(COLOR_EMPAREJADA);
                    boton.setEnabled(false);
                }
            }
        }
    }

    private void actualizarEstado() {
        lblEstado.setText("Tiempo: " + segundos + " s   |   Intentos: " + tablero.getIntentos());
    }
}
