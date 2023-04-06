import java.awt.*;
import java.util.Set;
import java.util.TreeMap;

/**
 * class to handle the sketch for the server and each client
 *
 * @author nikhilpande and nathanmcallister
 */
public class Sketch {
    private TreeMap<Integer, Shape> shapeMap;
    int id;

    public Sketch() {
        shapeMap = new TreeMap<Integer, Shape>();
        id = 0;
    }

    /**
     * @return the map of all the shapes in the sketch
     */
    public synchronized TreeMap<Integer, Shape> getMap() {
        return shapeMap;
    }

    /**
     * adds a shape to the sketch
     * @param s the shape to add
     */
    public synchronized void addSketch(Shape s) {
        shapeMap.put(id, s);
        id++;
    }

    /**
     * adds shape at a certain id
     * @param id id at which to put shape
     * @param s shape to insert
     */
    public synchronized void addSketch(Integer id, Shape s) {
        shapeMap.put(id, s);
    }

    /**
     * remove a shape from diagram
     * @param id id of shape to remove
     */
    public synchronized void removeSketch(Integer id){
        shapeMap.remove(id);
    }

    /**
     * change color of a shape
     * @param id id of shape to recolor
     * @param color color to change it into
     */
    public synchronized void changeColor(Integer id, Color color) {
        shapeMap.get(id).setColor(color);
    }

    /**
     * move shape
     * @param id id of shape to move
     * @param dx
     * @param dy
     */
    public synchronized void move(Integer id, int dx, int dy) {
        shapeMap.get(id).moveBy(dx, dy);

    }

    /**
     * retrieve the id of the topmost shape that is clicked on (that contains a point p)
     * @param p point where clicked
     * @return id of shape
     */
    public synchronized int getID(Point p) {
        Set<Integer> shapeIDs = shapeMap.descendingKeySet(); // to get the topmost shape
        for(Integer id: shapeIDs)
            if(shapeMap.get(id).contains(p.x, p.y))
                return id; // the first shape that contains the point will return id and end method
        return -1;
    }

    /**
     * toString method
     * @return string of all shapes
     */
    public String toString() {
        String out = "";
        for (Integer i : shapeMap.navigableKeySet()) {
            out += "s " + shapeMap.get(i).toString() + "\n";
        }
        return out;
    }
}
