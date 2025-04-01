package panels;

import javax.swing.*;
import java.awt.*;
import javax.swing.Timer;
import constants.SpacecraftConstants;
import controllers.LandingPIDController;
import controllers.PIDController;
import models.Spacecraft;
import java.util.HashMap;

/**
 * TelemetryPanel.java
 *
 * Displays real-time telemetry data and provides PID landing controls.
 * When PID is active, the cascaded PID controller (with attitude correction)
 * overrides engine thrust commands. These commands are pushed to the
 * EngineSlidersPanel so that both engine visuals and slider values reflect the active commands.
 */
public class TelemetryPanel extends JPanel {
    private final SpacecraftPanel sp;
    private final EngineSlidersPanel sliders;

    private JLabel titleLabel;
    private JLabel velocityTitleLabel;
    private JLabel horizontalLabel, verticalLabel;
    private JLabel horizontalValue, verticalValue;
    private JLabel altitudeLabel, angleLabel, fuelLabel;
    private JButton restartButton;
    private JButton pidToggleButton;
    private JButton pidResetButton;

    private boolean pidActive = false;
    // Create the cascaded PID controller with 15 parameters.
    // Example gains (tune as needed):
    // Vertical position: 0.01, 0.0001, 0.001
    // Vertical speed: 0.5, 0.01, 0.1
    // Horizontal position: 0.01, 0.0001, 0.001
    // Horizontal speed: 0.5, 0.01, 0.1
    // Attitude: 0.5, 0.001, 0.05
    private LandingPIDController pidController;

    public TelemetryPanel(SpacecraftPanel spacecraftPanel, EngineSlidersPanel slidersPanel) {
        this.sp = spacecraftPanel;
        this.sliders = slidersPanel;
        setBackground(Color.DARK_GRAY);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        buildUI();
        pidController = new LandingPIDController(
                0.01, 0.0001, 0.001,   // vertical position gains
                0.5, 0.01, 0.1,        // vertical speed gains
                0.01, 0.0001, 0.001,    // horizontal position gains
                0.5, 0.01, 0.1,         // horizontal speed gains
                0.5, 0.001, 0.05        // attitude gains
        );
        Timer t = new Timer(100, e -> updateTelemetry());
        t.start();
    }

