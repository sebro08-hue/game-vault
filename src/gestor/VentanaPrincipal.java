package gestor;

import gestor.CargadorJuegos.JuegoInfo;
import gestor.api.GameFunction;
import gestor.api.Stat;
import sonidos.ControlAudio;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.util.HashSet;
import java.util.Set;

/**
 * Pantalla principal: un escritorio de ventanas internas con dos opciones (elegir un
 * juego o ver los mejores resultados). Cada juego se muestra como JInternalFrame.
 */
public class VentanaPrincipal extends JFrame {

    private final JDesktopPane escritorio = new JDesktopPane();
    private final GestorRecords records;
    private final String jugador;
    private final PanelRecords panelRecords;
    private final Set<JInternalFrame> conOyente = new HashSet<>();

    public VentanaPrincipal(GestorRecords records, String jugador) {
        super("Game Vault - " + jugador);
        this.records = records;
        this.jugador = jugador;
        this.panelRecords = new PanelRecords(records);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setContentPane(escritorio);
        setJMenuBar(crearMenu());
        escritorio.add(panelRecords);
        escritorio.add(crearMenuPrincipal());
    }

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();
        JMenu menuJuegos = new JMenu("Juegos");
        for (JuegoInfo juego : CargadorJuegos.getJuegos()) {
            JMenuItem item = new JMenuItem(juego.nombre());
            item.addActionListener(e -> abrirJuego(juego));
            menuJuegos.add(item);
        }
        JMenu menuRecords = new JMenu("Records");
        JMenuItem verRecords = new JMenuItem("Ver mejores resultados");
        verRecords.addActionListener(e -> mostrarRecords());
        menuRecords.add(verRecords);
        JMenu menuArchivo = new JMenu("Archivo");
        JCheckBoxMenuItem silenciar = new JCheckBoxMenuItem("Silenciar sonido", ControlAudio.isSilenciado());
        silenciar.addActionListener(e -> ControlAudio.setSilenciado(silenciar.isSelected()));
        menuArchivo.add(silenciar);
        menuArchivo.addSeparator();
        JMenuItem salir = new JMenuItem("Salir");
        // igual que la X de la ventana: con EXIT_ON_CLOSE la aplicacion termina aunque
        // haya juegos con relojes activos (dispose() dejaba el proceso vivo)
        salir.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        menuArchivo.add(salir);
        barra.add(menuArchivo);
        barra.add(menuJuegos);
        barra.add(menuRecords);
        return barra;
    }

    /** Ventana interna de bienvenida con las dos opciones principales. */
    private JInternalFrame crearMenuPrincipal() {
        JInternalFrame marco = new JInternalFrame("Menu principal", false, false, false, false);
        JPanel botones = new JPanel(new GridLayout(2, 1, 10, 10));
        botones.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JButton btnJugar = new JButton("Seleccionar un juego");
        JButton btnRecords = new JButton("Mejores resultados");
        btnJugar.addActionListener(e -> mostrarSelectorJuegos());
        btnRecords.addActionListener(e -> mostrarRecords());
        botones.add(btnJugar);
        botones.add(btnRecords);
        marco.add(botones);
        marco.setSize(240, 150);
        marco.setLocation(20, 20);
        marco.setVisible(true);
        return marco;
    }

    private void mostrarSelectorJuegos() {
        DefaultListModel<JuegoInfo> modelo = new DefaultListModel<>();
        CargadorJuegos.getJuegos().forEach(modelo::addElement);
        JList<JuegoInfo> lista = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setCellRenderer((l, valor, i, sel, foco) -> {
            javax.swing.JLabel etiqueta = new javax.swing.JLabel(valor.nombre());
            etiqueta.setOpaque(true);
            etiqueta.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));
            if (sel) {
                etiqueta.setBackground(l.getSelectionBackground());
                etiqueta.setForeground(l.getSelectionForeground());
            }
            return etiqueta;
        });
        JInternalFrame marco = new JInternalFrame("Seleccionar juego", true, true, false, false);
        JButton btnJugar = new JButton("Jugar");
        Runnable jugar = () -> {
            JuegoInfo elegido = lista.getSelectedValue();
            if (elegido != null) {
                abrirJuego(elegido);
            }
        };
        btnJugar.addActionListener(e -> jugar.run());
        lista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    jugar.run();
                }
            }
        });
        marco.add(new JScrollPane(lista), BorderLayout.CENTER);
        marco.add(btnJugar, BorderLayout.SOUTH);
        marco.setSize(240, 220);
        marco.setLocation(280, 20);
        escritorio.add(marco);
        marco.setVisible(true);
        traerAlFrente(marco);
    }

    private void mostrarRecords() {
        panelRecords.actualizar();
        panelRecords.setVisible(true);
        traerAlFrente(panelRecords);
    }

    /**
     * Abre un juego. Si ya esta abierto solo se trae al frente (no se reinicia la partida
     * en curso); si no, se llama a iniciar() y se muestra.
     */
    private void abrirJuego(JuegoInfo info) {
        GameFunction juego = info.obtener();
        if (juego instanceof JInternalFrame marco && marco.isVisible()) {
            traerAlFrente(marco);
            return;
        }
        juego.iniciar();
        if (juego instanceof JInternalFrame marco) {
            if (marco.getParent() == null) {
                escritorio.add(marco);
            }
            if (conOyente.add(marco)) {
                marco.addInternalFrameListener(new InternalFrameAdapter() {
                    @Override
                    public void internalFrameClosing(InternalFrameEvent e) {
                        registrarPartida(info, juego);
                    }
                });
            }
            marco.setVisible(true);
            traerAlFrente(marco);
        }
    }

    /** Al cerrar un juego: si termino una partida, intenta guardarla en el top 3. */
    private void registrarPartida(JuegoInfo info, GameFunction juego) {
        Stat resultado = juego.getStats();
        if (resultado == null) {
            return;
        }
        if (resultado.getNombre() == null || resultado.getNombre().isBlank()) {
            resultado.setNombre(jugador);
        }
        try {
            if (records.registrar(info.nombre(), resultado)) {
                panelRecords.actualizar();
                JOptionPane.showInternalMessageDialog(escritorio,
                        "Nuevo record en " + info.nombre() + ": " + resultado.getValor(),
                        "Record", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (RecordsException e) {
            JOptionPane.showInternalMessageDialog(escritorio, e.getMessage(),
                    "Error al guardar el record", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void traerAlFrente(JInternalFrame marco) {
        try {
            if (marco.isIcon()) {
                marco.setIcon(false);
            }
            marco.toFront();
            marco.setSelected(true);
        } catch (PropertyVetoException ignorada) {
            // la ventana no acepto el foco; no es critico
        }
    }
}
