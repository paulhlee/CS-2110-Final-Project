package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import controllers.Controller;

import static gui.SidePanel.StatName.*;
import models.Model;
import models.Model.Stage;
import models.Node;

/** An instance is a graphical representation of a Planet X game. */
@SuppressWarnings("serial")
public class GUI extends JFrame {
    /* Defines buffers to base initial interface drawing on */
    public static final int X_BUFFER= 100;
    public static final int Y_BUFFER= 50;

    /* Specifies the minimum size the drawing board screen must be */
    public static final int DRAWING_BOARD_WIDTH_MIN= 400;
    public static final int DRAWING_BOARD_HEIGHT_MIN= 400;

    /* Dynamic based on the screen size the user has */
    public static final int DRAWING_BOARD_WIDTH;
    public static final int DRAWING_BOARD_HEIGHT;

    /* Two panels aside from the drawing board (fixed dimensions) */
    public static final int UPDATE_PANEL_HEIGHT= 100;
    public static final int SIDE_PANEL_WIDTH= 300;
    
    /* How long to wait for an old renderer to terminate */
    private static final int RESET_TIMEOUT= 3;
    private static final TimeUnit RESET_TIMEOUT_UNITS= TimeUnit.SECONDS;

    /* Set the (width, height) based on user's screen size */
    static {
        Dimension s= Toolkit.getDefaultToolkit().getScreenSize();
        DRAWING_BOARD_WIDTH= s.width - SIDE_PANEL_WIDTH - 2 * X_BUFFER;
        DRAWING_BOARD_HEIGHT= (int) (s.height * 0.8) - UPDATE_PANEL_HEIGHT
            - 2 * Y_BUFFER;
    }

    /* Various panels of the GUI */
    private SpacePanel spacePanel;
    private SidePanel sidePanel;
    private TopMenu menuBar;
    
    /* The model that this GUI is displaying. */
    private Model model;
    
    /* The controller of this game */
    private Controller ctrlr;
    
    /* True iff the model has entered the return stage. */
    private boolean returnStage;
    
    /* True iff the GUI should pause when a new stage begins. */
    private boolean pauseOnReturn;
    
    /* Simulation speed factor; 1 = normal speed, 2 = 2x speed, etc. */
    private int simSpeed;
    
    /* iff true, a Renderer will continue to run */
    private boolean running;
    
    /* iff true, a Renderer will not update the model */
    private boolean paused;
    
    /* The current renderer for this GUI. */
    private Renderer renderer;
    
    /* The currently selected Node. */
    private Node clicked;

