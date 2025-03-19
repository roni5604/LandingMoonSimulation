/*
 * PIDController.java
 *
 * Description:
 * A generic PID controller that computes a control output based on the error
 * between a setpoint and a measured value. It includes proportional, integral,
 * and derivative terms.
 */

package controllers;

public class PIDController {
    private double kp, ki, kd;
    private double previousError;
    private double integral;

    public PIDController(double kp, double ki, double kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.previousError = 0;
        this.integral = 0;
    }

    /**
     * Computes the control output.
     * @param setpoint The desired value.
     * @param measured The current measured value.
     * @param dt The time interval.
     * @return The control output.
     */
    public double update(double setpoint, double measured, double dt) {
        double error = setpoint - measured;
        integral += error * dt;
        double derivative = (error - previousError) / dt;
        previousError = error;
        return kp * error + ki * integral + kd * derivative;
    }

    /**
     * Resets the integral and previous error terms.
     */
    public void reset() {
        previousError = 0;
        integral = 0;
    }
}
