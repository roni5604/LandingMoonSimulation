package constants;

/*
 * SpacecraftConstants.java
 *
 * Contains global constants for the project.
 * Note: The initial altitude is set to 30000 m and the spacecraft’s dry mass (WEIGHT_EMP)
 * plus fuel (1 kg per liter) gives the dynamic mass.
 */
public class SpacecraftConstants {
    public static final double WEIGHT_EMP = 165;    // Dry mass in kg
    public static final double WEIGHT_FULE = 420;    // Initial fuel in liters (assume 1 kg per liter)
    public static final double WEIGHT_FULL = WEIGHT_EMP + WEIGHT_FULE; // Full mass (not used directly, mass is dynamic)

    public static final double MAIN_ENG_F = 430;      // Main engine maximum thrust in N
    public static final double SECOND_ENG_F = 25;     // Secondary engine maximum thrust in N

    public static final double MAIN_BURN = 0.15;      // Fuel burn rate (liters/sec) at maximum main engine thrust
    public static final double SECOND_BURN = 0.009;   // Fuel burn rate (liters/sec) at maximum secondary engine thrust
    public static final double ALL_BURN = MAIN_BURN + 8 * SECOND_BURN;

    // Initial conditions for the spacecraft.
    public static final double INITIAL_FUEL = 420;       // liters
    public static final double INITIAL_ALTITUDE = 30000;  // meters above the Moon
    public static final double DT = 0.05;                 // Simulation time step (seconds)
}
