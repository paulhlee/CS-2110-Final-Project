package models;

import controllers.RescueStage;
import controllers.ReturnStage;

/** An instance is controllable by some Controller. */
public interface Controllable extends RescueStage, ReturnStage {
    /** Start the rescue stage and returns an instance which purely implements
     * RescueStage. */
    public RescueStage beginRescueStage();
    
    /** Start the return stage and returns an instance which purely implements
     * ReturnStage. */
    public ReturnStage beginReturnStage();
    
    /** End the rescue stage, returning true iff it was successful and false
     * otherwise. Precondition: the game is in the rescue stage. */
    public boolean endRescueStage();
    
    /** End the return stage, returning true iff it was successful and false
     * otherwise. Precondition: the game is in the return stage. */
    public boolean endReturnStage();
    
    /** Advance the simulation by one tick.
     * Throw SolutionFailedException if the update causes a failure. */
    public void update(int tick) throws SolutionFailedException;
    
    /** Abort the current game. Any attempts to change the state (i.e. by
     * moveTo) will result in an exception. This allows the game to instantly
     * end a solution, rather than stepping through the entire solution. */
    public void abort();
    
    /** An instance indicates that the game has aborted. */
    @SuppressWarnings("serial")
    public static class AbortException extends RuntimeException {}
    
    /** An instance contains a message detailing how a solution failed. */
    @SuppressWarnings("serial")
    public static class SolutionFailedException extends Exception {
        public SolutionFailedException(String msg) {
            super(msg);
        }
    }
}
