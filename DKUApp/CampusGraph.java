import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * BACK-END. The campus road network, held as an adjacency list.
 *
 * A graph is the right structure for this problem: places are nodes and the
 * paths between them are edges.
 *
 * This class is only the data structure and the queries over it. The
 * route-finding algorithm lives in its own file, Dijkstra.java, so each can be
 * read on its own.
 *
 * THE WEIGHT OF A ROAD. Walking from u to v costs the distance between them
 * plus an extra equivalent distance if v is a gate -- a turnstile you have to
 * pass through costs you time, and that is modelled as extra distance rather
 * than as a separate concept. Only the node being entered is charged, so the
 * two directions of the same road usually differ, and the graph is directed
 * even though the campus is not. That asymmetry is folded in HERE, at build
 * time, which is what lets the algorithm stay a textbook shortest-path search.
 */
public class CampusGraph {

    /**
     * One road leaving a node: where it goes, and what the step costs. The
     * cost already includes the gate charge for arriving at `to`.
     *
     * Public so the algorithm class can walk the network without reaching into
     * this one.
     */
    public static class Road {
        public final Node to;
        public final double weight;

        Road(Node to, double weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    /** name -> node, so a click can find one without scanning. */
    private final Map<String, Node> byName = new HashMap<>();

    /** name -> roads leaving that node. This is the adjacency list. */
    private final Map<String, List<Road>> adjacency = new HashMap<>();

    public void addNode(Node n) {
        if (byName.containsKey(n.getName())) {
            throw new IllegalArgumentException("Duplicate node: " + n.getName());
        }
        byName.put(n.getName(), n);
        adjacency.put(n.getName(), new ArrayList<>());
    }

    /**
     * Join two places with a road.
     *
     * The walking distance is worked out from their positions rather than typed
     * in, so the map can never disagree with itself about how far apart two
     * places are. On top of that, arriving at a place costs its gate charge --
     * so the two directions are added separately and usually differ. Leaving u
     * is free: you only pay to go through a turnstile, not to walk away from
     * one.
     */
    public void connect(String a, String b) {
        Node from = require(a);
        Node to = require(b);
        double gap = from.distanceTo(to);
        adjacency.get(a).add(new Road(to, gap + to.getGateCost()));
        adjacency.get(b).add(new Road(from, gap + from.getGateCost()));
    }

    /** The node with this name. Throws if there is no such node. */
    public Node require(String name) {
        Node n = byName.get(name);
        if (n == null) {
            throw new IllegalArgumentException("No such node: " + name);
        }
        return n;
    }

    public Node get(String name) { return byName.get(name); }

    /** The roads leaving a node: where each one goes, and what the step costs. */
    public List<Road> roadsFrom(String name) {
        List<Road> roads = adjacency.get(name);
        return roads == null ? Collections.emptyList() : Collections.unmodifiableList(roads);
    }

    public List<Node> getNodes() { return new ArrayList<>(byName.values()); }

    /** Every road once, as its two endpoints, so the view can draw it. */
    public List<Node[]> getRoadSegments() {
        List<Node[]> segments = new ArrayList<>();
        Set<String> drawn = new HashSet<>();
        for (Map.Entry<String, List<Road>> entry : adjacency.entrySet()) {
            for (Road road : entry.getValue()) {
                String a = entry.getKey();
                String b = road.to.getName();
                String key = a.compareTo(b) < 0 ? a + "\u0000" + b : b + "\u0000" + a;
                if (drawn.add(key)) {
                    segments.add(new Node[] { byName.get(a), road.to });
                }
            }
        }
        return segments;
    }

    /**
     * What a route costs: the walk, plus the equivalent distance of every gate
     * it passes through.
     *
     * The road weights already carry the gate charges for every node entered,
     * which is one gate too many -- the destination is where the route ends, not
     * a place it passes through, so it is exempt. Subtracting that last charge
     * is the whole of the correction. The origin never appears in the sum at
     * all, because nothing is ever walked *into* it.
     */
    public double routeLength(List<Node> route) {
        if (route.size() < 2) {
            return 0;
        }
        double total = 0;
        for (int i = 0; i + 1 < route.size(); i++) {
            total += roadWeight(route.get(i), route.get(i + 1));
        }
        return total - route.get(route.size() - 1).getGateCost();
    }

    private double roadWeight(Node a, Node b) {
        for (Road road : adjacency.get(a.getName())) {
            if (road.to.getName().equals(b.getName())) {
                return road.weight;
            }
        }
        throw new IllegalArgumentException("No road between " + a + " and " + b);
    }

    /**
     * True when every place can be reached from every other. Used to check the
     * map data before trusting it -- with a queue, to flood outwards and count
     * what was reached.
     */
    public boolean isConnected() {
        if (byName.isEmpty()) {
            return true;
        }
        String start = byName.keySet().iterator().next();
        Set<String> seen = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        seen.add(start);
        queue.add(byName.get(start));

        while (!queue.isEmpty()) {
            for (Road road : adjacency.get(queue.remove().getName())) {
                if (seen.add(road.to.getName())) {
                    queue.add(road.to);
                }
            }
        }
        return seen.size() == byName.size();
    }

    public int size() { return byName.size(); }
}
