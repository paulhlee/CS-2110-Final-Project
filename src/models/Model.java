package models;

import java.awt.geom.Point2D;
import java.util.Set;

/** An instance allows for access to a Space Gems game's state. */
public interface Model {
    /** Return the seed used to generate this game. */
    public long getSeed();
    
    /** Return a Set of all Nodes in this game. */
    public Set<Node> getNodes();
    
    /** Return a Set of all Edges in this game. */
    public Set<Edge> getEdges();
    
    /** Return the maximum separation between Nodes in the x-direction. */
    public int getWidth();
    
    /** Return the maximum separation between Nodes in the y-direction. */
    public int getHeight();
    
    /** Return the closest Node to the given Point, or null if there are no
     * Nodes on the Board. */
    public Node getClosestNode(Point2D p);
    
    /** Return the Node that the ship is currently on or the Node from which
     * the ship has just departed. */
    public Node getShipNode();
    
    /** Return the current location of the ship in this game. */
    public Point2D getShipLocation();
    
    /** Return the current stage of this Space Gems game. */
    public Stage getStage();
    
    /** Return the remaining amount of distance that can be traveled. */
    public int getDistanceLeft();
    
    /** Return the total distance traveled since the rescue stage started. */
    public int getDistanceTraveled();
    
    /** Return the current amount of gems collected. */
    public int getGems();
    
    /** Return the current score of this game. */
    public int getScore();
    
    /** Return true iff the rescue stage ended successfully. */
    public boolean isRescueSuccessful();
    
    /** Return true iff the return stage ended successfully. */
    public boolean isReturnSuccessful();

    /** An instance describes the current stage of the model. */
    public static enum Stage { RESCUE, RETURN, NONE };
}
