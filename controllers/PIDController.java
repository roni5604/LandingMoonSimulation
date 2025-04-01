package controllers;

/**
 * PIDController.java
 *
 * A generic PID controller that computes a control output based on the error between
 * a setpoint and a measured value. It includes proportional, integral, and derivative terms.
 */
public class PIDController {
    private double kp, ki, kd;
    private double previousError;
    private double integral;

    /**
     * Constructor for the PID controller.
     *
     * @param kp Proportional gain.
     * @param ki Integral gain.
     * @param kd Derivative gain.
     */
    public PIDController(double kp, double ki, double kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.previousError = 0;
        this.integral = 0;
    }

    /**
     * Computes the PID control output.
     *
     * @param setpoint The desired value.
     * @param measured The current measured value.
     * @param dt       The time interval.
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
     * Resets the PID controller's internal state.
     */
    public void reset() {
        previousError = 0;
        integral = 0;
    }
}
