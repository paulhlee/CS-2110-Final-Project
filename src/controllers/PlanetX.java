package controllers;

import java.util.Random;
import java.util.function.Supplier;

import models.Board;
import models.Controllable.AbortException;
import models.Controllable.SolutionFailedException;
import models.PlanetXModel;
import static models.Model.Stage.*;
import views.*;
import student.MySpaceship;

/** An instance runs the game, linking the state to the user interface. */
public class PlanetX implements Controller {
    /* Tunable map generation parameters. */
    public static final int MIN_NODES= 5;
    public static final int MAX_NODES= 750;    //750
    public static final int MIN_GEMS= 0;
    public static final int MAX_GEMS= 5000;
    public static final int WIDTH= 4096;
    public static final int HEIGHT= 4096;

    private long seed; // The seed used to generate this game.
    private Spaceship spaceship; // The solution implementing this game. 

    private boolean started; // True iff this game has started.
    private boolean failed; // True iff this game's solution failed.

    private PlanetXModel model; // The controllable model for this game.
    private View view; // The view for this game.

    private static final Random RNG= new Random(); // used for random seed generation.

    /* A Spaceship supplier used to get new Spaceships (e.g. for restarting). */
    private static final Supplier<Spaceship> ships= () -> new MySpaceship();

    /* Separate thread used to prevent the model from blocking the view */
    private ModelThread thread;

    /** Constructor: an game with seed s, spaceship sp, and View v. */
    public PlanetX(long s, Spaceship sp, View v) {
        view= v;
        init(s, sp);
        started= false;
        failed= false;
    }

    /** Initialize the game with seed s and spaceship sp. If this game has
     * already been initialized, this simply overwrites the previous
     * initialization. */
    private void init(long s, Spaceship sp) {
        // stop the old thread, if it exists
        if (thread != null)
            thread.kill();

        seed= s;
        spaceship= sp;
        Board b= new Board.BoardBuilder()
                .size(WIDTH, HEIGHT)
                .seed(s)
                .nodeBounds(MIN_NODES, MAX_NODES)
                .gemBounds(MIN_GEMS, MAX_GEMS)
                .build();
        model= new PlanetXModel(b);
        thread= new ModelThread();
        started= false;
        view.init(this, model);
    }

    @Override
    public void newGame(String str) {
        if (str == null) return;
        try {
            init(Long.valueOf(str), ships.get());
        } catch (NumberFormatException ex) {
            init(RNG.nextLong(), ships.get());
        }
    }

    @Override
    public void reset() {
        init(seed, ships.get());
    }

    @Override
    public void start() {
        if (started) {
            view.errprintln("Game has already started");
            return;
        }
        started= true;
        thread.start();
    }
    
    @Override
    public synchronized void update() {
        try {
            model.update(TICKTIME);
        } catch (SolutionFailedException e) {
            if (!failed) {
                failed= true;
                view.errprintln("Solution failed with reason: " + e.getMessage());
                view.endGame(0);
            }
        }
    }

    /** An instance runs a model in a separate thread. It can be killed by
     * calling kill(). */
    private class ModelThread extends Thread {
        /** Runs through the game until it finishes, fails, or is aborted. */
        @Override
        public void run() {
            try {
                rescue();
                returnToEarth();
                view.endGame(model.getScore());
            } catch (SolutionFailedException e) {
                view.errprintln("Solution failed with reason: " + e.getMessage());
                view.endGame(0);
            } catch (AbortException e) {}
        }

        /** Kill this model thread by aborting the underlying model. */
        public void kill() {
            model.abort();
        }
    }

    /** Run ship's rescue method. Throw a SolutionFailedException if the
     * rescue fails. */
    private void rescue() throws SolutionFailedException {
        view.beginStage(RESCUE);
        spaceship.rescue(model.beginRescueStage());
        boolean success= model.endRescueStage();
        view.endStage(RESCUE);
        if (success)
            return;

        throw new SolutionFailedException("Your solution to "
                + "rescue() returned at the wrong location.");
    }

    /** Run ship's returnToEarth method. Throw a SolutionFailedException if
     * the return fails. */
    private void returnToEarth() throws SolutionFailedException {
        view.beginStage(RETURN);
        spaceship.returnToEarth(model.beginReturnStage());
        boolean success= model.endReturnStage();
        view.endStage(RETURN);
        if (success)
            return;

        throw new SolutionFailedException("Your solution to "
                + "returnToEarth() returned at the wrong location.");
    }
    
    @Override
    public boolean isRescueSuccessful() {
        return model.isRescueSuccessful();
    }
    
    @Override
    public boolean isReturnSuccessful() {
        return model.isReturnSuccessful();
    }

    /** Run the Space Gems game. Without any options, this defaults to a game
     * with a random seed using a GUI view.
     * 
     * -s, --seed=SEED  Run this game using the seed SEED 
     * -g, --gui        Use the GUI (graphical user interface) view 
     * -q, --quiet      Use a quiet view, which will not output anything. */
    public static void main(String[] argv) {
        // parse arguments
        View view= null;
        Long seed= null;
        for (int i= 0; i < argv.length; ++i) {
            try {
                if (argv[i].equals("-g") || argv[i].equals("--gui")) {
                    if (view != null) {
                        System.err.println("Error: cannot specify more than "
                                + "one view option");
                        return;
                    } else {
                        view= new GUIView();
                    }
                } else if (argv[i].equals("-c") || argv[i].equals("--cli")) {
                    if (view != null) {
                        System.err.println("Error: cannot specify more than "
                                + "one view option");
                        return;
                    } else {
                        view= new CLIView();
                    }
                } else if (argv[i].equals("-q") || argv[i].equals("--quiet")) {
                    if (view != null) {
                        System.err.println("Error: cannot specify more than "
                                + "one view option");
                        return;
                    } else {
                        view= new QuietView();
                    }
                } else if (argv[i].length() > 7
                        && argv[i].substring(0, 7).equals("--seed=")) {
                    seed= Long.parseLong(argv[i].substring(7));
                } else if (argv[i].equals("-s")) {
                    if (i + 1 < argv.length) {
                        ++i;
                        seed= Long.parseLong(argv[i]);
                    } else {
                        System.err.println("Error: no seed specified.");
                        return;
                    }
                } else {
                    System.err.println("Error: invalid argument \""
                            + argv[i] + '"');
                    return;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid seed \"" + argv[i] + '"');
                return;
            }
        }
        if (seed == null)
            seed= RNG.nextLong(); // avoid burning RNG; only generate if needed

        // begin the game with the appropriate parameters
        if (view == null)
            view= new GUIView();
        new PlanetX(seed, ships.get(), view);
    }
}
