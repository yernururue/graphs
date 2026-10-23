package algorithms;

import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DagShortestPath {
    private DagShortestPath() { }

    public static final class Result {
        private final long[] distances;
        private final int[] parents;
        private final long totalDistance;
        private final long edgeScans;
        private final long comparisons;

        private Result(long[] distances, int[] parents, long totalDistance, long edgeScans, long comparisons) {
            this.distances = distances.clone();
            this.parents = parents.clone();
            this.totalDistance = totalDistance;
            this.edgeScans = edgeScans;
            this.comparisons = comparisons;
        }

        public long[] distances() { return distances.clone(); }
        public int[] parents() { return parents.clone(); }
        public long totalDistance() { return totalDistance; }
        public long edgeScans() { return edgeScans; }
        public AlgorithmMetrics metrics() { return new AlgorithmMetrics(0, edgeScans, comparisons, 0); }

        public List<Integer> pathTo(int target) {
            if (target < 0 || target >= parents.length) {
                throw new IndexOutOfBoundsException("Vertex out of range: " + target);
            }
            List<Integer> path = new ArrayList<>();
            for (int vertex = target; vertex != -1; vertex = parents[vertex]) path.add(vertex);
            Collections.reverse(path);
            return List.copyOf(path);
        }
    }

    public static Result shortestPaths(Graph graph) {
        if (graph == null) throw new IllegalArgumentException("Graph must not be null");
        if (!graph.isDirected()) throw new IllegalArgumentException("DAG shortest paths require a directed graph");

        int n = graph.vertexCount();
        int[] indegree = new int[n];
        for (int from = 0; from < n; from++) {
            for (Edge edge : graph.neighbors(from)) indegree[edge.to]++;
        }

        List<Integer> order = TopologicalSort.kahn(graph).order();
        long[] distances = new long[n];
        int[] parents = new int[n];
        java.util.Arrays.fill(distances, Long.MAX_VALUE);
        java.util.Arrays.fill(parents, -1);
        for (int vertex = 0; vertex < n; vertex++) {
            if (indegree[vertex] == 0) distances[vertex] = 0;
        }

        long edgeScans = 0;
        long comparisons = 0;
        for (int from : order) {
            if (distances[from] == Long.MAX_VALUE) continue;
            for (Edge edge : graph.neighbors(from)) {
                edgeScans++;
                long candidate = distances[from] + edge.weight;
                comparisons++;
                if (candidate < distances[edge.to]) {
                    distances[edge.to] = candidate;
                    parents[edge.to] = from;
                }
            }
        }

        long totalDistance = 0;
        for (long distance : distances) totalDistance += distance;
        return new Result(distances, parents, totalDistance, edgeScans, comparisons);
    }
}
