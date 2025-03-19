/*
 * LandingPIDController.java
 *
 * Description:
 * This controller uses two PID controllers (one for vertical and one for horizontal control)
 * to compute desired engine thrust commands for landing.
 * The goal is to achieve zero vertical and horizontal speeds.
 * The computed outputs are clamped to the allowed ranges:
 *   - Main engine (vertical): [-430, 430] N
 *   - Secondary engines (horizontal): [-25, 25] N (applied uniformly)
 */

package controllers;

import java.util.HashMap;

public class LandingPIDController {
    private PIDController verticalPID;
    private PIDController horizontalPID;

    // Desired setpoints (landing: 0 m/s)
    private double targetVerticalSpeed = 0;
    private double targetHorizontalSpeed = 0;

    public LandingPIDController(double kpV, double kiV, double kdV,
                                double kpH, double kiH, double kdH) {
        verticalPID = new PIDController(kpV, kiV, kdV);
        horizontalPID = new PIDController(kpH, kiH, kdH);
    }

    /**
     * Computes the engine thrust commands based on current vertical and horizontal speeds.
     * @param currentVerticalSpeed The current vertical speed (m/s).
     * @param currentHorizontalSpeed The current horizontal speed (m/s).
     * @param dt The time interval.
     * @return A map with thrust commands:
     *         "MHT" for main engine thrust and "HORIZONTAL" for secondary engines.
     */
    public HashMap<String, Double> update(double currentVerticalSpeed, double currentHorizontalSpeed, double dt) {
        HashMap<String, Double> commands = new HashMap<>();
        double verticalOutput = verticalPID.update(targetVerticalSpeed, currentVerticalSpeed, dt);
        double horizontalOutput = horizontalPID.update(targetHorizontalSpeed, currentHorizontalSpeed, dt);
        // Clamp outputs:
        verticalOutput = Math.max(-430, Math.min(430, verticalOutput));
        horizontalOutput = Math.max(-25, Math.min(25, horizontalOutput));
        commands.put("MHT", verticalOutput);
        commands.put("HORIZONTAL", horizontalOutput);
        return commands;
    }

    /**
     * Resets the internal state of the PID controllers.
     */
    public void reset() {
        verticalPID.reset();
        horizontalPID.reset();
    }

    public void setTargetVerticalSpeed(double target) {
        this.targetVerticalSpeed = target;
    }

    public void setTargetHorizontalSpeed(double target) {
        this.targetHorizontalSpeed = target;
    }
}
