/*
 * Copyright (c) 2006 Intel Corporation
 * Copyright (c) 2012 Csaba Kiraly
 * All rights reserved.
 *
 * This file is distributed under the terms of The GNU Affero General
 * Public License, version 3 or later. It is based on code distribted
 * as part of TinyOS, which was licensed under the terms of the INTEL
 * LICENSE.
 */

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/* The main GUI object. Build the GUI and coordinate all user activities */
class Window {
    MoteHunter parent;
    Graph graph;
    GraphView graphDir;
    
    Font smallFont = new Font("Dialog", Font.PLAIN, 8);
    Font boldFont = new Font("Dialog", Font.BOLD, 12);
    Font normalFont = new Font("Dialog", Font.PLAIN, 12);
    MoteTableModel moteListModel; // GUI view of mote list
    JLabel xLabel; // Label displaying X axis range
    JTextField sampleText, yText; // inputs for sample period and Y axis range
    JTextField targetText; // inputs for target ID
    JFrame frame;

    UsbIssCmps09 compass;
    public void setCompass(UsbIssCmps09 c) {
      compass = c;
    }

    Window(MoteHunter parent) {
	this.parent = parent;
    }

    /* A model for the mote table, and general utility operations on the mote
       list */
    class MoteTableModel extends AbstractTableModel {
	private ArrayList<Integer> motes = new ArrayList<Integer>();
	private ArrayList<Color> colors = new ArrayList<Color>();
	private ArrayList<Boolean> showTime = new ArrayList<Boolean>();
	private ArrayList<Boolean> showYaw = new ArrayList<Boolean>();

	/* Initial mote colors cycle through this list. Add more colors if
	   you want. */
	private Color[] cycle = {
	    Color.RED, Color.GREEN, Color.BLUE,
	    Color.MAGENTA, Color.BLACK, Color.CYAN,
	    Color.GRAY,
	    Color.PINK, Color.ORANGE
	};
	int cycleIndex;
	
	/* TableModel methods for achieving our table appearance */
	public String getColumnName(int col) {
	    switch (col) {
	    case 0:
		return "Mote";
	    case 1:
		return "Color";
	    case 2:
		return "Time";
	    case 3:
		return "Panorama";
	    default:
		return "unknown";
	    }
	}

	public int getColumnCount() { return 4; }

	public synchronized int getRowCount() { return motes.size(); }
	
	public synchronized Object getValueAt(int row, int col) {
	    switch (col) {
	    case 0:
		return motes.get(row);
	    case 1:
		return colors.get(row);
	    case 2:
		return showTime.get(row);
	    case 3:
		return showYaw.get(row);
	    default:
		return 0;
	    }
	}

        public Class getColumnClass(int col) {
            return getValueAt(0, col).getClass();
        }

	public boolean isCellEditable(int row, int col) {
	    return col == 1 || col == 2 || col == 3;
	}

	public synchronized void setValueAt(Object value, int row, int col) {
	    switch (col) {
	    case 1:
		colors.set(row, (Color)value);
		fireTableCellUpdated(row, col);
		break;
	    case 2:
		showTime.set(row, (Boolean)value);
		break;
	    case 3:
		int i, len = motes.size();
		for (i = 0; i<len; i++) {
		    showYaw.set(i, i==row);
		}
		int nodeID = motes.get(row);
		if (nodeID >=0 && nodeID < 500) {
		    parent.setPingerTarget(nodeID);
		} else if (nodeID >=500 && nodeID < 998) {
		    parent.setPingerTarget(nodeID - 500);
		}
		graphDir.targetId = nodeID;
		break;
	    default:
		return;
	    }
	    graph.repaint();
	    graphDir.repaint();
	    fireTableDataChanged();
        }

	/* Return mote id of i'th mote */
	int get(int i) { return (motes.get(i)).intValue(); }
	
	/* Return color of i'th mote */
	Color getColor(int i)  { return colors.get(i); }
	
	/* Return time flag of i'th mote */
	Boolean getShowTime(int i)  { return showTime.get(i); }

	/* Return ack flag of i'th mote */
	Boolean getShowYaw(int i)  { return showYaw.get(i); }

	/* Return number of motes */
	int size() { return motes.size(); }
	
	/* Add a new mote */
	synchronized void newNode(int nodeId) {
	    /* Shock, horror. No binary search. */
	    int i, len = motes.size();
	    
	    for (i = 0; ; i++) {
		if (i == len || nodeId < get(i)) {
		    motes.add(i, new Integer(nodeId));
		    // Cycle through a set of initial colors
		    colors.add(i, cycle[cycleIndex++ % cycle.length]);
		    showTime.add(i, true);
		    showYaw.add(i, false);
		    break;
		}
	    }
	    fireTableRowsInserted(i, i);
	}
	
