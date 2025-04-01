package panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import models.Spacecraft;
import constants.SpacecraftConstants;

/**
 * SpacecraftPanel.java
 *
 * Displays the spacecraft at the center of the screen.
 * The spacecraft remains fixed in position (centered) while its orientation and flame
 * update according to the applied engine forces. The main engine flame is drawn at a fixed
 * offset and points opposite to the applied main engine thrust.
 */
public class SpacecraftPanel extends JPanel {
    BufferedImage bg;
    EngineSlidersPanel sliders;
    private Spacecraft spacecraft;

    public SpacecraftPanel(EngineSlidersPanel slidersPanel) {
        sliders = slidersPanel;
        spacecraft = new Spacecraft();
        try {
            bg = ImageIO.read(new File("assets/space.jpg"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Cannot load space.jpg!");
            System.exit(1);
        }

        // Timer to update simulation and repaint the panel.
        Timer timer = new Timer(50, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                spacecraft.update(sliders.engineThrust);
                repaint();
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw the background image stretched to the panel size.
        g2d.drawImage(bg, 0, 0, getWidth(), getHeight(), this);

        // Always translate origin to the center of the panel.
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        g2d.translate(cx, cy);

        // Rotate the context by the current spacecraft angle.
        g2d.rotate(Math.toRadians(spacecraft.getAngle()));

        // --- Draw Main Engine Flame ---
        // Retrieve the main engine thrust value.
        double mainThrust = sliders.engineThrust.getOrDefault("MHT", 0.0);
        if (Math.abs(mainThrust) > 1e-5) {
            // Calculate flame length based on thrust magnitude.
            double flameLength = Math.min(20, 10 + Math.abs(mainThrust) * 0.05);
            // The flame is drawn at a fixed offset (0, 30) in local coordinates.
            Graphics2D g2 = (Graphics2D) g2d.create();
            g2.translate(0, 30);
            // The main engine force in body coordinates is (-mainThrust, 0) so the flame
            // should point in the opposite direction. In local coordinates, the flame points at 0°.
            Polygon flame = new Polygon(
                    new int[]{-5, 0, 5},
                    new int[]{0, (int) flameLength, 0},
                    3
            );
            g2.setColor(new Color(255, 140, 0));
            g2.fill(flame);
            g2.dispose();
        }

        // --- Draw Spacecraft Body ---
        // Draw the spacecraft centered at (0,0) in local coordinates.
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRoundRect(-15, -30, 30, 50, 10, 10);

        // Draw landing legs.
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawLine(-15, 20, -25, 35);
        g2d.drawLine(15, 20, 25, 35);

        // Draw auxiliary engines for visualization.
        g2d.setColor(Color.RED);
        g2d.fillOval(-12, 20, 8, 8);
        g2d.fillOval(4, 20, 8, 8);

        // Draw an antenna.
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.BLUE);
        g2d.drawLine(0, -30, 0, -40);
        g2d.fillOval(-3, -43, 6, 6);

        // Draw coordinate axes for reference.
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.RED);
        g2d.drawLine(0, 0, 50, 0);
        g2d.drawString("X", 55, 0);
        g2d.setColor(Color.GREEN);
        g2d.drawLine(0, 0, 0, -50);
        g2d.drawString("Y", 0, -55);
        g2d.setColor(Color.BLUE);
        g2d.drawLine(0, 0, -35, 35);
        g2d.drawString("Z", -45, 45);
    }

    // Getter for the spacecraft model.
    public Spacecraft getSpacecraft() {
        return spacecraft;
    }

    // Getter for the path traveled by the spacecraft.
    public java.util.List<Point> getPath() {
        return spacecraft.getPath();
    }

    // Reset the spacecraft state.
    public void resetSpacecraft() {
        spacecraft.reset();
    }
}
