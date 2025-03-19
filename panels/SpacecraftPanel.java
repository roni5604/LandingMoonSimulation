/*
 * SpacecraftPanel.java
 *
 * Description:
 * Displays the spacecraft on a space background and updates its state.
 * It delegates simulation updates to the Spacecraft model (models.Spacecraft) by using engine thrust commands
 * from EngineSlidersPanel. The panel is responsible for drawing the spacecraft.
 */

package panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import models.Spacecraft;
import constants.SpacecraftConstants;

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
        g2d.drawImage(bg, 0, 0, getWidth(), getHeight(), this);

        int cx = getWidth() / 2 + (int) spacecraft.x;
        int cy = getHeight() / 2 + (int) spacecraft.y;
        g2d.translate(cx, cy);
        g2d.rotate(Math.toRadians(spacecraft.angle));

        Polygon ship = new Polygon(new int[]{0, -30, 30}, new int[]{-40, 30, 30}, 3);
        g2d.setColor(Color.WHITE);
        g2d.fill(ship);

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

    public Spacecraft getSpacecraft() {
        return spacecraft;
    }

    public java.util.List<Point> getPath() {
        return spacecraft.getPath();
    }

    public void resetSpacecraft() {
        spacecraft.reset();
    }
}
