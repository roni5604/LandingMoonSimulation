/*
 * Main.java
 *
 * Description:
 * This is the main entry point for the Spacecraft Control system.
 * It creates the main JFrame and organizes the panels as follows:
 * 1. LandingPanel        - Far left: Displays the landing trajectory, the traveled path, and the Moon.
 * 2. EngineVisualPanel   - Left: Graphical display of engine status.
 * 3. SpacecraftPanel     - Center: Displays the spacecraft in flight.
 * 4. TelemetryPanel      - Right-center: Shows real-time telemetry data.
 * 5. EngineSlidersPanel  - Far right: Allows adjusting engine thrust via sliders.
 */

import javax.swing.*;
import java.awt.*;
import panels.EngineSlidersPanel;
import panels.SpacecraftPanel;
import panels.EngineVisualPanel;
import panels.TelemetryPanel;
import panels.LandingPanel;

public class Main extends JFrame {
    public Main() {
        setTitle("Spacecraft Control");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(2000, 700);
        setLayout(new GridLayout(1, 5));
        setLocationRelativeTo(null);

        EngineSlidersPanel slidersPanel = new EngineSlidersPanel();
        SpacecraftPanel spacecraftPanel = new SpacecraftPanel(slidersPanel);
        EngineVisualPanel visualPanel = new EngineVisualPanel(slidersPanel);
        TelemetryPanel telemetryPanel = new TelemetryPanel(spacecraftPanel, slidersPanel);
        LandingPanel landingPanel = new LandingPanel(spacecraftPanel);

        // Order: LandingPanel, EngineVisualPanel, SpacecraftPanel, TelemetryPanel, EngineSlidersPanel
        add(landingPanel);
        add(visualPanel);
        add(spacecraftPanel);
        add(telemetryPanel);
        add(slidersPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
