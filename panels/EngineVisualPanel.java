/*
 * EngineVisualPanel.java
 *
 * תיאור:
 * פאנל זה מציג את מצב המנועים בצורה גרפית.
 * הוא מציג עיגול עבור המנוע הראשי ועיגולים קטנים עבור המנועים המשניים,
 * עם קווים המחברים ביניהם.
 * צבע העיגול משתנה לצהוב כאשר ערך הדחף (thrust) שונה מאפס.
 */

package panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import constants.SpacecraftConstants;

public class EngineVisualPanel extends JPanel {
    EngineSlidersPanel sliders;
    String mainEngine = "MHT";
    String[] secondaryEngines = {"14", "24", "21", "1-1", "12", "22", "23", "13"};
    HashMap<String, Point> positions = new HashMap<>();

    public EngineVisualPanel(EngineSlidersPanel slidersPanel) {
        sliders = slidersPanel;
        setBackground(Color.WHITE);
        Timer timer = new Timer(50, e -> repaint());
        timer.start();
    }

    void calculatePositions(int w, int h) {
        positions.put(mainEngine, new Point(w/2, h/2));
        double step = 2 * Math.PI / 8;
        int r = 120, cx = w/2, cy = h/2;
        for (int i = 0; i < 8; i++) {
            positions.put(secondaryEngines[i], new Point(
                    cx + (int)(r * Math.cos(i * step)),
                    cy + (int)(r * Math.sin(i * step))
            ));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        calculatePositions(getWidth(), getHeight());
        Graphics2D g2d = (Graphics2D) g;

        // קווים המחברים בין מנועים משניים
        for (int i = 0; i < 8; i++) {
            Point curr = positions.get(secondaryEngines[i]);
            Point next = positions.get(secondaryEngines[(i + 1) % 8]);
            g2d.setColor(Color.BLACK);
            g2d.drawLine(curr.x, curr.y, next.x, next.y);
        }

        drawEngine(g2d, mainEngine, positions.get(mainEngine), 70, (int) SpacecraftConstants.MAIN_ENG_F);
        for (String eng : secondaryEngines) {
            drawEngine(g2d, eng, positions.get(eng), 30, (int) SpacecraftConstants.SECOND_ENG_F);
        }
    }

    void drawEngine(Graphics2D g2d, String lbl, Point p, int r, int power) {
        double thrust = sliders.engineThrust.get(lbl);
        boolean active = Math.abs(thrust) > 1e-5;
        g2d.setColor(active ? Color.YELLOW : Color.LIGHT_GRAY);
        g2d.fillOval(p.x - r, p.y - r, 2 * r, 2 * r);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(p.x - r, p.y - r, 2 * r, 2 * r);

        String text = lbl + " (" + power + "N)";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, p.x - fm.stringWidth(text) / 2, p.y + fm.getHeight() / 4);
    }
}
