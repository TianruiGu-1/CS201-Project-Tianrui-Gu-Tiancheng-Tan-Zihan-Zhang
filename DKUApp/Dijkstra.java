import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BACK-END. Dijkstra's shortest-path algorithm.
 *
 * Answers: what is the cheapest route between two places?
 *
 * "Cheapest" is measured in the road weights CampusGraph hands out, which are
 * walking distance plus the equivalent distance of any gate passed through. The
 * algorithm neither knows nor cares that gates exist -- by the time it runs, the
 * charges are already in the weights. That is the point of folding them in at
 * build time: this file stays a plain shortest-path search.
 *
 * Repeatedly settles the unsettled node with the smallest known distance, then
 * relaxes its neighbours -- each road leaving it is offered as a way to reach
 * the far end. Settling in that order is what makes the answer correct: by the
 * time a node is settled, no detour through anything still unsettled can be
 * cheaper, because roads only ever add to a distance. That last part needs
 * weights that are never negative, which is why this algorithm must not be
 * pointed at a graph with negative roads.
 *
 * A campus has few enough places that scanning for the smallest beats keeping a
 * heap; with thousands a priority queue would be the better trade.
 */
public class Dijkstra {

    /** The cheapest route from one place to another, or empty if none exists. */
    public static List<Node> shortestPath(CampusGraph graph, String from, String to) {
        Node start = graph.require(from);
        Node goal = graph.require(to);

        Map<String, Double> best = new HashMap<>();
        Map<String, String> cameFrom = new HashMap<>();
        Set<String> settled = new HashSet<>();

        for (Node node : graph.getNodes()) {
            best.put(node.getName(), Double.POSITIVE_INFINITY);
        }
        best.put(start.getName(), 0.0);
        cameFrom.put(start.getName(), null);

        while (true) {
            String current = null;
            double currentBest = Double.POSITIVE_INFINITY;
            for (Map.Entry<String, Double> entry : best.entrySet()) {
                if (!settled.contains(entry.getKey()) && entry.getValue() < currentBest) {
                    currentBest = entry.getValue();
                    current = entry.getKey();
                }
            }
            if (current == null) {
                break;                          // nothing reachable is left
            }
            if (current.equals(goal.getName())) {
                return rebuild(graph, cameFrom, goal);
            }
            settled.add(current);

            for (CampusGraph.Road road : graph.roadsFrom(current)) {
                String next = road.to.getName();
                if (settled.contains(next)) {
                    continue;
                }
                double candidate = currentBest + road.weight;
                if (candidate < best.get(next)) {
                    best.put(next, candidate);
                    cameFrom.put(next, current);
                }
            }
        }
        return Collections.emptyList();
    }

    /** Walk the cameFrom chain backwards to turn it into a route. */
    private static List<Node> rebuild(CampusGraph graph,
                                      Map<String, String> cameFrom, Node goal) {
        if (!cameFrom.containsKey(goal.getName())) {
            return Collections.emptyList();
        }
        LinkedList<Node> route = new LinkedList<>();
        String name = goal.getName();
        while (name != null) {
            route.addFirst(graph.require(name));
            name = cameFrom.get(name);
        }
        return route;
    }
}
