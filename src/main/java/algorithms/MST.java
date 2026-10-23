package algorithms;

import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/** Minimum spanning tree algorithms that return a forest for disconnected graphs. */
public final class MST {
    private MST() { }

    /** Immutable result of a minimum spanning forest computation. */
    public static final class Result {
        private final List<Edge> edges;
        private final long totalWeight;
        private final int components;
        private final AlgorithmMetrics metrics;

        private Result(List<Edge> edges, long totalWeight, int components, AlgorithmMetrics metrics) {
            this.edges = List.copyOf(edges);
            this.totalWeight = totalWeight;
            this.components = components;
            this.metrics = metrics;
        }

        public List<Edge> edges() { return edges; }
        public long totalWeight() { return totalWeight; }
        public int components() { return components; }
        public AlgorithmMetrics metrics() { return metrics; }
    }

    /** Computes a minimum spanning forest by sorting logical edges and joining components. */
    public static Result kruskal(Graph graph) {
        requireUndirected(graph);
        List<Edge> edges = new ArrayList<>();
        for (int from = 0; from < graph.vertexCount(); from++) {
            for (Edge edge : graph.neighbors(from)) {
                if (edge.from < edge.to) {
                    edges.add(edge);
                }
            }
        }
        long[] comparisons = {0};
        edges.sort((left, right) -> {
            comparisons[0]++;
            return Integer.compare(left.weight, right.weight);
        });

        DSU dsu = new DSU(graph.vertexCount());
        List<Edge> selected = new ArrayList<>();
        long totalWeight = 0;
        long edgeScans = 0;
        for (Edge edge : edges) {
            edgeScans++;
            if (dsu.find(edge.from) != dsu.find(edge.to)) {
                selected.add(edge);
                totalWeight += edge.weight;
                dsu.union(edge.from, edge.to);
            }
        }
        return new Result(selected, totalWeight, dsu.components(),
                new AlgorithmMetrics(dsu.dsuOps(), edgeScans, comparisons[0], 0));
    }

    /** Computes a minimum spanning forest using Prim's priority-queue expansion. */
    public static Result prim(Graph graph) {
        requireUndirected(graph);
        int n = graph.vertexCount();
        boolean[] visited = new boolean[n];
        List<Edge> selected = new ArrayList<>();
        long[] comparisons = {0};
        PriorityQueue<Edge> candidates = new PriorityQueue<>((left, right) -> {
            comparisons[0]++;
            return Integer.compare(left.weight, right.weight);
        });
        long totalWeight = 0;
        int components = 0;
        long edgeScans = 0;

        for (int start = 0; start < n; start++) {
            if (visited[start]) continue;
            components++;
            visited[start] = true;
            candidates.addAll(graph.neighbors(start));
            while (!candidates.isEmpty()) {
                Edge edge = candidates.remove();
                edgeScans++;
                if (visited[edge.to]) continue;
                visited[edge.to] = true;
                selected.add(edge);
                totalWeight += edge.weight;
                candidates.addAll(graph.neighbors(edge.to));
            }
        }
        return new Result(selected, totalWeight, components,
                new AlgorithmMetrics(0, edgeScans, comparisons[0], 0));
    }

    private static void requireUndirected(Graph graph) {
        if (graph == null) throw new IllegalArgumentException("Graph must not be null");
        if (graph.isDirected()) throw new IllegalArgumentException("MST requires an undirected graph");
    }
}
