package panels;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/*
 * EngineSlidersPanel.java
 *
 * Displays sliders to control the thrust values for each engine.
 * - Main engine (MHT): Range [-430, +430]
 * - Secondary engines: Range [-25, +25]
 * Each slider updates only its associated engine’s thrust value.
 */
public class EngineSlidersPanel extends JPanel {
    public String mainEngine = "MHT";
    public String[] secondaryEngines = {
            "FR1", "FR2",
            "FL1", "FL2",
            "BL1", "BL2",
            "BR1", "BR2"
    };
    // Current thrust values for each engine.
    public HashMap<String, Double> engineThrust = new HashMap<>();
    // Sliders for controlling thrust.
    public HashMap<String, JSlider> sliders = new HashMap<>();

    public EngineSlidersPanel() {
        setLayout(new GridLayout(9, 1, 5, 5));

        // Main engine slider.
        engineThrust.put(mainEngine, 0.0);
        JSlider mainSlider = createSliderForKey(mainEngine, -430, 430, 0);
        sliders.put(mainEngine, mainSlider);
        add(labeledPanel(mainEngine + " thrust", mainSlider));

        // Secondary engine sliders.
        for (String eng : secondaryEngines) {
            engineThrust.put(eng, 0.0);
            JSlider s = createSliderForKey(eng, -25, 25, 0);
            sliders.put(eng, s);
            add(labeledPanel("Engine " + eng, s));
        }
    }

    /**
     * Creates a slider for a specific engine key.
     *
     * @param key  The engine identifier.
     * @param min  The minimum slider value.
     * @param max  The maximum slider value.
     * @param init The initial slider value.
     * @return A JSlider configured for the given engine.
     */
    private JSlider createSliderForKey(String key, int min, int max, int init) {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, init);
        slider.setMajorTickSpacing((max - min) / 2);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            engineThrust.put(key, (double) slider.getValue());
        });
        return slider;
    }

    private JPanel labeledPanel(String labelText, JSlider slider) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel(labelText, JLabel.CENTER);
        lbl.setForeground(Color.BLACK);
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);
        return panel;
    }

    /** Resets all sliders to 0. */
    public void resetAllSliders() {
        for (String eng : engineThrust.keySet()) {
            engineThrust.put(eng, 0.0);
            sliders.get(eng).setValue(0);
        }
    }
}
