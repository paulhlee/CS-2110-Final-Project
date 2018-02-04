package controllers;

/** An instance contains the methods that must be implemented in order to solve
 * the game. */
public interface Spaceship {

    /** The spaceship is on the location given by parameter state.
     * Move the spaceship to Planet X and then return, while the spaceship is on
     * Planet X. This completes the first phase of the mission.
     * 
     * If the spaceship continues to move after reaching Planet X, rather than
     * returning, it will not count. If you return from this procedure while
     * not on Planet X, it will count as a failure.
     *
     * There is no limit to how many steps you can take, but your score is
     * directly related to how long it takes you to find Planet X.
     *
     * At every step, you know only the current planet's ID, the IDs of
     * neighboring planets, and the strength of the ping from Planet X at
     * each planet.
     *
     * In this rescueStage,
     * (1) In order to get information about the current state, use functions
     * getCurrentLocation(), getNeighbors(), and getPing().
     *
     * (2) Use method foundSpaceship() to know if you are on Planet X.
     *
     * (3) Use function moveTo(long id) to move to a neighboring planet
     * by its ID. Doing this will change state to reflect your new position. */
    public void rescue(RescueStage state);

    /** The spaceship is on the location given by state. Get back to Earth
     * without running out of fuel and return while on Earth. Your ship can
     * determine how much distance it can travel via method getDistanceLeft().
     * 
     * In addition, each Planet has some gems. Passing over a Planet will
     * automatically collect any gems it carries, which will increase your
     * score; your objective is to return to earth successfully with as many
     * gems as possible.
     * 
     * You now have access to the entire underlying graph, which can be accessed
     * through parameter state. currentNode() and getEarth() return Node objects
     * of interest, and getNodes() returns a collection of all nodes on the
     * graph.
     *
     * Note: Use moveTo() to move to a destination node adjacent to your current
     * node. */
    public void returnToEarth(ReturnStage state);
}