import java.awt.*;
import java.util.ArrayList;

/**
 * handles the messages transmitted between editor and sketch server communicators
 *
 * @author nikhilpande and nathanmcallister
 */
public class Messages {
    private String mode; // editing mode
    private String shapeType; // type of shape we're editing
    private int dfx; // drawFrom x
    private int dfy; // drawFrom y
    private int dtx; // drawTo x
    private int dty; // drawTo y
    private int dx; // move by x distance
    private int dy; // move by y distance
    private int id; // shape id
    private ArrayList<Point> points;
    private Color color; // color of shape

    /**
     * method to decode the messages passed between editor and server communicators
     *
     * @param s message in string form
     */
    public void decodeMsg(String s) {
        String[] tokens = s.split(" ");

        if (tokens[0].equals("a")) { // adding/drawing a new shape
            mode = tokens[0]; // mode = draw mode
            shapeType = tokens[1]; // the second token specifies the shapetype (ellipse, rectangle, segment, freehand)

            if (!shapeType.equals("freehand")) { // ellipse/rectangle/segment
                dfx = Integer.parseInt(tokens[2]);
                dfy = Integer.parseInt(tokens[3]);
                dtx = Integer.parseInt(tokens[4]);
                dty = Integer.parseInt(tokens[5]);
                color = new Color(Integer.parseInt(tokens[6]));
            } else { // freehand
                color = new Color(Integer.parseInt(tokens[tokens.length - 1]));
                points = new ArrayList<Point>();
                for (int i = 2; i < tokens.length - 3; i += 2) { // each pair makes a point, and avoid last (which is the color)
                    points.add(new Point(Integer.parseInt(tokens[i]), Integer.parseInt(tokens[i + 1])));
                }
            }
        }
        else if (tokens[0].equals("m")) { // moving
            mode = tokens[0]; // mode = move mode
            id = Integer.parseInt(tokens[1]);
            dx = Integer.parseInt(tokens[2]);
            dy = Integer.parseInt(tokens[3]);
        }
        else if (tokens[0].equals("r")) { // recoloring
            mode = tokens[0]; // mode = recoloring mode
            id = Integer.parseInt(tokens[1]);
            color = new Color(Integer.parseInt(tokens[2]));
        }
        else if (tokens[0].equals("d")) { // deleting
            mode = tokens[0]; // mode = delete mode
            id = Integer.parseInt(tokens[1]);
        }
        else if (tokens[0].equals("s")) { // first token is "s" when new client is entering, needs to update with current canvas
            mode = "a"; // also adding mode, because it's adding the shapes already on the canvas
            shapeType = tokens[1];

            if (!shapeType.equals("freehand")) {
                dfx = Integer.parseInt(tokens[2]);
                dfy = Integer.parseInt(tokens[3]);
                dtx = Integer.parseInt(tokens[4]);
                dty = Integer.parseInt(tokens[5]);
                color = new Color(Integer.parseInt(tokens[6]));
            } else { // freehand
                points = new ArrayList<Point>();
                for (int i = 2; i < tokens.length - 3; i += 2) {
                    points.add(new Point(Integer.parseInt(tokens[i]), Integer.parseInt(tokens[i + 1])));
                }
                color = new Color(Integer.parseInt(tokens[tokens.length - 1]));
            }

        }
    }


    // getters for the values decoded by the above method
    public String getMode() {return mode;}
    public String getShapeType() {return shapeType;}
    public int getDfx() {return dfx;}
    public int getDfy() {return dfy;}
    public int getDtx() {return dtx;}
    public int getDty() {return dty;}
    public int getDx() {return dx;}
    public int getDy() {return dy;}
    public int getId() {return id;}
    public ArrayList<Point> getPoints() {return points;}
    public Color getColor() {return color;}

}