	/* Remove all motes */
	void clear() {
	    motes = new ArrayList<Integer>();
	    colors = new ArrayList<Color>();
	    fireTableDataChanged();
	}
    } /* End of MoteTableModel */

    /* A simple full-color cell */
    static class MoteColor extends JLabel implements TableCellRenderer {
	public MoteColor() { setOpaque(true); }
	public Component getTableCellRendererComponent
	    (JTable table, Object color,
	     boolean isSelected, boolean hasFocus, 
	     int row, int column) {
	    setBackground((Color)color);
	    return this;
	}
    }

    /* A simple renderer for the nodeId */
    class MoteName extends JLabel implements TableCellRenderer {
	public MoteName() { setOpaque(true); }
	public Component getTableCellRendererComponent(JTable table, Object nodeId, boolean isSelected, boolean hasFocus, int row, int column) {
	    setText(parent.data.getNodeName((Integer)nodeId));
	    return this;
	}
    }

    /* Convenience methods for making buttons, labels and textfields.
       Simplifies code and ensures a consistent style. */

    JButton makeButton(String label, ActionListener action) {
	JButton button = new JButton();
        button.setText(label);
        button.setFont(boldFont);
	button.addActionListener(action);
	return button;
    }

    JLabel makeLabel(String txt, int alignment) {
	JLabel label = new JLabel(txt, alignment);
	label.setFont(boldFont);
	return label;
    }
    
    JLabel makeSmallLabel(String txt, int alignment) {
	JLabel label = new JLabel(txt, alignment);
	label.setFont(smallFont);
	return label;
    }
    
    JTextField makeTextField(int columns, ActionListener action) {
	JTextField tf = new JTextField(columns);
	tf.setFont(normalFont);
	tf.setMaximumSize(tf.getPreferredSize());
	tf.addActionListener(action);
	return tf;
    }

