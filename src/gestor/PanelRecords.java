package gestor;

import gestor.CargadorJuegos.JuegoInfo;
import gestor.api.Stat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

/** Ventana interna que muestra el top 3 de cada juego. */
public class PanelRecords extends JInternalFrame {

    private final GestorRecords records;
    private final JPanel contenido = new JPanel();

    public PanelRecords(GestorRecords records) {
        super("Mejores resultados", true, true, true, true);
        this.records = records;
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        add(new JScrollPane(contenido), BorderLayout.CENTER);
        setSize(420, 380);
        setLocation(120, 40);
        actualizar();
    }

    /** Vuelve a leer los records y reconstruye las tablas. */
    public void actualizar() {
        contenido.removeAll();
        for (JuegoInfo juego : CargadorJuegos.getJuegos()) {
            contenido.add(crearSeccion(juego.nombre()));
        }
        contenido.revalidate();
        contenido.repaint();
    }

    private JPanel crearSeccion(String juego) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Puesto", "Jugador", "Rubro", "Valor"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        List<Stat> top = records.getTop(juego);
        for (int i = 0; i < GestorRecords.TOP; i++) {
            if (i < top.size()) {
                Stat s = top.get(i);
                modelo.addRow(new Object[]{i + 1, s.getNombre(), s.getClave(), s.getValor()});
            } else {
                modelo.addRow(new Object[]{i + 1, "-", "-", "-"});
            }
        }
        JTable tabla = new JTable(modelo);
        tabla.setEnabled(false);
        JPanel seccion = new JPanel(new BorderLayout());
        seccion.setBorder(BorderFactory.createTitledBorder(juego));
        seccion.add(tabla.getTableHeader(), BorderLayout.NORTH);
        seccion.add(tabla, BorderLayout.CENTER);
        return seccion;
    }
}