    private void buildUI() {
        titleLabel = new JLabel("Real-Time Telemetry");
        styleLabel(titleLabel, 22, Color.CYAN);

        velocityTitleLabel = new JLabel("Velocity [m/s]");
        styleLabel(velocityTitleLabel, 18, Color.WHITE);

        JPanel velocityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        velocityPanel.setBackground(Color.DARK_GRAY);

        horizontalLabel = new JLabel("Horizontal:");
        styleLabel(horizontalLabel, 16, Color.LIGHT_GRAY);
        horizontalValue = new JLabel("0.00");
        styleValueLabel(horizontalValue, 16, new Color(235, 235, 200));

        verticalLabel = new JLabel("Vertical:");
        styleLabel(verticalLabel, 16, Color.LIGHT_GRAY);
        verticalValue = new JLabel("0.00");
        styleValueLabel(verticalValue, 16, new Color(144, 238, 144));

        velocityPanel.add(horizontalLabel);
        velocityPanel.add(horizontalValue);
        velocityPanel.add(verticalLabel);
        velocityPanel.add(verticalValue);

        altitudeLabel = new JLabel("Altitude: " + SpacecraftConstants.INITIAL_ALTITUDE + " m");
        styleLabel(altitudeLabel, 16, Color.WHITE);

        angleLabel = new JLabel("Angle: 0.00°");
        styleLabel(angleLabel, 16, Color.WHITE);

        fuelLabel = new JLabel("Fuel: " + SpacecraftConstants.INITIAL_FUEL + " L");
        styleLabel(fuelLabel, 16, Color.WHITE);

        restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.BOLD, 16));
        restartButton.setForeground(Color.BLACK);
        restartButton.setBackground(Color.ORANGE);
        restartButton.addActionListener(e -> onRestartClicked());

        pidToggleButton = new JButton("Activate PID");
        pidToggleButton.setFont(new Font("Arial", Font.BOLD, 16));
        pidToggleButton.setForeground(Color.BLACK);
        pidToggleButton.setBackground(Color.YELLOW);
        pidToggleButton.addActionListener(e -> onPIDToggle());

        pidResetButton = new JButton("Reset PID");
        pidResetButton.setFont(new Font("Arial", Font.BOLD, 16));
        pidResetButton.setForeground(Color.BLACK);
        pidResetButton.setBackground(Color.LIGHT_GRAY);
        pidResetButton.addActionListener(e -> onPIDReset());

        add(Box.createVerticalStrut(10));
        add(centeredPanel(titleLabel));
        add(Box.createVerticalStrut(10));
        add(centeredPanel(velocityTitleLabel));
        add(velocityPanel);
        add(Box.createVerticalStrut(10));
        add(centeredPanel(altitudeLabel));
        add(centeredPanel(angleLabel));
        add(centeredPanel(fuelLabel));
        add(Box.createVerticalStrut(10));
        add(centeredPanel(pidToggleButton));
        add(Box.createVerticalStrut(5));
        add(centeredPanel(pidResetButton));
        add(Box.createVerticalGlue());
        add(centeredPanel(restartButton));
        add(Box.createVerticalStrut(10));
    }

    private void updateTelemetry() {
        Spacecraft sc = sp.getSpacecraft();
        double hSpeed = sc.getHorizontalSpeed();
        double vSpeed = sc.getVerticalSpeed();
        double alt    = sc.getAltitude();
        double ang    = sc.getAngle();
        double fuel   = sc.getFuel();
        double hPos   = sc.x; // horizontal position

        horizontalValue.setText(String.format("%.2f", hSpeed));
        verticalValue.setText(String.format("%.2f", vSpeed));
        altitudeLabel.setText(String.format("Altitude: %.2f m", alt));
        angleLabel.setText(String.format("Angle: %.2f°", ang));
        fuelLabel.setText(String.format("Fuel: %.2f L", fuel));

        if (pidActive) {
            // Update PID using: currentAltitude, currentVerticalSpeed, currentHorizontalPos, currentHorizontalSpeed, currentAngle, dt.
            HashMap<String, Double> pidCommands = pidController.update(
                    alt, vSpeed, hPos, hSpeed, ang, SpacecraftConstants.DT
            );
            double verticalCmd = pidCommands.get("MHT");
            double horizontalCmd = pidCommands.get("HORIZONTAL");
            // Override main engine command.
            sliders.engineThrust.put("MHT", verticalCmd);

            // Selectively activate secondary engines:
            // If horizontalCmd is positive, fire left engines; if negative, fire right engines.
            if (horizontalCmd > 0) {
                for (String eng : sliders.secondaryEngines) {
                    if (eng.contains("L")) {
                        sliders.engineThrust.put(eng, horizontalCmd);
                    } else {
                        sliders.engineThrust.put(eng, 0.0);
                    }
                }
            } else if (horizontalCmd < 0) {
                for (String eng : sliders.secondaryEngines) {
                    if (eng.contains("R")) {
                        sliders.engineThrust.put(eng, -horizontalCmd);
                    } else {
                        sliders.engineThrust.put(eng, 0.0);
                    }
                }
            } else {
                for (String eng : sliders.secondaryEngines) {
                    sliders.engineThrust.put(eng, 0.0);
                }
            }
            pidToggleButton.setText("PID Active");
        } else {
            pidToggleButton.setText("Activate PID");
        }
    }

    private void onRestartClicked() {
        sp.resetSpacecraft();
        sliders.resetAllSliders();
        pidController.reset();
    }

    private void onPIDToggle() {
        pidActive = !pidActive;
    }

    private void onPIDReset() {
        pidController.reset();
    }

    private void styleLabel(JLabel lbl, int fontSize, Color color) {
        lbl.setFont(new Font("Arial", Font.BOLD, fontSize));
        lbl.setForeground(color);
    }

    private void styleValueLabel(JLabel lbl, int fontSize, Color bgColor) {
        lbl.setOpaque(true);
        lbl.setBackground(bgColor);
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        lbl.setFont(new Font("Consolas", Font.BOLD, fontSize));
        lbl.setForeground(Color.BLACK);
    }

    private JPanel centeredPanel(JComponent comp) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.setBackground(Color.DARK_GRAY);
        panel.add(comp);
        return panel;
    }
}
