package utils;

/** Contains various utility functions and constants. */
public abstract class Utils {
    public static final String EARTH_NAME= "Earth";
    public static final String CRASHED_PLANET_NAME= "Planet X";
    public static final String DIRECTORY= System.getProperty("user.dir");

    /** Returns true iff any of the given arguments are null. */
    public static boolean anyNull(Object... objects) {
        for (Object obj : objects) {
            if (obj == null)
                return true;
        }
        return false;
    }
    
    /** Returns the distance between (x1, x2) and (y1, y2). */
    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
    }
}
