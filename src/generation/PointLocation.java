package generation;

import java.awt.Point;

public class PointLocation {
    public enum Location {
        IN, EDGE, VERTEX, OUT;
    }

    /* The type of location of this Point */
    private Location l;

    /* The Triangle this Point lies on (for IN/EDGE/VERTEX). null if OUT. */
    private Triangle t;

    /* If IN/OUT, null. If EDGE, this is the vertex OPPOSITE the edge. If
     * VERTEX, this is the vertex. */
    private Point p;

    /** Constructor: a Location, Triangle, and Point. */
    private PointLocation(Location l, Triangle t, Point p) {
        this.l= l;
        this.t= t;
        this.p= p;
    }

    /** Returns a PointLocation for a Point in Triangle t. */
    public static PointLocation makeIn(Triangle t) {
        return new PointLocation(Location.IN, t, null);
    }

    /** Returns a PointLocation for a Point on an edge of Triangle t. Point
     * opposite is the vertex OPPOSITE (i.e. not on) the edge of interest. */
    public static PointLocation makeEdge(Triangle t, Point opposite) {
        return new PointLocation(Location.EDGE, t, opposite);
    }

    /** Returns a PointLocation for a Point on vertex v of Triangle t. */
    public static PointLocation makeVertex(Triangle t, Point v) {
        return new PointLocation(Location.VERTEX, t, v);
    }

    /** Returns a PointLocation for a Point outside of some area of interest. */
    public static PointLocation makeOut() {
        return new PointLocation(Location.OUT, null, null);
    }

    /** If IN/EDGE/VERTEX, returns the Triangle on which this Point lies. If
     * OUT, returns null. */
    public Triangle getTriangle() {
        return t;
    }
    
    /** Returns the Point associated with this Triangle. If EDGE, this is the
     * vertex OPPOSITE of the edge of interest. If VERTEX, this is the vertex.
     * If IN/OUT, this is null. */
    public Point getPoint() {
        return p;
    }

    /** Returns true iff this is not an OUT PointLocation. */
    public boolean isNotOut() {
        return l != Location.OUT;
    }

    /** Returns the Location of this PointLocation. */
    public Location getLocation() {
        return l;
    }
}
