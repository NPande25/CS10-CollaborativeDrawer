import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Client-server graphical editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 */

public class Editor extends JFrame {
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged


	// Communication
	private EditorCommunicator comm;			// communication with the sketch server
	private String msg = "";
	private Messages m;

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();


		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};

		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease(event.getPoint());
			}
		});

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});

		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// TODO: YOUR CODE HERE
		// draw the sketch
		for (Integer i : sketch.getMap().navigableKeySet()) { // need a method in sketch to get all shapes in order
			Shape shape = sketch.getMap().get(i);
			if (shape != null) { // draw each shape if it isn't null
				shape.draw(g);
			}
		}
	}

	// Helpers for event handlers

	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		// TODO: YOUR CODE HERE
		String msg = "";
		int id = sketch.getID(p);
		curr = sketch.getMap().get(id);

		if (mode == Mode.DRAW) {
			if (shapeType.equals("ellipse")) {
				curr = new Ellipse(p.x, p.y, color);
				drawFrom = p;
			}
			else if (shapeType.equals("rectangle")) {
				curr = new Rectangle(p.x, p.y, color);
				drawFrom = p;
			}
			else if (shapeType.equals("segment")) {
				curr = new Segment(p.x, p.y, color);
				drawFrom = p;
			}
			else if (shapeType.equals("freehand")) {
				curr = new Polyline(p, color);
			}
			sketch.addSketch(-1, curr); // temporarily adding so local canvas will show
		}
		else if (curr != null && curr.contains(p.x, p.y))
			if (mode == Mode.MOVE) {
				moveFrom = p;
				// request to start dragging that shape
				msg += "m " + id + " " + 0 + " " + 0; // move by dx, dy = 0
				comm.send(msg);
			}
			else if (mode == Mode.RECOLOR) {
				// request to recolor that shape
				msg += "r " + id + " " + color.getRGB();
				comm.send(msg);
				repaint();
			}
			else if (mode == Mode.DELETE){ // delete
				// request to delete that shape
				msg += "d " + id;
				comm.send(msg);
				repaint();
		}
	}


	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		// TODO: YOUR CODE HERE
		String msg = "";
		if (mode == Mode.DRAW) {
			if (shapeType.equals("ellipse") || shapeType.equals("rectangle") || shapeType.equals("segment")) {
				curr.setCorners(drawFrom.x, drawFrom.y, p.x, p.y); // adjust the corners with the drag
			}
			else if (shapeType.equals("freehand")) {
				((Polyline) curr).addPoint(p); // for freehands, everywhere the mouse drags, add another point
			}
		}
		else if (mode == Mode.MOVE) {
			int dx = p.x - moveFrom.x;
			int dy = p.y - moveFrom.y;
			msg += "m " + sketch.getID(p) + " " + dx + " " + dy;
			comm.send(msg);
			moveFrom = p;
		}
		repaint();
	}


	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it
	 */
	private void handleRelease(Point p) {
		// TODO: YOUR CODE HERE
		String msg = "";
		if (mode == Mode.DRAW) {
			if (!shapeType.equals("freehand")) {
				msg += "a " + curr.toString();
				comm.send(msg); // now that the shape is complete, add it to the global
			}
			else { // freehand
				msg += "a " + curr.toString() + " " + color.getRGB();
				comm.send(msg); // now that the freehand is complete, add it to the global
			}
		}
		if (mode == Mode.MOVE) {
			moveFrom = p;
		}
	}

	public void setLocal(String line) {
		m = new Messages();
		m.decodeMsg(line);
		if (m.getMode().equals("a")) {
			if (m.getShapeType().equals("rectangle")) {
				// when order is received from server, create this new shape on local canvas
				sketch.addSketch(new Rectangle(m.getDfx(), m.getDfy(), m.getDtx(), m.getDty(), m.getColor()));
			}
			else if (m.getShapeType().equals("ellipse")) {
				sketch.addSketch(new Ellipse(m.getDfx(), m.getDfy(), m.getDtx(), m.getDty(), m.getColor()));
			}
			else if (m.getShapeType().equals("segment")) {
				sketch.addSketch(new Segment(m.getDfx(), m.getDfy(), m.getDtx(), m.getDty(), m.getColor()));
			}
			else { // freehand
				ArrayList<Point> points = m.getPoints();
				Polyline freehand = new Polyline(points.get(0), m.getColor());
				for (int i = 1; i < points.size(); i++) {
					freehand.addPoint(points.get(i));
				}
				sketch.addSketch(freehand);
			}
			sketch.removeSketch(-1); // remove the temporary local shape we had, avoid duplicate

		}
		else if (m.getMode().equals("m")) {
			if (m.getId() != -1) // if a valid shape is being moved
				sketch.move(m.getId(), m.getDx(), m.getDy());
		}
		else if (m.getMode().equals("d")) {
			sketch.removeSketch(m.getId());
		}
		else if (m.getMode().equals("r")) {
			sketch.changeColor(m.getId(), m.getColor());
		}
		repaint();
	}


	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});
	}
}
