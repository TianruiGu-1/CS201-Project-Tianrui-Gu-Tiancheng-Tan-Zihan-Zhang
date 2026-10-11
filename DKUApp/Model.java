import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * BACK-END. Owns the campus graph and whatever the user has selected.
 *
 * The rules for this layer stay the same:
 *   - The view never computes. It reads from here and paints.
 *   - Anything that appears on screen must live in this model.
 *
 * Coordinates in and out of here are raw map units, never canvas pixels -- the
 * front end does the projection both ways. The data structures all live here or
 * in the graph this model owns: the adjacency list, the hash map from name to
 * node, and whatever the algorithm keeps while it runs -- the scan for the
 * smallest distance in Dijkstra.java, the queue inside isConnected.
 */
public class Model {

    /**
     * How close a click has to land to count as hitting a place, in raw map
     * units -- roughly 30 canvas pixels at the scale the view draws at.
     */
    private static final double CLICK_RADIUS = 120;

    private final CampusGraph campus = CampusData.build();

    private Node start;
    private Node end;
    private List<Node> route = Collections.emptyList();
    private boolean showWaypoints;

    public Model() {
        // The map is unusable if some place cannot be reached, and this is
        // exactly the kind of mistake data like this tends to contain.
        if (!campus.isConnected()) {
            System.err.println("WARNING: the campus map is not fully connected. "
                    + "Some places cannot be reached.");
        }
    }

    public CampusGraph getCampus() { return campus; }

    /** The coordinate space the node positions live in. */
    public int getMapWidth() { return CampusData.MAP_WIDTH; }

    public int getMapHeight() { return CampusData.MAP_HEIGHT; }

    public Node getStart() { return start; }

    public Node getEnd() { return end; }

    public List<Node> getRoute() { return route; }

    /**
     * Whether the map draws the unnamed bends as well as the places worth
     * naming. Off by default: the bends are scaffolding, and the named places
     * are the answer.
     *
     * A display flag is a strange thing to keep next to the graph, but the rule
     * this layer follows is that whatever appears on screen is read from here,
     * so that the view holds no state of its own. This is the same rule that
     * makes one refresh() enough to keep the window honest.
     */
    public boolean isShowingWaypoints() { return showWaypoints; }

    public void setShowingWaypoints(boolean show) { showWaypoints = show; }

    /**
     * The route stops worth naming: the places it passes through, with the bare
     * bends in the path left out. Every waypoint is a corner the walker turns at
     * and would not recognise on a signpost, so listing them would only bury the
     * two or three names that actually matter.
     */
    public List<Node> getPublicStops() {
        List<Node> stops = new ArrayList<>();
        for (Node node : route) {
            if (node.isSelectable()) {
                stops.add(node);
            }
        }
        return stops;
    }

    /**
     * What the route costs: the walk, plus the equivalent distance of every gate
     * passed through. The start and the destination are both exempt -- see
     * CampusGraph.routeLength.
     */
    public double getRouteLength() { return campus.routeLength(route); }

    /**
     * Handle a click at map coordinates: pick the place under it, then move the
     * selection on by one step -- nothing, then start, then end, then start
     * again.
     */
    public void clickAt(double x, double y) {
        Node hit = nearestSelectable(x, y);
        if (hit == null) {
            return;
        }
        if (start == null) {
            start = hit;
        } else if (end == null && !hit.equals(start)) {
            end = hit;
        } else {
            start = hit;
            end = null;
        }
        recomputeRoute();
    }

    /**
     * The place whose centre is closest to the click, if any is close enough.
     *
     * Only real destinations are considered. The bare bends in a path are
     * invisible on the map, so letting a click land on one would look like the
     * program had picked something at random.
     */
    private Node nearestSelectable(double x, double y) {
        return campus.getNodes().stream()
                .filter(Node::isSelectable)
                .filter(n -> distance(n, x, y) < CLICK_RADIUS)
                .min(Comparator.comparingDouble(n -> distance(n, x, y)))
                .orElse(null);
    }

    private static double distance(Node n, double x, double y) {
        return Math.sqrt((n.getX() - x) * (n.getX() - x) + (n.getY() - y) * (n.getY() - y));
    }

    private void recomputeRoute() {
        if (start != null && end != null) {
            route = Dijkstra.shortestPath(campus, start.getName(), end.getName());
        } else {
            route = Collections.emptyList();
        }
    }

    /** Clear the selection and the route. */
    public void reset() {
        start = null;
        end = null;
        route = Collections.emptyList();
    }
}
