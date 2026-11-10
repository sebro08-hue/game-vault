package gestor;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.nio.file.Path;

/** Punto de entrada de la plataforma. Ejecutar desde la raiz del proyecto. */
public class Main {

    private static final Path ARCHIVO_RECORDS = Path.of("data", "records.txt");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::iniciar);
    }

    private static void iniciar() {
        String jugador = JOptionPane.showInputDialog(null, "Nombre del jugador:",
                "Game Vault", JOptionPane.QUESTION_MESSAGE);
        if (jugador == null || jugador.isBlank()) {
            jugador = "Jugador";
        }
        GestorRecords records = new GestorRecords(ARCHIVO_RECORDS);
        try {
            records.cargar();
            if (records.getLineasInvalidas() > 0) {
                JOptionPane.showMessageDialog(null, records.getLineasInvalidas()
                        + " linea(s) invalida(s) del archivo de records fueron omitidas.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (RecordsException e) {
            JOptionPane.showMessageDialog(null, e.getMessage() + "\nSe iniciara sin records previos.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        new VentanaPrincipal(records, jugador.trim()).setVisible(true);
    }
}
