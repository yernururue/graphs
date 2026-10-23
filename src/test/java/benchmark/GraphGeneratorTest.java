package benchmark;

import graph.Edge;
import graph.Graph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphGeneratorTest {
    @Test
    void seededGeneratorsReproduceEdgesAndProduceRequestedLogicalCounts() {
        int n = 20;
        int m = 4 * n;
        Graph undirected = GraphGenerator.undirected(n, m);
        Graph directed = GraphGenerator.directed(n, m);
        Graph dag = GraphGenerator.dag(n, m);

        assertEquals(m, logicalEdges(undirected, false));
        assertEquals(m, logicalEdges(directed, true));
        assertEquals(m, logicalEdges(dag, true));
        assertFalse(undirected.isDirected());
        assertTrue(directed.isDirected());
        assertTrue(dag.isDirected());
        assertSameEdges(undirected, GraphGenerator.undirected(n, m), false);
        assertSameEdges(directed, GraphGenerator.directed(n, m), true);
        assertSameEdges(dag, GraphGenerator.dag(n, m), true);
        assertNoSelfLoopsAndValidWeights(undirected);
        assertNoSelfLoopsAndValidWeights(directed);
        assertNoSelfLoopsAndValidWeights(dag);
    }

    @Test
    void dagEdgesAlwaysFollowAConsistentTopologicalRank() {
        Graph dag = GraphGenerator.dag(100, 800);
        // Recover the rank constraints and ensure they form an acyclic graph.
        assertDoesNotThrow(() -> algorithms.TopologicalSort.kahn(dag));
        for (int from = 0; from < dag.vertexCount(); from++) {
            for (Edge edge : dag.neighbors(from)) assertNotEquals(edge.from, edge.to);
        }
    }

    private static void assertNoSelfLoopsAndValidWeights(Graph graph) {
        for (int from = 0; from < graph.vertexCount(); from++) {
            for (Edge edge : graph.neighbors(from)) {
                assertNotEquals(edge.from, edge.to);
                assertTrue(edge.weight >= 1 && edge.weight <= 1000);
            }
        }
    }

    private static int logicalEdges(Graph graph, boolean directed) {
        int records = 0;
        for (int v = 0; v < graph.vertexCount(); v++) records += graph.neighbors(v).size();
        return directed ? records : records / 2;
    }

    private static void assertSameEdges(Graph a, Graph b, boolean directed) {
        for (int from = 0; from < a.vertexCount(); from++) {
            assertEquals(a.neighbors(from).size(), b.neighbors(from).size());
            for (int i = 0; i < a.neighbors(from).size(); i++) {
                Edge x = a.neighbors(from).get(i);
                Edge y = b.neighbors(from).get(i);
                assertEquals(x.from, y.from);
                assertEquals(x.to, y.to);
                assertEquals(x.weight, y.weight);
            }
        }
    }
}
