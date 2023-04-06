import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 * @author nikhilpande and nathanmcallister
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for
	private Messages m;

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}
	
	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");
			
			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			// TODO: YOUR CODE HERE
			for (Integer id : server.getSketch().getMap().navigableKeySet()) {
				// relay each element to the new client
				Shape s = server.getSketch().getMap().get(id);
				out.println("s " + s.toString()); // "s" keyword informs it to add the following shape
			}

			// Keep getting and handling messages from the client
			// TODO: YOUR CODE HERE
			m = new Messages();
			String line = "";
			while ((line = in.readLine()) != null) {
				m.decodeMsg(line);
				if (m.getMode().equals("a")) { // adding/drawing mode
					if (m.getShapeType().equals("ellipse")) {
						server.getSketch().addSketch(new Ellipse(m.getDfx(), m.getDfy(), m.getDtx(), m.getDty(), m.getColor()));
					}
					else if (m.getShapeType().equals("rectangle")) {
						server.getSketch().addSketch(new Rectangle(m.getDfx(), m.getDfy(), m.getDtx(), m.getDty(), m.getColor()));
					}
					else if (m.getShapeType().equals("segment")) {
						server.getSketch().addSketch(new Segment(m.getDfx(), m.getDfy(), m.getDtx(), m.getDty(), m.getColor()));
					}
					else if (m.getShapeType().equals("freehand")) {
						ArrayList<Point> points = m.getPoints();
						Polyline freehand = new Polyline(points.get(0), m.getColor());
						for (int i = 1; i < points.size(); i++) {
							freehand.addPoint(points.get(i));
						}
						server.getSketch().addSketch(freehand);
					}
				} else if (m.getMode().equals("m")) { // moving mode
					if (m.getId() != -1) // if a valid shape is being moved
						server.getSketch().getMap().get(m.getId()).moveBy(m.getDx(), m.getDy()); // get the shape, move by
				}
				else if (m.getMode().equals("r")) { // recoloring
					server.getSketch().changeColor(m.getId(), m.getColor()); // access shape at this id and change color
				}
				else if (m.getMode().equals("d")) { // deleting mode
					server.getSketch().removeSketch(m.getId()); // remove shape at this id
				}
				System.out.println("Master Sketch: \n" + server.getSketch().toString());

				server.broadcast(line); // now that server sketch changes have been made, broadcast the same instructions
			}							// to all the clients so they can update their canvases accordingly

			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}