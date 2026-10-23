package algorithms;

import graph.Graph;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SCCAndTopologicalSortTest {
    @Test
    void kosarajuMatchesBruteForceReachabilityOnSeededGraphs() {
        Random random = new Random(7319);
        for (int n = 0; n <= 200; n++) {
            Graph graph = new Graph(n, true);
            int edgeCount = n == 0 ? 0 : random.nextInt(n * 3 + 1);
            for (int i = 0; i < edgeCount; i++) {
                graph.addEdge(random.nextInt(n), random.nextInt(n), i);
            }

            List<List<Integer>> components = SCC.kosaraju(graph).components();
            assertEquals(n, components.stream().mapToInt(List::size).sum(), "n=" + n);
            int[] componentOf = new int[n];
            for (int component = 0; component < components.size(); component++) {
                for (int vertex : components.get(component)) componentOf[vertex] = component;
            }
            boolean[][] reachable = transitiveClosure(graph);
            for (int from = 0; from < n; from++) {
                for (int to = 0; to < n; to++) {
                    assertEquals(reachable[from][to] && reachable[to][from],
                            componentOf[from] == componentOf[to], "n=" + n + ", " + from + " -> " + to);
                }
            }
        }
    }

    @Test
    void kosarajuHandlesHundredThousandVertexPathWithoutRecursion() {
        int n = 100_000;
        Graph path = new Graph(n, true);
        for (int vertex = 0; vertex + 1 < n; vertex++) path.addEdge(vertex, vertex + 1, 1);

        SCC.Result result = SCC.kosaraju(path);

        assertEquals(n, result.count());
        assertEquals(n, result.components().size());
        assertTrue(result.maxDepth() > 0);
        assertEquals(2L * (n - 1), result.edgeScans());
    }

    @Test
    void kosarajuComponentsAreImmutableAtBothListLevels() {
        Graph graph = new Graph(2, true);
        SCC.Result result = SCC.kosaraju(graph);

        assertThrows(UnsupportedOperationException.class, () -> result.components().clear());
        assertThrows(UnsupportedOperationException.class, () -> result.components().get(0).clear());
    }

    @Test
    void kahnReturnsAnImmutableValidOrderAndCountsParallelArcs() {
        Graph dag = new Graph(5, true);
        dag.addEdge(0, 1, 1);
        dag.addEdge(0, 1, 2);
        dag.addEdge(0, 2, 3);
        dag.addEdge(1, 3, 4);
        dag.addEdge(2, 3, 5);
        dag.addEdge(3, 4, 6);

        TopologicalSort.Result result = TopologicalSort.kahn(dag);

        assertEquals(5, result.order().size());
        assertEveryEdgePointsForward(dag, result.order());
        assertEquals(6, result.edgeScans());
        assertThrows(UnsupportedOperationException.class, () -> result.order().clear());
    }

    @Test
    void kahnReturnsEmptyOrderForEmptyGraph() {
        TopologicalSort.Result result = TopologicalSort.kahn(new Graph(0, true));
        assertTrue(result.order().isEmpty());
        assertEquals(0, result.edgeScans());
    }

    @Test
    void kahnRejectsCyclesIncludingSelfLoops() {
        Graph cycle = new Graph(3, true);
        cycle.addEdge(0, 1, 1);
        cycle.addEdge(1, 2, 1);
        cycle.addEdge(2, 0, 1);
        Graph selfLoop = new Graph(1, true);
        selfLoop.addEdge(0, 0, 1);

        assertThrows(IllegalArgumentException.class, () -> TopologicalSort.kahn(cycle));
        assertThrows(IllegalArgumentException.class, () -> TopologicalSort.kahn(selfLoop));
    }

    @Test
    void bothAlgorithmsRejectNullAndUndirectedGraphs() {
        Graph undirected = new Graph(2, false);
        assertThrows(IllegalArgumentException.class, () -> SCC.kosaraju(null));
        assertThrows(IllegalArgumentException.class, () -> SCC.kosaraju(undirected));
        assertThrows(IllegalArgumentException.class, () -> TopologicalSort.kahn(null));
        assertThrows(IllegalArgumentException.class, () -> TopologicalSort.kahn(undirected));
    }

    private static void assertEveryEdgePointsForward(Graph graph, List<Integer> order) {
        int[] position = new int[graph.vertexCount()];
        for (int i = 0; i < order.size(); i++) position[order.get(i)] = i;
        for (int from = 0; from < graph.vertexCount(); from++) {
            for (graph.Edge edge : graph.neighbors(from)) {
                assertTrue(position[edge.from] < position[edge.to], edge.from + " -> " + edge.to);
            }
        }
    }

    private static boolean[][] transitiveClosure(Graph graph) {
        int n = graph.vertexCount();
        boolean[][] reachable = new boolean[n][n];
        for (int start = 0; start < n; start++) {
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            reachable[start][start] = true;
            queue.add(start);
            while (!queue.isEmpty()) {
                int vertex = queue.remove();
                for (graph.Edge edge : graph.neighbors(vertex)) {
                    if (!reachable[start][edge.to]) {
                        reachable[start][edge.to] = true;
                        queue.add(edge.to);
                    }
                }
            }
        }
        return reachable;
    }
}
