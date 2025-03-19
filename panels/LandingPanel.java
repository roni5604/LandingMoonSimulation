/*
 * LandingPanel.java
 *
 * Description:
 * This panel displays the landing trajectory and landing status.
 * It shows:
 *  - A fixed desired landing trajectory drawn as a curved (quadratic Bezier) line in blue.
 *    This curve is fixed and does not change over time.
 *  - The actual path traveled by the spacecraft drawn in orange, updated in real-time.
 *  - The Moon (landing target) as a small white circle positioned near the bottom center.
 *  - The current spacecraft position as a red dot.
 *  - When the spacecraft's altitude is near zero, it checks the landing speed.
 *    If the speed exceeds 277.78 m/s (1000 km/h), it displays "Collision!",
 *    otherwise, "Successful Landing!" is shown.
 *
 * The transformation maps world coordinates (from the Spacecraft model) to screen coordinates.
 * A vertical shift is applied so that when sp.y = 0, the spacecraft appears at the defined starting point.
 */

package panels;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import constants.SpacecraftConstants;

public class LandingPanel extends JPanel {
    private SpacecraftPanel sp;

    // Margins and world coordinate limits for the landing panel
    private final int margin = 20;
    private final double H_MAX = 500;
    private final double V_MAX = 7000;

    // Desired start point on screen (Y coordinate)
    private final int P0y = margin + 50;

    public LandingPanel(SpacecraftPanel spPanel) {
        this.sp = spPanel;
        setBackground(Color.BLACK);
        Timer timer = new Timer(100, e -> repaint());
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        int availWidth = width - 2 * margin;
        int availHeight = height - 2 * margin;

        double xScale = availWidth / (2.0 * H_MAX);
        double yScale = availHeight / V_MAX;

        double effectiveAltitude0 = (height - margin - P0y) / yScale;
        double shift = SpacecraftConstants.INITIAL_ALTITUDE - effectiveAltitude0;

        int targetX = width / 2;
        int targetY = height - margin - 40;

        int moonRadius = 15;
        int moonX = targetX - moonRadius;
        int moonY = targetY - moonRadius;
        g2d.setColor(Color.WHITE);
        g2d.fillOval(moonX, moonY, 2 * moonRadius, 2 * moonRadius);
        g2d.setColor(Color.GRAY);
        g2d.drawOval(moonX, moonY, 2 * moonRadius, 2 * moonRadius);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Landing Target", targetX - 40, targetY - 10);

        int P0x = width / 2;
        int P0y_screen = P0y;
        int P2x = targetX;
        int P2y = targetY;
        int P1x = width / 2 + 150;
        int P1y = (P0y_screen + P2y) / 2 - 100;

        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2));
        Point prevPoint = null;
        for (double t = 0; t <= 1; t += 0.01) {
            int bx = (int) ((1 - t) * (1 - t) * P0x + 2 * (1 - t) * t * P1x + t * t * P2x);
            int by = (int) ((1 - t) * (1 - t) * P0y_screen + 2 * (1 - t) * t * P1y + t * t * P2y);
            Point current = new Point(bx, by);
            if (prevPoint != null) {
                g2d.drawLine(prevPoint.x, prevPoint.y, current.x, current.y);
            }
            prevPoint = current;
        }

        List<Point> path = sp.getPath();
        if (path.size() > 1) {
            g2d.setColor(Color.ORANGE);
            for (int i = 1; i < path.size(); i++) {
                Point p1 = path.get(i - 1);
                Point p2 = path.get(i);
                double alt1 = (SpacecraftConstants.INITIAL_ALTITUDE - p1.y);
                double alt2 = (SpacecraftConstants.INITIAL_ALTITUDE - p2.y);
                int x1 = (int) (width / 2 + p1.x * xScale);
                int y1 = height - margin - (int) ((alt1 - shift) * yScale);
                int x2 = (int) (width / 2 + p2.x * xScale);
                int y2 = height - margin - (int) ((alt2 - shift) * yScale);
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        double currentAlt = (SpacecraftConstants.INITIAL_ALTITUDE - sp.getSpacecraft().y) - shift;
        int currentX = (int) (width / 2 + sp.getSpacecraft().x * xScale);
        int currentY = height - margin - (int) (currentAlt * yScale);
        g2d.setColor(Color.RED);
        g2d.fillOval(currentX - 5, currentY - 5, 10, 10);
        g2d.drawString("Current", currentX + 10, currentY);

        if (currentAlt <= 10) {
            double speed = Math.sqrt(sp.getSpacecraft().vx * sp.getSpacecraft().vx + sp.getSpacecraft().vy * sp.getSpacecraft().vy);
            if (speed > 277.78) {
                g2d.setColor(Color.RED);
                g2d.drawString("Collision! Landing speed: " + String.format("%.2f m/s", speed), margin, margin + 20);
            } else {
                g2d.setColor(Color.GREEN);
                g2d.drawString("Successful Landing! Speed: " + String.format("%.2f m/s", speed), margin, margin + 20);
            }
        }
    }
}
