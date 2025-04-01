package controllers;

import java.util.HashMap;

/**
 * LandingPIDController.java
 *
 * This controller uses cascaded PID loops for vertical and horizontal control plus
 * an additional attitude (angle) controller.
 *
 *  - The outer loops compute desired speeds from the position error.
 *  - The inner loops compute thrust commands from the speed error.
 *  - The attitude controller computes a correction based on the difference between a desired angle
 *    (derived from the horizontal error and altitude) and the current spacecraft angle.
 *
 * The final commands are clamped to:
 *   - Main engine (vertical): [-430, 430] N.
 *   - Secondary engines (horizontal): [-25, 25] N.
 */
public class LandingPIDController {
    // Outer loop PID controllers for position control.
    private PIDController verticalPositionPID;
    private PIDController horizontalPositionPID;
    // Inner loop PID controllers for speed control.
    private PIDController verticalSpeedPID;
    private PIDController horizontalSpeedPID;
    // Additional PID controller for attitude (angle) control.
    private PIDController attitudePID;

    /**
     * Constructor initializes all five PID controllers.
     * The parameters are provided in the following order:
     *   Vertical position: kpVp, kiVp, kdVp
     *   Vertical speed: kpV, kiV, kdV
     *   Horizontal position: kpHp, kiHp, kdHp
     *   Horizontal speed: kpH, kiH, kdH
     *   Attitude (angle): kpA, kiA, kdA
     *
     * @param kpVp Proportional gain for vertical position controller.
     * @param kiVp Integral gain for vertical position controller.
     * @param kdVp Derivative gain for vertical position controller.
     * @param kpV  Proportional gain for vertical speed controller.
     * @param kiV  Integral gain for vertical speed controller.
     * @param kdV  Derivative gain for vertical speed controller.
     * @param kpHp Proportional gain for horizontal position controller.
     * @param kiHp Integral gain for horizontal position controller.
     * @param kdHp Derivative gain for horizontal position controller.
     * @param kpH  Proportional gain for horizontal speed controller.
     * @param kiH  Integral gain for horizontal speed controller.
     * @param kdH  Derivative gain for horizontal speed controller.
     * @param kpA  Proportional gain for attitude controller.
     * @param kiA  Integral gain for attitude controller.
     * @param kdA  Derivative gain for attitude controller.
     */
    public LandingPIDController(
            double kpVp, double kiVp, double kdVp,
            double kpV, double kiV, double kdV,
            double kpHp, double kiHp, double kdHp,
            double kpH, double kiH, double kdH,
            double kpA, double kiA, double kdA) {
        verticalPositionPID = new PIDController(kpVp, kiVp, kdVp);
        verticalSpeedPID = new PIDController(kpV, kiV, kdV);
        horizontalPositionPID = new PIDController(kpHp, kiHp, kdHp);
        horizontalSpeedPID = new PIDController(kpH, kiH, kdH);
        attitudePID = new PIDController(kpA, kiA, kdA);
    }

    /**
     * Computes engine thrust commands based on the current state.
     *
     * @param currentAltitude         Current altitude (m) (target is 0).
     * @param currentVerticalSpeed    Current vertical speed (m/s).
     * @param currentHorizontalPos    Current horizontal position (m) (target is 0).
     * @param currentHorizontalSpeed  Current horizontal speed (m/s).
     * @param currentAngle            Current spacecraft angle (degrees).
     * @param dt                      Time interval (s).
     * @return A map with thrust commands:
     *         "MHT" for main engine (vertical) and "HORIZONTAL" for secondary engines (horizontal/attitude).
     */
    public HashMap<String, Double> update(
            double currentAltitude,
            double currentVerticalSpeed,
            double currentHorizontalPos,
            double currentHorizontalSpeed,
            double currentAngle,
            double dt) {
        HashMap<String, Double> commands = new HashMap<>();

        // Outer loop: compute desired speeds from position errors.
        double desiredVerticalSpeed = -verticalPositionPID.update(0, currentAltitude, dt);
        double desiredHorizontalSpeed = -horizontalPositionPID.update(0, currentHorizontalPos, dt);

        // Inner loop: compute thrust commands to match desired speeds.
        double verticalOutput = verticalSpeedPID.update(desiredVerticalSpeed, currentVerticalSpeed, dt);
        double horizontalOutput = horizontalSpeedPID.update(desiredHorizontalSpeed, currentHorizontalSpeed, dt);

        // Compute desired attitude.
        // For example, desired angle can be computed from horizontal error:
        // If the spacecraft is off to the right (positive horizontal pos), then a slight negative tilt (to the left)
        // can help steer it back. Here we use arctan(error/altitude) as a guideline.
        double desiredAngle = -Math.toDegrees(Math.atan2(currentHorizontalPos, currentAltitude + 1e-3));
        double attitudeError = desiredAngle - currentAngle;
        double attitudeCorrection = attitudePID.update(0, attitudeError, dt);

        // Combine horizontal thrust command with attitude correction.
        // We add the attitude correction to the horizontal output.
        double combinedHorizontal = horizontalOutput + attitudeCorrection;
        // Clamp outputs.
        verticalOutput = Math.max(-430, Math.min(430, verticalOutput));
        combinedHorizontal = Math.max(-25, Math.min(25, combinedHorizontal));

        commands.put("MHT", verticalOutput);
        commands.put("HORIZONTAL", combinedHorizontal);
        return commands;
    }

    /**
     * Resets all internal PID controllers.
     */
    public void reset() {
        verticalPositionPID.reset();
        verticalSpeedPID.reset();
        horizontalPositionPID.reset();
        horizontalSpeedPID.reset();
        attitudePID.reset();
    }
}
