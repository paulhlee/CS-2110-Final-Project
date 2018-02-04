package gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import utils.Utils;

/** An instance is a graphical representation of a Planet. */
public class Planet extends Circle {
    /* The diameter of an ordinary Planet. Earth and the target are larger. */
    private static final double BASE_DIAMETER= 16;
    
    /* The color of Earth. */
    private static final Color EARTH_COLOR= new Color(40, 80, 140);

    /** Constructor: a Circle with the given name centered at ctr within the
     * given area, drawn in the given bounds with initial diameter d and a
     * the given color. */
    protected Planet(String name, Point ctr, Rectangle2D area,
        Rectangle2D bounds, double d, Color c, FontMetrics fm) {
        super(name, ctr, area, bounds, d, c, fm);
    }
    
    /** Returns a Planet with the given name centered at ctr within the given
     * area, drawn within the given bounds. Its name's dimensions are determined
     * via fm. */
    public static Planet make(String name, Point ctr, Rectangle2D area,
        Rectangle2D bounds, FontMetrics fm, Random r) {
        if (name.equals(Utils.EARTH_NAME)) {
            return new Planet(name, ctr, area, bounds, BASE_DIAMETER * 2,
                EARTH_COLOR, fm);
        } else if (name.equals(Utils.CRASHED_PLANET_NAME)) {
            return new Planet(name, ctr, area, bounds, BASE_DIAMETER * 2,
                randomColor(r), fm);
        } else {
            return new Planet(name, ctr, area, bounds, BASE_DIAMETER,
                randomColor(r), fm);
        }
    }
    
    /** Returns a random color using Random r. */
    public static Color randomColor(Random r) {
        return new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256));
    }
}
