package constants;/*
 * constants.SpacecraftConstants.java
 *
 * תיאור:
 * קובץ זה מכיל את כל הקבועים הגלובליים המשמשים את הפרויקט,
 * כגון משקלים, כוחות מנועים, קצבי שריפה, ערכי התחלה (דלק, altitude)
 * וקצב העדכון (dt). שינוי ערכים כאן משפיע על כל המערכת.
 */

public class SpacecraftConstants {
    public static final double WEIGHT_EMP = 165;    // kg
    public static final double WEIGHT_FULE = 420;   // kg
    public static final double WEIGHT_FULL = WEIGHT_EMP + WEIGHT_FULE; // kg

    public static final double MAIN_ENG_F = 430;    // N
    public static final double SECOND_ENG_F = 25;   // N

    public static final double MAIN_BURN = 0.15;    // liter/sec (במקסימום)
    public static final double SECOND_BURN = 0.009; // liter/sec (במקסימום)
    public static final double ALL_BURN = MAIN_BURN + 8 * SECOND_BURN;

    // ערכי התחלה – ניתנים לשינוי בקלות
    public static final double INITIAL_FUEL = 420;       // liters
    public static final double INITIAL_ALTITUDE = 13748;  // meters
    public static final double DT = 0.05;                 // 50ms (קצב עדכון)
}
