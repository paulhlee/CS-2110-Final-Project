package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemListener;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

/** An instance is a JPanel that displays various stats and has sliders to
 * control the view of the GUI. */
@SuppressWarnings("serial")
public class SidePanel extends JPanel {
    /* Values for the speed slider. */
    public static final int MINIMUM_SPEED= 1;
    public static final int MAXIMUM_SPEED= 100;
    public static final int INITIAL_SPEED= MINIMUM_SPEED;
    
    /* Values for the zoom slider. */
    public static final int MINIMUM_ZOOM= 1;
    public static final int MAXIMUM_ZOOM= 10;
    public static final int INITIAL_ZOOM= MINIMUM_ZOOM;

    /* The font used to display stats on this Panel. */
	private static final Font LABEL_FONT= new Font("SansSerif", Font.BOLD, 14);
	private static final Font STAT_FONT= new Font("SansSerif", Font.PLAIN, 16);

	/* Stats currently on the SidePanel. */
	private HashMap<StatName, Stat> stats;
	
	/** An instance is a stat that can be displayed on a SidePanel. */
	public static enum StatName {
	    MESSAGE,
		STAGE,
		PREVIOUS_NAME,
		GEMS,
		SCORE,
		DISTANCE_LEFT,
		CLICKED_NAME,
		CLICKED_GEMS
	}
	
	/* Contains various control tools for this game. */
	private JPanel ctrlPanel;

	/* The sliders on panel. */
	private JSlider speedSlider;
	private JSlider zoomSlider;
	
	/* The check boxes on panel. */
	private JCheckBox followShipBox;
	private JCheckBox pauseBox;
	private JCheckBox pauseOnReturnBox;

	/** Create a new side panel of dimension (width, height). */
	public SidePanel(int width, int height) {
		super();

		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(width, height));
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		stats= new HashMap<>();

		addText("Progress");
		addStat(StatName.MESSAGE, "", "");
		addStat(StatName.STAGE, "Stage: ", "not started");
		addStat(StatName.PREVIOUS_NAME, "Previous planet: ", "N/A");
		addStat(StatName.GEMS, "Gems: ", "N/A");
		addStat(StatName.SCORE, "Score: ", "N/A");
		addStat(StatName.DISTANCE_LEFT, "Distance remaining: ", "N/A");
		addText(" ");
		addText("Clicked planet");
		addStat(StatName.CLICKED_NAME, "Name: ", "N/A");
		addStat(StatName.CLICKED_GEMS, "Gems: ", "N/A");
		addText(" ");

		ctrlPanel= new JPanel();
		ctrlPanel.add(new JLabel("Simulation speed"));
		ctrlPanel.setBackground(Color.WHITE);
		speedSlider= makeSlider(MINIMUM_SPEED, MAXIMUM_SPEED, INITIAL_SPEED);
		ctrlPanel.add(speedSlider);
		ctrlPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		add(ctrlPanel);
		
		ctrlPanel.add(new JLabel("Zoom"));
		ctrlPanel.setBackground(Color.WHITE);
		zoomSlider= makeSlider(MINIMUM_ZOOM, MAXIMUM_ZOOM, INITIAL_ZOOM);
		ctrlPanel.add(zoomSlider);

		followShipBox= new JCheckBox("Camera follows ship", false);
		followShipBox.setBackground(Color.WHITE);
		followShipBox.setFocusable(false);
		ctrlPanel.add(followShipBox);

		pauseBox= new JCheckBox("Pause", false);
		pauseBox.setBackground(Color.WHITE);
		pauseBox.setFocusable(false);
		pauseBox.setSelected(true);
		ctrlPanel.add(pauseBox);

		pauseOnReturnBox= new JCheckBox("Pause when beginning the return stage", false);
		pauseOnReturnBox.setBackground(Color.WHITE);
		pauseOnReturnBox.setFocusable(false);
		ctrlPanel.add(pauseOnReturnBox);

		ctrlPanel.setLayout(new BoxLayout(ctrlPanel, BoxLayout.Y_AXIS));
		ctrlPanel.add(Box.createVerticalGlue());
		add(ctrlPanel);
	}
	
	public void init() {
		updateStat(StatName.MESSAGE, "");
		updateStat(StatName.STAGE, "not started");
		updateStat(StatName.PREVIOUS_NAME, "N/A");
		updateStat(StatName.GEMS, "N/A");
		updateStat(StatName.SCORE, "N/A");
		updateStat(StatName.DISTANCE_LEFT, "N/A");
		updateStat(StatName.CLICKED_NAME, "N/A");
		updateStat(StatName.CLICKED_GEMS, "N/A");
	    repaint();
	}

    /** If labels contains t, do nothing; else, append a new label into the menu
     * with text t. This label cannot be updated later. */
	public void addText(String t) {
		JLabel label= new JLabel(t);
		label.setFont(LABEL_FONT);
		add(label);
	}

	/** Returns a new horizontal Slider with the given parameters. */
	private JSlider makeSlider(int min, int max, int init) {
        JSlider slider= new JSlider(JSlider.HORIZONTAL, min, max, init);
		slider.setBackground(Color.WHITE);
		int range= max - min;
		slider.setMajorTickSpacing(range / 10);
		slider.setMinorTickSpacing(range / 5);
		slider.setPaintTicks(true);
		slider.setPaintLabels(false);
		slider.setFocusable(false);
		return slider;
	}
	
	/** Add a ChangeListener to this SidePanel's speed slider. */
	public void addSpeedSliderListener(ChangeListener listener) {
	    speedSlider.addChangeListener(listener);
	}
	
	/** Add a ChangeListener to this SidePanel's zoom slider. */
	public void addZoomSliderListener(ChangeListener listener) {
	    zoomSlider.addChangeListener(listener);
	}
	
	/** Add an ItemListener to this SidePanel's follow ship check box. */
	public void addFollowShipListener(ItemListener listener) { 
	    followShipBox.addItemListener(listener);
	}
	
	/** Add an ItemListener to this SidePanel's pause check box. */
	public void addPauseListener(ItemListener listener) {
	    pauseBox.addItemListener(listener);
	}
	
	/** Set this SidePanel's pause check box to checked or unchecked. */
	public void setPauseBox(boolean checked) {
	    pauseBox.setSelected(checked);
	}
	
	/** Add an ItemListener to this SidePanel's pause on return check box. */
	public void addPauseOnReturnListener(ItemListener listener) {
	    pauseOnReturnBox.addItemListener(listener);
	}
	
    /** Append a statistic to display on the menu. The number displayed can be
     * updated later using the specified StatName. */
	public void addStat(StatName sn, String name, String value) {
		Stat stat= new Stat(name, value);
		stats.put(sn, stat);
		add(stat.label);
	}

	/** Update an existing statistic to display value. */
	public void updateStat(StatName sn, String value) {
		if (!stats.containsKey(sn))
		    throw new IllegalArgumentException("Uninitialized stat " + sn);

		stats.get(sn).update(value);
	}

    /** An instance is a statistic displayed in this SidePanel. */
	private static class Stat {
		String name; // the first block of text displayed for this stat 
		String value; // the second block of text displayed for this stat
		JLabel label; // the JLabel used to display this stat

        /** Constructor: a stat with name n and value v. */
		public Stat(String n, String v) {
			name= n;
			value= v;
			label= new JLabel(name + value);
			label.setFont(STAT_FONT);
		}

		/** Change this stat's value to v. */
		public void update(String v) {
			value= v;
			label.setText(name + value);
		}
	}

}
