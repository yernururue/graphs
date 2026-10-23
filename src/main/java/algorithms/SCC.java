package algorithms;

import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;

public final class SCC {
    private SCC() { }

    public static final class Result {
        private final List<List<Integer>> components;
        private final long edgeScans;
        private final int maxDepth;
        private final AlgorithmMetrics metrics;

        private Result(List<List<Integer>> components, long edgeScans, int maxDepth) {
            List<List<Integer>> copy = new ArrayList<>(components.size());
            for (List<Integer> component : components) copy.add(List.copyOf(component));
            this.components = List.copyOf(copy);
            this.edgeScans = edgeScans;
            this.maxDepth = maxDepth;
            this.metrics = new AlgorithmMetrics(0, edgeScans, 0, maxDepth);
        }

        public List<List<Integer>> components() { return components; }
        public int count() { return components.size(); }
        public long edgeScans() { return edgeScans; }
        public int maxDepth() { return maxDepth; }
        public AlgorithmMetrics metrics() { return metrics; }
    }

    public static Result kosaraju(Graph graph) {
        requireDirected(graph);
        int n = graph.vertexCount();
        boolean[] visited = new boolean[n];
        int[] vertices = new int[n];
        int[] nextNeighbor = new int[n];
        List<Integer> finishOrder = new ArrayList<>(n);
        long edgeScans = 0;
        int maxDepth = 0;

        for (int start = 0; start < n; start++) {
            if (visited[start]) continue;
            int depth = 1;
            vertices[0] = start;
            nextNeighbor[0] = 0;
            visited[start] = true;
            maxDepth = Math.max(maxDepth, depth);

            while (depth > 0) {
                int top = depth - 1;
                List<Edge> neighbors = graph.neighbors(vertices[top]);
                if (nextNeighbor[top] < neighbors.size()) {
                    Edge edge = neighbors.get(nextNeighbor[top]++);
                    edgeScans++;
                    if (!visited[edge.to]) {
                        visited[edge.to] = true;
                        vertices[depth] = edge.to;
                        nextNeighbor[depth] = 0;
                        depth++;
                        maxDepth = Math.max(maxDepth, depth);
                    }
                } else {
                    finishOrder.add(vertices[top]);
                    depth--;
                }
            }
        }

        List<List<Integer>> reversed = new ArrayList<>(n);
        for (int vertex = 0; vertex < n; vertex++) reversed.add(new ArrayList<>());
        for (int from = 0; from < n; from++) {
            for (Edge edge : graph.neighbors(from)) {
                reversed.get(edge.to).add(from);
            }
        }

        visited = new boolean[n];
        List<List<Integer>> components = new ArrayList<>();
        int[] stack = new int[n];
        for (int i = finishOrder.size() - 1; i >= 0; i--) {
            int start = finishOrder.get(i);
            if (visited[start]) continue;
            List<Integer> component = new ArrayList<>();
            int size = 1;
            stack[0] = start;
            visited[start] = true;
            maxDepth = Math.max(maxDepth, size);
            while (size > 0) {
                int vertex = stack[--size];
                component.add(vertex);
                for (int neighbor : reversed.get(vertex)) {
                    edgeScans++;
                    if (!visited[neighbor]) {
                        visited[neighbor] = true;
                        stack[size++] = neighbor;
                        maxDepth = Math.max(maxDepth, size);
                    }
                }
            }
            components.add(component);
        }
        return new Result(components, edgeScans, maxDepth);
    }

    private static void requireDirected(Graph graph) {
        if (graph == null) throw new IllegalArgumentException("Graph must not be null");
        if (!graph.isDirected()) throw new IllegalArgumentException("SCC requires a directed graph");
    }
}
