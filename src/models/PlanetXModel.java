package models;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import controllers.RescueStage;
import controllers.ReturnStage;

import static models.Model.Stage.*;
import utils.Utils;

/** An instance maintains the current state of a Planet X game. */
public class PlanetXModel implements Model, Controllable {
    private static final double TOLERANCE= 1e-6; // floating-point tolerance

    private Stage stage; // The current stage of the model
    private final Board board; // The Board associated with this instance 

    private Node shipNode; // The Node that the ship is on or has last visited.
    private Point2D.Double shipLocation; // The current location of the ship.
    private Edge shipEdge; // The Edge on which the ship is traveling. null if still.
    private int distToNext; // The length to reach the next Node

    private int distRemaining; // Distance left to return. < 0 => failed solution.
    private int distTraveled; // Cumulative distance traveled by the ship
    private static final int BASE_SPEED= 100; // Base speed of ship (per second)
    
    private int gems; // The current amount of gems that the ship holds; >= 0
    private int score; // The cumulative score

    private String failMessage; // Iff failed, contains message; else null
    private boolean abort; // True if a game has aborted
    private boolean rescueSuccessful; // True if rescue ended successfully
    private boolean returnSuccessful; // True if return ended successfully

    /** Constructor: a new game with Board b. */
    public PlanetXModel(Board b) {
        stage= NONE;
        board= b;

        shipNode= board.getEarth();
        shipLocation= new Point2D.Double(shipNode.getX(), shipNode.getY());
        shipEdge= null;
        distToNext= 0;

        distTraveled= 0;
        distRemaining= 0;

        gems= 0;
        score= 0;

        failMessage= null;
        abort= false;
        rescueSuccessful= false;
        returnSuccessful= false;
    }
    
    @Override
    public int getWidth() {
        return board.getWidth();
    }
    
    @Override
    public int getHeight() {
        return board.getHeight();
    }
    
    @Override
    public long getSeed() {
        return board.getSeed();
    }
    
    @Override
    public Set<Node> getNodes() {
        HashSet<Node> ns= new HashSet<>();
        for (Node n : board.getNodes())
            ns.add(n);
        return ns;
    }
    
    @Override
    public Set<Edge> getEdges() {
        return board.getEdges();
    }
    
    @Override
    public Node getClosestNode(Point2D p) {
        return board.getClosestNode(p);
    }
    
    @Override
    public Node getShipNode() {
        return shipNode;
    }
    
    @Override
    public Point2D getShipLocation() {
        return shipLocation;
    }
    
    @Override
    public Stage getStage() {
        return stage;
    }
    
    @Override
    public int getDistanceLeft() {
        return distRemaining;
    }
    
    @Override
    public int getDistanceTraveled() {
        return distTraveled;
    }
    
    @Override
    public int getScore() {
        return score;
    }
    
    @Override
    public synchronized void update(int tick) throws SolutionFailedException {
        if (failMessage != null) {
            throw new SolutionFailedException(failMessage);
        }

        if (shipEdge != null) {
            Node shipNext= shipEdge.getOther(shipNode);
            double oldDist= Utils.distance(
                shipLocation.x, shipLocation.y,
                shipNext.getX(), shipNext.getY());
            double travelDist= BASE_SPEED * (tick / 1e3);
            double totalDiff= oldDist - shipEdge.length;
            if (totalDiff >= -TOLERANCE)
                travelDist+= totalDiff + TOLERANCE;
            if (travelDist > oldDist) {
                elapseTime(distToNext);
                distToNext= 0;
                shipArrive();
                notifyAll();
            } else {
                double newDist= oldDist - travelDist;
                double r= newDist / shipEdge.length;
                shipLocation.x= r * shipNode.getX() + (1 - r) * shipNext.getX();
                shipLocation.y= r * shipNode.getY() + (1 - r) * shipNext.getY();
                int diff= distToNext - (int) newDist;
                if (diff > 0) {
                    distToNext-= diff;
                    elapseTime(diff);
                }
            }
        }
    }
    
    /** Simulate time passing based on the given distance.
     * Rescue: score decreases by one point per distance traveled
     * Return: remaining distance decreases */
    private void elapseTime(int distance) throws SolutionFailedException {
        distTraveled+= distance;
        if (stage == Stage.RESCUE) {
            score= score > distance ? score - distance : 0;
        } else {
            distRemaining-= distance;
            if (distRemaining < 0) {
                failMessage= "ran out of fuel and can no longer travel.";
                throw new SolutionFailedException(failMessage);
            }
        }
    }
    
    /** Make the ship arrive to its next destination.
     * Precondition: the ship is moving between two Nodes. */
    private void shipArrive() {
        shipNode= shipEdge.getOther(shipNode);
        shipLocation.x= shipNode.getX();
        shipLocation.y= shipNode.getY();
        shipEdge= null;
    }