    /* Build the GUI */
    void setup() {
	JPanel main = new JPanel(new BorderLayout());

	main.setMinimumSize(new Dimension(500, 250));
	main.setPreferredSize(new Dimension(1000, 700));
	
	// Three panels: mote list, graph, controls
	moteListModel = new  MoteTableModel();
	JTable moteList = new JTable(moteListModel);
	moteList.setDefaultRenderer(Integer.class, new MoteName());
	moteList.setDefaultRenderer(Color.class, new MoteColor());
	moteList.setDefaultEditor(Color.class, 
				  new ColorCellEditor("Pick Mote Color"));
	moteList.setPreferredScrollableViewportSize(new Dimension(180, 400));
	moteList.getColumnModel().getColumn(0).setMinWidth(100);
	JScrollPane motePanel = new JScrollPane();
	motePanel.getViewport().add(moteList, null);
	main.add(motePanel, BorderLayout.WEST);
	
	//JPanel graphs = new JPanel(new BorderLayout());
	//main.add(graphs, BorderLayout.CENTER);

	graph = new Graph(this);
	graph.setPreferredSize(new Dimension(200, 200));
	graph.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Time view"));
	//graphs.add(graph, BorderLayout.SOUTH);
	main.add(graph, BorderLayout.NORTH);
	
	graphDir = new GraphView(this);
	graphDir.setCompass(compass);
	graphDir.setPreferredSize(new Dimension(200, 200));
	graphDir.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Panorama view"));
	//graphs.add(graphDir, BorderLayout.CENTER);
	main.add(graphDir, BorderLayout.CENTER);
	
	// Controls. Organised using box layouts.
	
	// Sample period.
	JLabel sampleLabel = makeLabel("Sample period (ms):", JLabel.RIGHT);
	sampleText = makeTextField(6, new ActionListener() {
		public void actionPerformed(ActionEvent e) { setSamplePeriod(); }
	    } );
	updateSamplePeriod();

	// target ID.
	JLabel targetLabel = makeLabel("Target node ID:", JLabel.RIGHT);
	targetText = makeTextField(6, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			setPingerTarget(Integer.decode(targetText.getText().trim()));
		    } catch (NumberFormatException ex) {
			error("Invalid target ID " + targetText.getText());
		    }
		}
	    } );

	// Channel selector
	JLabel channelLabel = makeLabel("Radio channel:", JLabel.RIGHT);
	Integer[] channels = {11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25 ,26};
	final JComboBox channelSelector = new JComboBox(channels);
	channelSelector.setMaximumSize(channelSelector.getPreferredSize());
	channelSelector.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) { setChannel((Integer)channelSelector.getSelectedItem()); }
	    } );
	channelSelector.setSelectedItem(26);
	
	// Clear data.
	JButton clearButton = makeButton("Clear data", new ActionListener() {
		public void actionPerformed(ActionEvent e) { clearData(); }
	    } );
	
	// Adjust X-axis zoom.
	Box xControl = new Box(BoxLayout.X_AXIS);
	xLabel = makeLabel("X:", JLabel.RIGHT);
	final JSlider xSlider = new JSlider(JSlider.HORIZONTAL, 0, 8, graph.scale);
	Hashtable<Integer, JLabel> xTable = new Hashtable<Integer, JLabel>();
	for (int i = 0; i <= 8; i += 2) {
	    xTable.put(new Integer(i),
		       makeSmallLabel("" + ((Graph.MIN_WIDTH << i) * parent.interval / 1000.0 + " s"),
				      JLabel.CENTER));
	}
	xSlider.setLabelTable(xTable);
	xSlider.setPaintLabels(true);
	graph.setScale(graph.scale);
	graphDir.setScale(graph.scale);
	xSlider.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    //if (!xSlider.getValueIsAdjusting())
		    graph.setScale((int)xSlider.getValue());
		    graphDir.setScale((int)xSlider.getValue());
		}
	    });
	xControl.add(xLabel);
	xControl.add(xSlider);
	
	// Adjust Y-axis range.
	JLabel yLabel = makeLabel("Y:", JLabel.RIGHT);
	yText = makeTextField(12, new ActionListener() {
		public void actionPerformed(ActionEvent e) { setYAxis(); }
	    } );
	yText.setText(graph.gy0 + " - " + graph.gy1);
	
	Box controls = new Box(BoxLayout.X_AXIS);
	controls.add(clearButton);
	controls.add(Box.createHorizontalGlue());
	controls.add(Box.createRigidArea(new Dimension(20, 0)));
	controls.add(channelLabel);
	controls.add(channelSelector);
	controls.add(targetLabel);
	controls.add(targetText);
	controls.add(sampleLabel);
	controls.add(sampleText);
	controls.add(Box.createHorizontalGlue());
	controls.add(Box.createRigidArea(new Dimension(20, 0)));
	controls.add(xControl);
	controls.add(yLabel);
	controls.add(yText);
	main.add(controls, BorderLayout.SOUTH);

	// Pause button
	JButton pauseButton = makeButton("Pause", new ActionListener() {
		boolean paused = false;
		public void actionPerformed(ActionEvent e) { paused = !paused; pauseXUpdate(paused); }
	    } );
	controls.add(pauseButton);

	// The frame part
	frame = new JFrame("MoteHunter");
	frame.setSize(main.getPreferredSize());
	frame.getContentPane().add(main);
	frame.setVisible(true);
	frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) { System.exit(0); }
	    });
    }

    /* User operation: clear data */
    void clearData() {
	synchronized (parent) {
	    moteListModel.clear();
	    parent.clear();
	    graph.newData();
	    graphDir.newData();
	}
    }

    /* User operation: pause X axis recentering */
    void pauseXUpdate(Boolean b) {
	synchronized (parent) {
	    graph.pauseXUpdate(b);
	    graphDir.pauseXUpdate(b);
	}
    }

    /* User operation: set Y-axis range. */
    void setYAxis() {
	String val = yText.getText();

	try {
	    int dash = val.indexOf('-');
	    if (dash >= 0) {
		String min = val.substring(0, dash).trim();
		String max = val.substring(dash + 1).trim();

		if (!graph.setYAxis(Integer.parseInt(min), Integer.parseInt(max))) {
		    error("Invalid range " 
			  + min 
			  + " - " 
			  + max 
			  + " (expected values between 0 and 65535)");
		}
		if (!graphDir.setYAxis(Integer.parseInt(min), Integer.parseInt(max))) {
		    error("Invalid range " 
			  + min 
			  + " - " 
			  + max 
			  + " (expected values between 0 and 65535)");
		}
		return;
	    }
	}
	catch (NumberFormatException e) { }
	error("Invalid range " + val + " (expected NN-MM)");
    }

    /* User operation: set sample period. */
    void setSamplePeriod() {
	String periodS = sampleText.getText().trim();
	try {
	    int newPeriod = Integer.parseInt(periodS);
	    if (parent.setInterval(newPeriod)) {
		return;
	    }
	}
	catch (NumberFormatException e) { }
	error("Invalid sample period " + periodS);
    }

    /* User operation: set target node. */
    void setPingerTarget(Integer nodeId) {
	if (parent.setPingerTarget(nodeId)) {
	    graphDir.targetId = nodeId + 500;
	    return;
	}
    }

    /* User operation: set channel. */
    void setChannel(int channel) {
      parent.setChannel(channel);
    }

    /* Notification: sample period changed. */
    void updateSamplePeriod() {
	sampleText.setText("" + parent.interval);
    }

    /* Notification: new node. */
    void newNode(int nodeId) {
	moteListModel.newNode(nodeId);
    }

    /* Notification: new data. */
    void newData() {
	graph.newData();
	graphDir.newData();
    }

    void error(String msg) {
	JOptionPane.showMessageDialog(frame, msg, "Error",
				      JOptionPane.ERROR_MESSAGE);
    }
}
