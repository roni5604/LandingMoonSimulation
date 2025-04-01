package models;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import constants.SpacecraftConstants;
// Gravity is not added in this version, so the spacecraft’s motion depends solely on initial velocity and engine thrust.
import constants.Moon;

/**
 * Spacecraft.java
 *
 * Represents the spacecraft with translational and rotational dynamics.
 * The state is updated using Newton's laws (F = m*a and τ = I*α) based solely on
 * engine thrust and the initial velocity. No external gravitational force is added.
 * Dynamic mass is computed as the dry mass plus remaining fuel.
 *
 * An angular damping factor has been added so that when side engines are turned off,
 * any residual rotation gradually decays.
 */
public class Spacecraft {
    public double x, y;             // Position in world coordinates (meters)
    public double vx, vy;           // Linear velocities (m/s)
    public double angle;            // Orientation (degrees, in the body frame)
    public double angularVelocity;  // Angular velocity (deg/s)
    public double fuel;             // Remaining fuel (liters; assume 1 liter ≈ 1 kg)
    public double altitude;         // Altitude above the Moon (meters)
    public double dt;               // Simulation time step (seconds)
    private List<Point> path;       // Trajectory for visualization

    /**
     * Constructor sets initial conditions.
     * The spacecraft starts at x = 0, y = 0 (simulation coordinate) with an altitude equal to INITIAL_ALTITUDE.
     * Initial horizontal speed is 1700 m/s, vertical speed is 0.
     * The spacecraft is initially vertical (angle = 90°) so that the main engine thrust acts upward.
     */
    public Spacecraft() {
        x = 0;
        y = 0;
        angle = 0;           // Initial angle (degrees)
        angularVelocity = 0;
        vx = 0;            // Initial horizontal speed (m/s)
        vy = 0;
        fuel = SpacecraftConstants.INITIAL_FUEL;
        altitude = SpacecraftConstants.INITIAL_ALTITUDE;
        dt = SpacecraftConstants.DT;
        path = new ArrayList<>();
    }

