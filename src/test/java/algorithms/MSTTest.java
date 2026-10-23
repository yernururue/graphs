package algorithms;

import graph.Edge;
import graph.Graph;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MSTTest {
    @Test
    void connectedGraphProducesMinimumTreeWithSameWeightFromBothAlgorithms() {
        Graph graph = new Graph(4, false);
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 2, 2);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 5);

        MST.Result kruskal = MST.kruskal(graph);
        MST.Result prim = MST.prim(graph);

        assertEquals(3, kruskal.edges().size());
        assertEquals(4, kruskal.totalWeight());
        assertEquals(1, kruskal.components());
        assertEquals(kruskal.totalWeight(), prim.totalWeight());
        assertEquals(1, prim.components());
    }

    @Test
    void emptyAndSingletonGraphsHaveNoEdges() {
        for (Graph graph : List.of(new Graph(0, false), new Graph(1, false))) {
            for (MST.Result result : List.of(MST.kruskal(graph), MST.prim(graph))) {
                assertTrue(result.edges().isEmpty());
                assertEquals(0, result.totalWeight());
                assertEquals(graph.vertexCount(), result.components());
            }
        }
    }

    @Test
    void disconnectedGraphReturnsMinimumSpanningForestIncludingIsolates() {
        Graph graph = new Graph(5, false);
        graph.addEdge(0, 1, -2);
        graph.addEdge(1, 2, 3);
        graph.addEdge(0, 2, 4);
        graph.addEdge(3, 4, 7);

        for (MST.Result result : List.of(MST.kruskal(graph), MST.prim(graph))) {
            assertEquals(3, result.edges().size());
            assertEquals(8, result.totalWeight());
            assertEquals(2, result.components());
        }
    }

    @Test
    void handlesLoopsParallelEdgesEqualWeightsAndLongTotal() {
        Graph graph = new Graph(3, false);
        graph.addEdge(0, 0, Integer.MIN_VALUE);
        graph.addEdge(0, 1, Integer.MAX_VALUE);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, Integer.MAX_VALUE);
        graph.addEdge(0, 2, Integer.MAX_VALUE);

        for (MST.Result result : List.of(MST.kruskal(graph), MST.prim(graph))) {
            assertEquals(2, result.edges().size());
            assertEquals(1L + Integer.MAX_VALUE, result.totalWeight());
            assertEquals(1, result.components());
        }
    }

    @Test
    void rejectsDirectedGraphsClearly() {
        Graph graph = new Graph(2, true);
        assertThrows(IllegalArgumentException.class, () -> MST.kruskal(graph));
        assertThrows(IllegalArgumentException.class, () -> MST.prim(graph));
    }

    @Test
    void selectedEdgesAreImmutable() {
        Graph graph = new Graph(2, false);
        graph.addEdge(0, 1, 1);
        List<Edge> edges = MST.kruskal(graph).edges();
        assertThrows(UnsupportedOperationException.class, () -> edges.clear());
    }
}
