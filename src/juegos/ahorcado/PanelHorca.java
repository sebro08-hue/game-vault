package juegos.ahorcado;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/** Dibuja la horca y agrega una parte del muneco por cada error. */
final class PanelHorca extends JPanel {

    private int errores;
    private boolean perdio;

    PanelHorca() {
        setPreferredSize(new Dimension(220, 260));
        setBackground(new Color(250, 250, 245));
        setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
    }

    void setErrores(int errores, boolean perdio) {
        this.errores = Math.min(errores, LogicaAhorcado.MAX_ERRORES);
        this.perdio = perdio;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int base = h - 25;
            int poste = w / 4;
            int viga = h / 9;
            int cuerdaX = w * 2 / 3;

            // Estructura
            g2.setColor(new Color(121, 85, 72));
            g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(20, base, w - 20, base);
            g2.drawLine(poste, base, poste, viga);
            g2.drawLine(poste, viga, cuerdaX, viga);
            g2.drawLine(poste, viga + 35, poste + 35, viga);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(cuerdaX, viga, cuerdaX, viga + 25);

            // Muneco
            g2.setColor(perdio ? new Color(192, 57, 43) : new Color(44, 62, 80));
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int radio = 18;
            int cabezaY = viga + 25;
            int cuelloY = cabezaY + radio * 2;
            int caderaY = cuelloY + 65;

            if (errores >= 1) {
                g2.drawOval(cuerdaX - radio, cabezaY, radio * 2, radio * 2);
                if (perdio) {
                    int ojoY = cabezaY + 12;
                    g2.setStroke(new BasicStroke(2));
                    g2.drawLine(cuerdaX - 9, ojoY, cuerdaX - 3, ojoY + 6);
                    g2.drawLine(cuerdaX - 3, ojoY, cuerdaX - 9, ojoY + 6);
                    g2.drawLine(cuerdaX + 3, ojoY, cuerdaX + 9, ojoY + 6);
                    g2.drawLine(cuerdaX + 9, ojoY, cuerdaX + 3, ojoY + 6);
                    g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                }
            }
            if (errores >= 2) {
                g2.drawLine(cuerdaX, cuelloY, cuerdaX, caderaY);
            }
            if (errores >= 3) {
                g2.drawLine(cuerdaX, cuelloY + 15, cuerdaX - 30, cuelloY + 45);
            }
            if (errores >= 4) {
                g2.drawLine(cuerdaX, cuelloY + 15, cuerdaX + 30, cuelloY + 45);
            }
            if (errores >= 5) {
                g2.drawLine(cuerdaX, caderaY, cuerdaX - 25, caderaY + 45);
            }
            if (errores >= 6) {
                g2.drawLine(cuerdaX, caderaY, cuerdaX + 25, caderaY + 45);
            }
        } finally {
            g2.dispose();
        }
    }
}
