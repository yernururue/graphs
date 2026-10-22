package graph;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {
    @Test
    void directedEdgeHasNoReverseEdge() {
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, 7);

        assertEquals(3, graph.vertexCount());
        assertTrue(graph.isDirected());
        assertTrue(graph.hasEdge(0, 1));
        assertFalse(graph.hasEdge(1, 0));
        assertEdge(graph.neighbors(0).get(0), 0, 1, 7);
        assertTrue(graph.neighbors(1).isEmpty());
    }

    @Test
    void undirectedEdgeStoresReverseEndpointsAndSameWeight() {
        Graph graph = new Graph(3, false);
        graph.addEdge(0, 2, -4);

        assertFalse(graph.isDirected());
        assertTrue(graph.hasEdge(0, 2));
        assertTrue(graph.hasEdge(2, 0));
        assertEquals(1, graph.neighbors(0).size());
        assertEquals(1, graph.neighbors(2).size());
        assertEdge(graph.neighbors(0).get(0), 0, 2, -4);
        assertEdge(graph.neighbors(2).get(0), 2, 0, -4);
    }

    @Test
    void directedSelfLoopHasOneEntry() {
        Graph graph = new Graph(1, true);
        graph.addEdge(0, 0, 0);

        assertTrue(graph.hasEdge(0, 0));
        assertEquals(1, graph.neighbors(0).size());
        assertEdge(graph.neighbors(0).get(0), 0, 0, 0);
    }

    @Test
    void undirectedSelfLoopHasTwoEntries() {
        Graph graph = new Graph(1, false);
        graph.addEdge(0, 0, 9);

        assertTrue(graph.hasEdge(0, 0));
        assertEquals(2, graph.neighbors(0).size());
        assertEdge(graph.neighbors(0).get(0), 0, 0, 9);
        assertEdge(graph.neighbors(0).get(1), 0, 0, 9);
    }

    @Test
    void parallelEdgesPreserveOrderAndAllIntegerWeightsInBothModes() {
        for (boolean directed : new boolean[]{true, false}) {
            Graph graph = new Graph(3, directed);
            graph.addEdge(0, 1, Integer.MIN_VALUE);
            graph.addEdge(0, 2, 0);
            graph.addEdge(0, 1, Integer.MAX_VALUE);

            assertEquals(3, graph.neighbors(0).size());
            assertEdge(graph.neighbors(0).get(0), 0, 1, Integer.MIN_VALUE);
            assertEdge(graph.neighbors(0).get(1), 0, 2, 0);
            assertEdge(graph.neighbors(0).get(2), 0, 1, Integer.MAX_VALUE);
            if (directed) {
                assertTrue(graph.neighbors(1).isEmpty());
            } else {
                assertEquals(2, graph.neighbors(1).size());
                assertEdge(graph.neighbors(1).get(0), 1, 0, Integer.MIN_VALUE);
                assertEdge(graph.neighbors(1).get(1), 1, 0, Integer.MAX_VALUE);
            }
        }
    }

    @Test
    void emptyGraphsAreAllowedInBothModes() {
        for (boolean directed : new boolean[]{true, false}) {
            Graph graph = new Graph(0, directed);
            assertEquals(0, graph.vertexCount());
            assertEquals(directed, graph.isDirected());
            assertThrows(IndexOutOfBoundsException.class, () -> graph.neighbors(0));
            assertThrows(IndexOutOfBoundsException.class, () -> graph.hasEdge(0, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(0, 0, 1));
        }
    }

    @Test
    void disconnectedComponentsAndIsolatedVerticesRemainSeparate() {
        Graph graph = new Graph(5, false);
        graph.addEdge(0, 1, 2);
        graph.addEdge(2, 3, 3);

        assertFalse(graph.hasEdge(1, 2));
        assertFalse(graph.hasEdge(0, 4));
        assertTrue(graph.neighbors(4).isEmpty());
        assertEquals(5, graph.vertexCount());
    }

    @Test
    void negativeVertexCountIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Graph(-1, true));
        assertThrows(IllegalArgumentException.class, () -> new Graph(-1, false));
    }

    @Test
    void everyVertexArgumentIsValidatedBeforeAnyEdgeIsAdded() {
        for (boolean directed : new boolean[]{true, false}) {
            Graph graph = new Graph(2, directed);
            graph.addEdge(0, 1, 3);
            for (int invalid : new int[]{-1, 2}) {
                assertThrows(IndexOutOfBoundsException.class, () -> graph.neighbors(invalid));
                assertThrows(IndexOutOfBoundsException.class, () -> graph.hasEdge(invalid, 0));
                assertThrows(IndexOutOfBoundsException.class, () -> graph.hasEdge(0, invalid));
                assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(invalid, 0, 4));
                assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(0, invalid, 4));
            }
            assertEquals(1, graph.neighbors(0).size());
            assertEquals(directed ? 0 : 1, graph.neighbors(1).size());
            assertEdge(graph.neighbors(0).get(0), 0, 1, 3);
        }
    }

    @Test
    void neighborsIsAnUnmodifiableView() {
        Graph graph = new Graph(2, false);
        List<Edge> neighbors = graph.neighbors(0);
        assertThrows(UnsupportedOperationException.class,
                () -> neighbors.add(new Edge(0, 1, 99)));

        graph.addEdge(0, 1, 5);
        assertEquals(1, neighbors.size());
        assertThrows(UnsupportedOperationException.class, neighbors::clear);
        assertThrows(UnsupportedOperationException.class, () -> neighbors.remove(0));
        assertThrows(UnsupportedOperationException.class,
                () -> neighbors.set(0, new Edge(0, 0, 99)));
        assertTrue(graph.hasEdge(0, 1));
        assertTrue(graph.hasEdge(1, 0));
    }

    private static void assertEdge(Edge edge, int from, int to, int weight) {
        assertEquals(from, edge.from);
        assertEquals(to, edge.to);
        assertEquals(weight, edge.weight);
    }
}
