package controllers;

import models.Node;
import java.util.Set;

/** Return to Earth on time while collecting as many gems as possible.
 * The rescued spaceship has information on the entire galaxy.
 * All planets will affect your ship's speed in some way. Additionally,
 * you will automatically collect any remaining gems on a planet when moving
 * onto one.
 * 
 * N.B.: There are many other methods in other classes that you will also
 * probably want to use, such as those in Node.
 * 
 * An instance provides all the necessary methods to move through the galaxy,
 * collect speed upgrades, and reach Earth. */
public interface ReturnStage {
    /** Return the Node corresponding to the ship's current location in the graph. */
    public Node getCurrentNode();

    /** Return the Node associated Earth.
     * You have to move to this Node in order to get out. */
    public Node getEarth();

    /** The set of all Nodes in the graph.. */
    public Set<Node> getNodes();

    /** Change the ship's to n.
     * @throws IllegalArgumentException
     *         if n is not adjacent to the ship's location. */
    public void moveTo(Node n);
    
    /** Return the remaining amount of distance that your ship can travel.
     * Your solution must end before this becomes negative. */
    public int getDistanceLeft();
}