    /**
     * Updates the spacecraft state based on engine thrust commands.
     * <p>
     * Main engine (MHT):
     * - Maximum thrust: 430 N.
     * - Defined in body coordinates as (-T, 0) so that for angle = 90° the world force is (0, -T) (upward).
     * - Applied at the center (r = (0,0)), so no torque is generated.
     * </p>
     * <p>
     * Secondary engines (balancing engines):
     * - Eight engines: "FR1", "FR2", "FL1", "FL2", "BL1", "BL2", "BR1", "BR2" with maximum thrust 25 N each.
     * - Mounted at y = -20 (front engines) or y = +20 (back engines) in body coordinates.
     * - Right engines are at x = +15 (force in +x) and left engines at x = -15 (force in -x).
     * - Their net torque is computed as τ = r_x * f_local_y - r_y * f_local_x (with f_local_y = 0, this becomes -r_y * f_local_x).
     * </p>
     * <p>
     * No gravitational force is added so that the only influences on motion are the initial velocity and engine thrust.
     * Linear acceleration is computed as a = F/m and integrated over time, and angular acceleration as α = τ/I.
     * An angular damping factor is applied so that if no torque is applied the angular velocity decays.
     * </p>
     *
     * @param engineThrust Map of engine labels to thrust values.
     */
    public void update(Map<String, Double> engineThrust) {
        double netForceX = 0, netForceY = 0;
        double netTorque = 0;
        double totalFuelBurn = 0;
        double dt = this.dt;

        // Compute dynamic mass (dry mass plus remaining fuel; assume 1 liter ≈ 1 kg).
        double mass = SpacecraftConstants.WEIGHT_EMP + fuel;
        // Chosen moment of inertia (tuned for simulation).
        double momentOfInertia = 500;

        // --- Process Main Engine ---
        double mainT = engineThrust.getOrDefault("MHT", 0.0);
        if (Math.abs(mainT) > 1e-5 && fuel > 0) {
            // Calculate fuel burn for main engine.
            double burnRate = SpacecraftConstants.MAIN_BURN * (Math.abs(mainT) / SpacecraftConstants.MAIN_ENG_F);
            totalFuelBurn += burnRate * dt;
            // In body coordinates, main engine force is (-mainT, 0).
            // For an angle of 90°, world force becomes (0, -mainT) (upward).
            double f_main_local_x = -mainT;
            double f_main_local_y = 0;
            double theta = Math.toRadians(angle);
            double f_main_world_x = f_main_local_x * Math.cos(theta) - f_main_local_y * Math.sin(theta);
            double f_main_world_y = f_main_local_x * Math.sin(theta) + f_main_local_y * Math.cos(theta);
            netForceX += f_main_world_x;
            netForceY += f_main_world_y;
            // No torque is produced since force is applied at the center.
        }

        // --- Process Secondary (Balancing) Engines ---
        for (String key : engineThrust.keySet()) {
            if (key.equals("MHT")) continue; // Skip main engine.
            double t = engineThrust.get(key);
            if (Math.abs(t) > 1e-5 && fuel > 0) {
                double burnRate = SpacecraftConstants.SECOND_BURN * (Math.abs(t) / SpacecraftConstants.SECOND_ENG_F);
                totalFuelBurn += burnRate * dt;

                double r_x = 0, r_y = 0;
                double f_local_x = 0, f_local_y = 0;
                // Set vertical mounting based on engine label: front engines ("F") at y = -20, back engines ("B") at y = +20.
                if (key.startsWith("F")) {
                    r_y = -20;
                } else { // Assume "B"
                    r_y = 20;
                }
                // Set horizontal mounting and force direction: right engines ("R") at x = +15 produce force in +x; left engines ("L") at x = -15 produce force in -x.
                if (key.contains("R")) {
                    r_x = 15;
                    f_local_x = t;
                } else if (key.contains("L")) {
                    r_x = -15;
                    f_local_x = -t;
                }
                // Secondary engines produce only horizontal force.
                f_local_y = 0;
                double theta = Math.toRadians(angle);
                double f_world_x = f_local_x * Math.cos(theta) - f_local_y * Math.sin(theta);
                double f_world_y = f_local_x * Math.sin(theta) + f_local_y * Math.cos(theta);
                netForceX += f_world_x;
                netForceY += f_world_y;
                // Compute torque: τ = r_x * f_local_y - r_y * f_local_x.
                // Since f_local_y is 0, torque = -r_y * f_local_x.
                double torque_secondary = -r_y * f_local_x;
                netTorque += torque_secondary;
            }
        }

        // --- (Gravity is Removed) ---
        // No gravitational force is added in this simulation.

        // --- Update Linear Motion ---
        // Compute acceleration: a = F/m.
        double ax = netForceX / mass;
        double ay = netForceY / mass;
        vx += ax * dt;
        vy += ay * dt;
        x += vx * dt;
        y += vy * dt;
        // Altitude is defined as INITIAL_ALTITUDE minus the simulation y coordinate.
        altitude = SpacecraftConstants.INITIAL_ALTITUDE - y;

        // --- Update Rotational Motion ---
        double angularAcc = netTorque / momentOfInertia;
        angularVelocity += angularAcc * dt;
        // Apply angular damping to gradually cancel residual rotation when no torque is applied.
        angularVelocity *= 0.98;  // Damping factor: adjust as needed.
        // Optionally, set angular velocity to zero if it falls below a small threshold.
        if (Math.abs(angularVelocity) < 0.001) {
            angularVelocity = 0;
        }
        angle += angularVelocity * dt;

        // --- Update Fuel ---
        if (totalFuelBurn > 0 && fuel > 0) {
            fuel -= totalFuelBurn;
            if (fuel < 0) {
                fuel = 0;
            }
        }

        // Record the current position for trajectory visualization.
        path.add(new Point((int)x, (int)y));
    }

    // Getter methods.
    public List<Point> getPath() { return path; }
    public double getHorizontalSpeed() { return vx; }
    public double getVerticalSpeed() { return vy; }
    public double getAltitude() { return altitude; }
    public double getAngle() { return angle; }
    public double getFuel() { return fuel; }

    /**
     * Resets the spacecraft to its initial state.
     */
    public void reset() {
        x = 0;
        y = 0;
        angle = 0;
        angularVelocity = 0;
        vx = 0;
        vy = 0;
        fuel = SpacecraftConstants.INITIAL_FUEL;
        altitude = SpacecraftConstants.INITIAL_ALTITUDE;
        path.clear();
    }
}