    @Override
    public int getLocation() {
        return shipNode.getId();
    }

    @Override
    public double getPing() {
        return board.getPing(shipNode);
    }

    @Override
    public NodeStatus[] neighbors() {
        Set<Node> nodes= shipNode.getNeighbors().keySet();
        NodeStatus[] ns = new NodeStatus[nodes.size()];
        int i= 0;
        for (Node n : nodes) {
            ns[i]= new NodeStatus(n.getId(), n.getName(), board.getPing(n));
            ++i;
        }
        return ns;
    }

    @Override
    public boolean foundSpaceship() {
        return shipNode == board.getTarget();
    }

    @Override
    public Node getCurrentNode() {
        return shipNode;
    }

    @Override
    public Node getEarth() {
        return board.getEarth();
    }
    
    /** When called, blocks until the ship has moved from shipNode to n. */
    private synchronized void waitUntilMoved(Node n) {
        shipEdge= shipNode.getConnect(n);
        shipEdge.visit();
        distToNext= shipEdge.length;
        while (shipEdge != null) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }

    @Override
    public synchronized void moveTo(int id) {
        if (abort) throw new AbortException();
        if (failMessage != null) waitUntilAbort();
            
        for (Node n : shipNode.getNeighbors().keySet()) {
            if (n.getId() == id) {
                waitUntilMoved(n);
                return;
            }
        }
        failMessage= "tried to call moveTo to a non-adjacent ID.";
        waitUntilAbort();
    }

    @Override
    public synchronized void moveTo(Node n) {
        if (abort) throw new AbortException();
        if (failMessage != null) waitUntilAbort();

        if (!shipNode.isConnectedTo(n)) {
            failMessage= "tried to call moveTo to a non-adjacent Node.";
            waitUntilAbort();
        }

        waitUntilMoved(n);

        int g= n.takeGems();
        gems+= g;
        score+= g;
    }
    
    @Override
    public int getGems() {
        return gems;
    }
    
    @Override
    public RescueStage beginRescueStage() {
        stage= RESCUE;
        score= board.distanceToTarget() * 2;

        return new RescueStage() {
            @Override
            public int getLocation() {
                return PlanetXModel.this.getLocation();
            }

            @Override
            public double getPing() {
                return PlanetXModel.this.getPing();
            }

            @Override
            public NodeStatus[] neighbors() {
                return PlanetXModel.this.neighbors();
            }
            
            @Override
            public boolean foundSpaceship() {
                return PlanetXModel.this.foundSpaceship();
            }

            @Override
            public void moveTo(int id) {
                PlanetXModel.this.moveTo(id);
            }

        };
    }
    
    @Override
    public ReturnStage beginReturnStage() {
        stage= RETURN;
        distRemaining= board.getSumEdges() / 2 + board.distanceToTarget();

        return new ReturnStage() {
            @Override
            public Node getCurrentNode() {
                return PlanetXModel.this.getCurrentNode();
            }

            @Override
            public Node getEarth() {
                return PlanetXModel.this.getEarth();
            }

            @Override
            public Set<Node> getNodes() {
                return PlanetXModel.this.getNodes();
            }

            @Override
            public void moveTo(Node n) {
                PlanetXModel.this.moveTo(n);
            }

            @Override
            public int getDistanceLeft() {
                return PlanetXModel.this.getDistanceLeft();
            }
        };
    }

    @Override
    public boolean endRescueStage() {
        if (stage != RESCUE) throw new IllegalStateException(
            "error: not in rescue stage; can't end rescue stage");

        stage= NONE;
        rescueSuccessful= shipNode == board.getTarget();
        if (!rescueSuccessful)
            score= 0;
        return rescueSuccessful;
    }

    @Override
    public boolean endReturnStage() {
        if (stage != RETURN) throw new IllegalStateException(
            "error: not in return stage; can't end return stage");

        stage= NONE;
        returnSuccessful= shipNode == board.getEarth();
        if (!returnSuccessful)
            score= 0;
        return returnSuccessful;
    }
    
    @Override
    public boolean isRescueSuccessful() {
        return rescueSuccessful;
    }
    
    @Override
    public boolean isReturnSuccessful() {
        return returnSuccessful;
    }
    
    /** Block until the game is aborted, then throws an AbortException. */
    private synchronized void waitUntilAbort() throws AbortException {
        while (!abort) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        throw new AbortException();
    }
    
    @Override
    public synchronized void abort() {
        abort= true;

        // If the ship was moving, forcibly stop it
        if (shipEdge != null) {
            shipArrive();
            notifyAll();
        }
    }
}