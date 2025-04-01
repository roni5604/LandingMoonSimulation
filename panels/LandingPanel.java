package panels;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import constants.SpacecraftConstants;

/**
 * LandingPanel.java
 *
 * This panel displays the landing trajectory, landing target, and the spacecraft's progress.
 * A fixed coordinate system is used so that the Moon (landing target) remains static,
 * and the spacecraft is drawn according to its simulation coordinates.
 * The mapping is fixed so that the spacecraft starts from a constant point with its initial data.
 */
public class LandingPanel extends JPanel {
    private SpacecraftPanel sp;

    // Define simulation ranges for scaling.
    // Horizontal range (in meters) for the view.
    private final double H_MAX = 10000;
    // Vertical range (in meters) for the view – set to cover the altitude.
    private final double V_MAX = 30000;

    // Margin in pixels.
    private final int margin = 20;

    // Landing target position in simulation coordinates.
    // For a static Moon, assume the landing target is at x = 0, and at y = INITIAL_ALTITUDE (altitude zero).
    private final double targetX_sim = 0;
    private final double targetY_sim = SpacecraftConstants.INITIAL_ALTITUDE;

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
        // Enable anti-aliasing for smoother drawing.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Calculate scaling factors: map simulation meters to screen pixels.
        double xScale = (width - 2.0 * margin) / (2.0 * H_MAX);
        double yScale = (height - 2.0 * margin) / V_MAX;

        // Convert simulation coordinates to screen coordinates.
        // Screen X: center at width/2 + (simX * xScale)
        // Screen Y: bottom margin corresponds to altitude 0, so:
        //   screenY = height - margin - (altitude * yScale)
        double simX = sp.getSpacecraft().x;
        double simAltitude = sp.getSpacecraft().getAltitude();
        int spacecraftScreenX = (int)(width / 2 + simX * xScale);
        int spacecraftScreenY = (int)(height - margin - simAltitude * yScale);

        // Draw the landing target (Moon) as a fixed element at the bottom center.
        int targetScreenX = width / 2;
        int targetScreenY = height - margin - 40;  // fixed position near the bottom
        int moonRadius = 15;
        g2d.setColor(Color.WHITE);
        g2d.fillOval(targetScreenX - moonRadius, targetScreenY - moonRadius, 2 * moonRadius, 2 * moonRadius);
        g2d.setColor(Color.GRAY);
        g2d.drawOval(targetScreenX - moonRadius, targetScreenY - moonRadius, 2 * moonRadius, 2 * moonRadius);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Landing Target", targetScreenX - 40, targetScreenY - 10);

        // Draw desired landing trajectory as a blue quadratic Bezier curve.
        // For simplicity, use control points between the spacecraft's starting position and the landing target.
        int P0x = width / 2;  // assume spacecraft starts at center horizontally
        int P0y = height - margin - (int)(SpacecraftConstants.INITIAL_ALTITUDE * yScale);
        int P2x = targetScreenX;
        int P2y = targetScreenY;
        // Control point for the curve – adjust for a smoother trajectory.
        int P1x = (P0x + P2x) / 2 + 100;
        int P1y = (P0y + P2y) / 2 - 100;

        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2));
        Point prevPoint = null;
        for (double t = 0; t <= 1; t += 0.01) {
            int bx = (int) ((1 - t) * (1 - t) * P0x + 2 * (1 - t) * t * P1x + t * t * P2x);
            int by = (int) ((1 - t) * (1 - t) * P0y + 2 * (1 - t) * t * P1y + t * t * P2y);
            Point current = new Point(bx, by);
            if (prevPoint != null) {
                g2d.drawLine(prevPoint.x, prevPoint.y, current.x, current.y);
            }
            prevPoint = current;
        }

        // Draw the trajectory path (actual progress) in orange.
        java.util.List<Point> path = sp.getPath();
        if (path.size() > 1) {
            g2d.setColor(Color.ORANGE);
            for (int i = 1; i < path.size(); i++) {
                double simX1 = path.get(i - 1).x;
                double simAltitude1 = SpacecraftConstants.INITIAL_ALTITUDE - path.get(i - 1).y;
                double simX2 = path.get(i).x;
                double simAltitude2 = SpacecraftConstants.INITIAL_ALTITUDE - path.get(i).y;
                int x1 = (int)(width/2 + simX1 * xScale);
                int y1 = (int)(height - margin - simAltitude1 * yScale);
                int x2 = (int)(width/2 + simX2 * xScale);
                int y2 = (int)(height - margin - simAltitude2 * yScale);
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        // Draw the current spacecraft position as a red dot.
        g2d.setColor(Color.RED);
        g2d.fillOval(spacecraftScreenX - 5, spacecraftScreenY - 5, 10, 10);
        g2d.drawString("Current", spacecraftScreenX + 10, spacecraftScreenY);
    }
}
