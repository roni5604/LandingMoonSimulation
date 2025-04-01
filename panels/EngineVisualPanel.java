package panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import constants.SpacecraftConstants;

/*
 * EngineVisualPanel.java
 *
 * Shows the engines visually.
 * Displays the main engine and 8 secondary engines in their designated positions.
 * The color changes (yellow when active) indicate current thrust status.
 */
public class EngineVisualPanel extends JPanel {
    EngineSlidersPanel sliders;

    String mainEngine = "MHT";
    String[] secondaryEngines = {
            "FR1", "FR2",
            "FL1", "FL2",
            "BL1", "BL2",
            "BR1", "BR2"
    };

    HashMap<String, Point> positions = new HashMap<>();

    public EngineVisualPanel(EngineSlidersPanel slidersPanel) {
        sliders = slidersPanel;
        setBackground(Color.WHITE);
        Timer timer = new Timer(50, e -> repaint());
        timer.start();
    }

    void calculatePositions(int w, int h) {
        positions.clear();
        int cx = w / 2;
        int cy = h / 2;
        positions.put(mainEngine, new Point(cx, cy));

        int offsetX = 100;
        int offsetY = 120;
        int spacing = 30;

        positions.put("FR1", new Point(cx + offsetX, cy - offsetY));
        positions.put("FR2", new Point(cx + offsetX + spacing, cy - offsetY));

        positions.put("FL1", new Point(cx - offsetX, cy - offsetY));
        positions.put("FL2", new Point(cx - offsetX - spacing, cy - offsetY));

        positions.put("BL1", new Point(cx - offsetX, cy + offsetY));
        positions.put("BL2", new Point(cx - offsetX - spacing, cy + offsetY));

        positions.put("BR1", new Point(cx + offsetX, cy + offsetY));
        positions.put("BR2", new Point(cx + offsetX + spacing, cy + offsetY));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        calculatePositions(getWidth(), getHeight());
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.GRAY);
        // Connect secondary engines with a continuous line.
        for (int i = 0; i < secondaryEngines.length; i++) {
            Point current = positions.get(secondaryEngines[i]);
            Point next = positions.get(secondaryEngines[(i + 1) % secondaryEngines.length]);
            g2d.drawLine(current.x, current.y, next.x, next.y);
        }

        // Draw main engine.
        drawEngine(g2d, mainEngine, positions.get(mainEngine), 70, (int) SpacecraftConstants.MAIN_ENG_F);

        // Draw secondary engines.
        for (String eng : secondaryEngines) {
            drawEngine(g2d, eng, positions.get(eng), 30, (int) SpacecraftConstants.SECOND_ENG_F);
        }
    }

    void drawEngine(Graphics2D g2d, String lbl, Point p, int r, int power) {
        double thrust = sliders.engineThrust.getOrDefault(lbl, 0.0);
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
