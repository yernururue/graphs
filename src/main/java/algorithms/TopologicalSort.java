package algorithms;

import graph.Edge;
import graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/** Topological ordering of directed acyclic graphs. */
public final class TopologicalSort {
    private TopologicalSort() { }

    /** Immutable result of Kahn's algorithm. */
    public static final class Result {
        private final List<Integer> order;
        private final long edgeScans;

        private Result(List<Integer> order, long edgeScans) {
            this.order = List.copyOf(order);
            this.edgeScans = edgeScans;
        }

        public List<Integer> order() { return order; }
        public long edgeScans() { return edgeScans; }
    }

    /** Returns a topological order, or throws if the directed graph contains a cycle. */
    public static Result kahn(Graph graph) {
        requireDirected(graph);
        int n = graph.vertexCount();
        int[] indegree = new int[n];
        long edgeScans = 0;
        for (int from = 0; from < n; from++) {
            for (Edge edge : graph.neighbors(from)) {
                indegree[edge.to]++;
            }
        }

        Queue<Integer> ready = new ArrayDeque<>();
        for (int vertex = 0; vertex < n; vertex++) {
            if (indegree[vertex] == 0) ready.add(vertex);
        }

        List<Integer> order = new ArrayList<>(n);
        while (!ready.isEmpty()) {
            int vertex = ready.remove();
            order.add(vertex);
            for (Edge edge : graph.neighbors(vertex)) {
                indegree[edge.to]--;
                edgeScans++;
                if (indegree[edge.to] == 0) ready.add(edge.to);
            }
        }
        if (order.size() != n) throw new IllegalArgumentException("Graph contains a directed cycle");
        return new Result(order, edgeScans);
    }

    private static void requireDirected(Graph graph) {
        if (graph == null) throw new IllegalArgumentException("Graph must not be null");
        if (!graph.isDirected()) throw new IllegalArgumentException("Topological sort requires a directed graph");
    }
}
