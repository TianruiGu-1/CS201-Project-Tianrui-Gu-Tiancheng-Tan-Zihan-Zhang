/**
 * BACK-END. One point on the campus map -- a node in the graph.
 *
 * Position is in raw map units: origin at the bottom-left of the map page, +y
 * pointing UP. That is the opposite of Swing's rule, so the front end flips y
 * when it paints and this class never has to think about the screen at all.
 *
 * A node is one of two kinds. Either it is somewhere you would actually want to
 * go -- a building entrance, a gate -- and it is drawn large and named on the
 * map, or it is a bare bend in a path, drawn small and named nothing. The JSON
 * this data comes from calls the first kind "public", which is the word
 * isSelectable() reports.
 *
 * A gate charges a flat equivalent distance the moment a route passes into it.
 * That charge is stored here but never added here -- CampusGraph folds it into
 * the road weight, so the algorithms can stay ignorant of it entirely.
 */
public class Node {

    private final String name;
    private final double x;
    private final double y;
    private final boolean selectable;
    private final double gateCost;

    public Node(String name, double x, double y, boolean selectable, double gateCost) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A node needs a name.");
        }
        this.name = name;
        this.x = x;
        this.y = y;
        this.selectable = selectable;
        this.gateCost = gateCost;
    }

    /** Straight-line distance to another node, in raw map units. */
    public double distanceTo(Node other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public String getName() { return name; }
    public double getX() { return x; }
    public double getY() { return y; }

    /** True for a real destination; false for a bare bend in a path. */
    public boolean isSelectable() { return selectable; }

    /** The equivalent distance charged for entering this node. 0 for most. */
    public double getGateCost() { return gateCost; }

    @Override
    public String toString() { return name; }
}
