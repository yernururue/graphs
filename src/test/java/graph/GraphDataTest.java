package graph;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphDataTest {
    @Test
    void directedAcyclicExampleHasOnlyForwardEdges() throws IOException {
        Graph graph = GraphReader.read(Path.of("data", "directed_acyclic.txt"));

        assertEquals(5, graph.vertexCount());
        assertTrue(graph.isDirected());
        assertNeighbors(graph, 0, new int[][]{{1, 3}, {2, 0}});
        assertNeighbors(graph, 1, new int[][]{{3, -2}});
        assertNeighbors(graph, 2, new int[][]{{3, 4}});
        assertNeighbors(graph, 3, new int[][]{{4, 1}});
        assertNeighbors(graph, 4, new int[][]{});
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            for (Edge edge : graph.neighbors(vertex)) {
                assertTrue(edge.from < edge.to);
            }
        }
    }

    @Test
    void directedCycleExamplePreservesCycleLoopAndParallelEdges() throws IOException {
        Graph graph = GraphReader.read(Path.of("data", "directed_cycle.txt"));

        assertEquals(3, graph.vertexCount());
        assertTrue(graph.isDirected());
        assertNeighbors(graph, 0, new int[][]{{1, 2}, {1, 5}});
        assertNeighbors(graph, 1, new int[][]{{2, -1}, {1, 7}});
        assertNeighbors(graph, 2, new int[][]{{0, 0}});
        assertTrue(graph.hasEdge(0, 1));
        assertTrue(graph.hasEdge(1, 2));
        assertTrue(graph.hasEdge(2, 0));
        assertTrue(graph.hasEdge(1, 1));
    }

    @Test
    void undirectedExampleHasSeparateComponentsAndAnIsolatedVertex() throws IOException {
        Graph graph = GraphReader.read(Path.of("data", "disconnected_undirected.txt"));

        assertEquals(6, graph.vertexCount());
        assertFalse(graph.isDirected());
        assertNeighbors(graph, 0, new int[][]{{1, 4}});
        assertNeighbors(graph, 1, new int[][]{{0, 4}, {2, -3}});
        assertNeighbors(graph, 2, new int[][]{{1, -3}});
        assertNeighbors(graph, 3, new int[][]{{4, 0}});
        assertNeighbors(graph, 4, new int[][]{{3, 0}});
        assertNeighbors(graph, 5, new int[][]{});
        assertFalse(graph.hasEdge(2, 3));
        assertFalse(graph.hasEdge(3, 2));
        assertTrue(graph.neighbors(5).isEmpty());
    }

    private static void assertNeighbors(Graph graph, int vertex, int[][] expected) {
        List<Edge> actual = graph.neighbors(vertex);
        assertEquals(expected.length, actual.size(), "Neighbor count for vertex " + vertex);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(vertex, actual.get(i).from);
            assertEquals(expected[i][0], actual.get(i).to);
            assertEquals(expected[i][1], actual.get(i).weight);
        }
    }
}
