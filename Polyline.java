import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 * @author nikhilpande and nathanmcallister
 */
public class Polyline implements Shape {
	// TODO: YOUR CODE HERE
	private Color color;
	private ArrayList<Point> points = new ArrayList<Point>();

	public Polyline(Point p, Color color) {
		points.add(p); // add first point
		this.color = color;
	}

	@Override
	public void moveBy(int dx, int dy) {
		for (int i = 0; i < points.size(); i++) {
			points.get(i).x += dx;
			points.get(i).y += dy;
		}
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}

	public boolean contains(int x, int y) {
		for (int i = 0; i < points.size() - 1; i++) {
			if (Segment.pointToSegmentDistance(x, y, points.get(i).x, points.get(i).y, points.get(i+1).x, points.get(i+1).y) <= 10) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		for (int i = 0; i < points.size() - 1; i++) { // stop at second to last point, otherwise index out of bounds
			g.drawLine(points.get(i).x, points.get(i).y, points.get(i+1).x, points.get(i+1).y);
		}
	}

	public void addPoint(Point p) {
		points.add(p);
	}

	@Override
	public void setCorners(int x1, int y1, int x2, int y2) {
	}

	@Override
	public String toString() {
		String s = "";

		for (Point p : points) {
			s += p.x + " " + p.y + " ";
		}
			return "freehand"+" "+s+" "+color.getRGB();
	}
}
