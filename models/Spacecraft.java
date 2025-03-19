/*
 * Spacecraft.java
 *
 * Description:
 * This class represents the spacecraft model and contains all its details:
 * position (x, y), orientation (angle), velocity (vx, vy), fuel, altitude,
 * and a record of the traveled path. It provides an update() method that
 * computes the new state based on engine thrust commands.
 *
 * Getters are provided for horizontal speed, vertical speed, altitude, angle, and fuel.
 */

package models;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import constants.SpacecraftConstants;

public class Spacecraft {
    public double x, y, angle;
    public double vx, vy;
    public double fuel;
    public double altitude;
    public double dt;
    private List<Point> path;

    public Spacecraft() {
        x = 0;
        y = 0;
        angle = 0;
        vx = 0;
        vy = 0;
        fuel = SpacecraftConstants.INITIAL_FUEL;
        altitude = SpacecraftConstants.INITIAL_ALTITUDE;
        dt = SpacecraftConstants.DT;
        path = new ArrayList<>();
    }

    /**
     * Updates the spacecraft state based on the given engine thrust commands.
     * @param engineThrust A map of engine labels to thrust values.
     */
    public void update(Map<String, Double> engineThrust) {
        double ax = 0, ay = 0;
        double totalFuelBurn = 0;

        // Main engine (MHT)
        double mainT = engineThrust.getOrDefault("MHT", 0.0);
        if (Math.abs(mainT) > 1e-5 && fuel > 0) {
            double burnRate = SpacecraftConstants.MAIN_BURN * (Math.abs(mainT) / SpacecraftConstants.MAIN_ENG_F);
            totalFuelBurn += burnRate * dt;
            double a = 0.5 * (Math.abs(mainT) / SpacecraftConstants.MAIN_ENG_F);
            double dir = (mainT >= 0) ? angle : angle + 180;
            ax += a * Math.sin(Math.toRadians(dir));
            ay -= a * Math.cos(Math.toRadians(dir));
        }

        // Secondary engines
        for (Map.Entry<String, Double> entry : engineThrust.entrySet()) {
            String eng = entry.getKey();
            if (eng.equals("MHT")) continue;
            double t = entry.getValue();
            if (Math.abs(t) > 1e-5 && fuel > 0) {
                double burnRate = SpacecraftConstants.SECOND_BURN * (Math.abs(t) / SpacecraftConstants.SECOND_ENG_F);
                totalFuelBurn += burnRate * dt;
                double a = 0.2 * (Math.abs(t) / SpacecraftConstants.SECOND_ENG_F);
                double dir = (t >= 0) ? (angle - 90) : (angle + 90);
                ax += a * Math.sin(Math.toRadians(dir));
                ay -= a * Math.cos(Math.toRadians(dir));
            }
        }

        vx += ax;
        vy += ay;
        vx *= 0.98;
        vy *= 0.98;
        x += vx * dt;
        y += vy * dt;
        altitude = SpacecraftConstants.INITIAL_ALTITUDE - y;

        if (totalFuelBurn > 0 && fuel > 0) {
            fuel -= totalFuelBurn;
            if (fuel < 0) fuel = 0;
        }

        path.add(new Point((int)x, (int)y));
    }

    /**
     * Returns the list of points representing the traveled path.
     */
    public List<Point> getPath() {
        return path;
    }

    /**
     * Returns the horizontal speed (vx).
     */
    public double getHorizontalSpeed() {
        return vx;
    }

    /**
     * Returns the vertical speed (vy).
     */
    public double getVerticalSpeed() {
        return vy;
    }

    /**
     * Returns the current altitude.
     */
    public double getAltitude() {
        return altitude;
    }

    /**
     * Returns the current angle (orientation) in degrees.
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Returns the remaining fuel.
     */
    public double getFuel() {
        return fuel;
    }

    /**
     * Resets the spacecraft state and clears the traveled path.
     */
    public void reset() {
        x = 0;
        y = 0;
        angle = 0;
        vx = 0;
        vy = 0;
        fuel = SpacecraftConstants.INITIAL_FUEL;
        altitude = SpacecraftConstants.INITIAL_ALTITUDE;
        path.clear();
    }
}
