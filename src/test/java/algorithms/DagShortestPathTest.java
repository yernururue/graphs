package algorithms;

import graph.Edge;
import graph.Graph;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DagShortestPathTest {
    @Test
    void disconnectedDagUsesEverySourceAndRestoresPath() {
        Graph graph = new Graph(6, true);
        graph.addEdge(0, 2, 7);
        graph.addEdge(1, 2, -4);
        graph.addEdge(2, 3, 0);
        graph.addEdge(4, 5, -2);

        DagShortestPath.Result result = DagShortestPath.shortestPaths(graph);

        assertArrayEquals(new long[]{0, 0, -4, -4, 0, -2}, result.distances());
        assertArrayEquals(new int[]{-1, -1, 1, 2, -1, 4}, result.parents());
        assertEquals(-10, result.totalDistance());
        assertEquals(4, result.edgeScans());
        assertEquals(List.of(1, 2, 3), result.pathTo(3));
        assertThrows(UnsupportedOperationException.class, () -> result.pathTo(3).clear());
        long[] copy = result.distances();
        copy[3] = 100;
        assertEquals(-4, result.distances()[3]);
    }

    @Test
    void matchesIndependentBellmanFordOnSeededRandomNegativeWeightDags() {
        Random random = new Random(84021);
        for (int trial = 0; trial < 200; trial++) {
            int n = random.nextInt(11);
            Graph graph = new Graph(n, true);
            for (int from = 0; from < n; from++) {
                for (int to = from + 1; to < n; to++) {
                    if (random.nextInt(4) == 0) graph.addEdge(from, to, random.nextInt(31) - 15);
                }
            }

            long[] expected = bellmanFordFromAllSources(graph);
            DagShortestPath.Result result = DagShortestPath.shortestPaths(graph);

            assertArrayEquals(expected, result.distances(), "trial " + trial);
            assertEquals(sum(expected), result.totalDistance(), "trial " + trial);
            assertEquals(edgeCount(graph), result.edgeScans(), "trial " + trial);
        }
    }

    @Test
    void handlesEmptyGraphZeroWeightsAndRejectsInvalidInputs() {
        DagShortestPath.Result empty = DagShortestPath.shortestPaths(new Graph(0, true));
        assertArrayEquals(new long[0], empty.distances());
        assertArrayEquals(new int[0], empty.parents());
        assertEquals(0, empty.totalDistance());
        assertEquals(0, empty.edgeScans());
        assertThrows(IndexOutOfBoundsException.class, () -> empty.pathTo(0));

        Graph zero = new Graph(3, true);
        zero.addEdge(0, 1, 0);
        zero.addEdge(1, 2, 0);
        assertArrayEquals(new long[]{0, 0, 0}, DagShortestPath.shortestPaths(zero).distances());

        Graph cycle = new Graph(2, true);
        cycle.addEdge(0, 1, 1);
        cycle.addEdge(1, 0, 1);
        assertThrows(IllegalArgumentException.class, () -> DagShortestPath.shortestPaths(cycle));
        assertThrows(IllegalArgumentException.class, () -> DagShortestPath.shortestPaths(null));
        assertThrows(IllegalArgumentException.class, () -> DagShortestPath.shortestPaths(new Graph(2, false)));
        assertThrows(IndexOutOfBoundsException.class, () -> DagShortestPath.shortestPaths(zero).pathTo(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> DagShortestPath.shortestPaths(zero).pathTo(3));
    }

    private static long[] bellmanFordFromAllSources(Graph graph) {
        int n = graph.vertexCount();
        long[] distances = new long[n];
        boolean[] hasIncoming = new boolean[n];
        List<Edge> edges = new ArrayList<>();
        for (int from = 0; from < n; from++) {
            for (Edge edge : graph.neighbors(from)) {
                edges.add(edge);
                hasIncoming[edge.to] = true;
            }
        }
        for (int vertex = 0; vertex < n; vertex++) {
            if (hasIncoming[vertex]) distances[vertex] = Long.MAX_VALUE;
        }
        for (int pass = 1; pass < n; pass++) {
            boolean changed = false;
            for (Edge edge : edges) {
                if (distances[edge.from] != Long.MAX_VALUE
                        && distances[edge.from] + edge.weight < distances[edge.to]) {
                    distances[edge.to] = distances[edge.from] + edge.weight;
                    changed = true;
                }
            }
            if (!changed) break;
        }
        return distances;
    }

    private static long sum(long[] values) {
        long total = 0;
        for (long value : values) total += value;
        return total;
    }

    private static long edgeCount(Graph graph) {
        long count = 0;
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) count += graph.neighbors(vertex).size();
        return count;
    }
}