    /** Constructor: a paused GUI that displays an empty SpacePanel. */
    public GUI() {
        super("Planet X");
        setMinimumSize(new Dimension(SIDE_PANEL_WIDTH + DRAWING_BOARD_WIDTH_MIN,
            UPDATE_PANEL_HEIGHT + DRAWING_BOARD_HEIGHT_MIN));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationByPlatform(true);

        spacePanel= new SpacePanel(DRAWING_BOARD_WIDTH, DRAWING_BOARD_HEIGHT);
        sidePanel= new SidePanel(SIDE_PANEL_WIDTH, DRAWING_BOARD_HEIGHT);
        menuBar= new TopMenu();
        getContentPane().add(spacePanel, BorderLayout.CENTER);
        getContentPane().add(sidePanel, BorderLayout.EAST);
        setJMenuBar(menuBar);
        addKeyListener(spacePanel.spacePanelCameraMover());
        spacePanel.callWhenClicked(nodeClicked);
        
        running= false;
        paused= true;
        returnStage= false;
        clicked= null;

        simSpeed= SidePanel.INITIAL_SPEED;
        
        // connect listeners
        sidePanel.addSpeedSliderListener(e ->
            simSpeed= ((JSlider) e.getSource()).getValue()
        );
        sidePanel.addFollowShipListener(e ->
            spacePanel.setFollowShip(e.getStateChange() == ItemEvent.SELECTED)
        );
        sidePanel.addZoomSliderListener(e ->
            spacePanel.setZoom(((JSlider) e.getSource()).getValue())
        );
        sidePanel.addPauseListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) pause();
            else unpause();
        });
        sidePanel.addPauseOnReturnListener(e ->
            pauseOnReturn= e.getStateChange() == ItemEvent.SELECTED
        );

        pack();
        validate();
        repaint();
        setVisible(true);
    }
    
    /** An instance animates a Planet X game. */
    private class Renderer extends SwingWorker<Void, Void> {
        @Override
        protected Void doInBackground() {
            running= true;
            long prevTime= System.nanoTime();
            long deltaTime= 0;
            while (running) {
                if (!paused) {
                    long time= System.nanoTime();
                    deltaTime+= time - prevTime;
                    prevTime= time;

                    while (deltaTime >= (Controller.TICKTIME * 1e6)) {
                        deltaTime-= (Controller.TICKTIME * 1e6);
                        for (int i= 0; i < simSpeed; ++i)
                            ctrlr.update();
                    }
                } else {
                    prevTime= System.nanoTime();
                }

                publish((Void) null);
            }

            render();
            return null;
        }
        
        @Override
        protected void process(List<Void> chunks) {
            if (running)
                render();
        }
    };
    
    /** Render the current state of the game. */
    private void render() {
        spacePanel.update();
        updateStats();
        updateClickedStats();
    }
    
    /* The function to run when a Node is clicked. */
    private Consumer<Node> nodeClicked= n -> {
        clicked= n;
        updateClickedStats();
    };
    
    /** Update the displayed stats for the clicked Node. */
    private void updateClickedStats() {
        if (clicked != null) {
            sidePanel.updateStat(CLICKED_NAME, clicked.getName());
            
            if (returnStage) {
                sidePanel.updateStat(CLICKED_GEMS,
                    Integer.toString(clicked.getNumGems()));
            }
            sidePanel.repaint();
        }
    }
    
    /** Update the current stats of the game. */
    private void updateStats() {
        sidePanel.updateStat(PREVIOUS_NAME, model.getShipNode().getName());
        sidePanel.updateStat(SCORE, Integer.toString(model.getScore()));
        if (returnStage) {
            sidePanel.updateStat(GEMS,
                                 Integer.toString(model.getGems()));
            sidePanel.updateStat(DISTANCE_LEFT,
                                 Integer.toString(model.getDistanceLeft()));
        }
    }
    
    /** Initialize this GUI to display the game with m and c, overwriting any
     * previous GUI. */
    public void init(Controller c, Model m) {
        if (!m.getNodes().contains(clicked))
            clicked= null;

        model= m;
        ctrlr= c;
        
        spacePanel.init(m);
        sidePanel.init();

        if (renderer != null) {
            running= false;
            try {
                renderer.get(RESET_TIMEOUT, RESET_TIMEOUT_UNITS);
            } catch (TimeoutException e) {
                System.err.println("error: old GUI not responding!\n"
                                 + "You may want to close this program.");

            } catch (Exception e) {}
        }
        renderer= new Renderer();
        renderer.execute();
    }
    
    /** Pause this GUI, preventing it from updating to the game's state. */
    public void pause() {
        paused= true;
        sidePanel.setPauseBox(true);
    }
    
    /** Unpause this GUI, allowing it to update and reflect the game's state. */
    public void unpause() {
        paused= false;
        sidePanel.setPauseBox(false);
    }
    
    /** Signal that stage s has begun. */
    public void beginStage(Stage s) {
        switch (s) {
        case RESCUE:
            unpause();
            sidePanel.updateStat(STAGE, "rescue");
            break;
            
        case RETURN:
            if (pauseOnReturn) pause();
            sidePanel.updateStat(STAGE, "return");
            returnStage= true;
            break;
        
        case NONE:
            sidePanel.updateStat(STAGE, "none");
        }
    }
    
    /** Signal that stage s has ended. */
    public void endStage(Stage s) {
        switch (s) {
        case RESCUE:
            if (model.isRescueSuccessful()) {
                sidePanel.updateStat(MESSAGE, "Rescue distance traveled: "
                                   + model.getDistanceTraveled());
                sidePanel.updateStat(STAGE, "rescue (completed)");
            } else {
                sidePanel.updateStat(STAGE, "rescue (failed)");
            }
            break;
            
        case RETURN:
            if (model.isReturnSuccessful()) {
                sidePanel.updateStat(STAGE, "return (completed)");
            } else {
                sidePanel.updateStat(STAGE, "return (failed)");
            }
            break;
        
        default:
        }
    }
    
    /** Add an ActionListener to the top menu's "Start" item. */
    public void addStartListener(ActionListener listener) {
        menuBar.getStartItem().addActionListener(listener);
    }
    
    /** Add an ActionListener to the top menu's "Reset" item. */
    public void addResetListener(ActionListener listener) {
        menuBar.getResetItem().addActionListener(listener);
    }
    
    /** Add an ActionListener to the top menu's "New Map" item. */
    public void addNewMapListener(ActionListener listener) {
        menuBar.getNewMapItem().addActionListener(listener);
    }

    /** Update a label id in the side menu to have value v. Pre: id is one of
     * the constants provided in gui.SidePanel */
    public void updateSidebar(SidePanel.StatName sn, String v) {
        sidePanel.updateStat(sn, v);
    }
}
