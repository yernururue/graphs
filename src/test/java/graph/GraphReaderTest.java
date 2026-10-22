package graph;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GraphReaderTest {
    @TempDir
    Path directory;

    @Test
    void readsDirectedEdgesAndAllIntegerWeights() throws IOException {
        Graph graph = read("4 4 directed\n0 1 0\n0 2 -7\n1 2 -2147483648\n2 0 2147483647\n");

        assertTrue(graph.isDirected());
        assertEquals(4, graph.vertexCount());
        assertEquals(2, graph.neighbors(0).size());
        assertEdge(graph.neighbors(0).get(0), 0, 1, 0);
        assertEdge(graph.neighbors(0).get(1), 0, 2, -7);
        assertEdge(graph.neighbors(1).get(0), 1, 2, Integer.MIN_VALUE);
        assertEdge(graph.neighbors(2).get(0), 2, 0, Integer.MAX_VALUE);
        assertFalse(graph.hasEdge(1, 0));
        assertTrue(graph.neighbors(3).isEmpty());
    }

    @Test
    void undirectedCountIncludesLogicalParallelEdgesAndSelfLoops() throws IOException {
        Graph graph = read("3 3 undirected\n0 1 -5\n0 1 8\n1 1 0\n");

        assertFalse(graph.isDirected());
        assertEquals(2, graph.neighbors(0).size());
        assertEquals(4, graph.neighbors(1).size());
        assertEdge(graph.neighbors(0).get(0), 0, 1, -5);
        assertEdge(graph.neighbors(0).get(1), 0, 1, 8);
        assertEdge(graph.neighbors(1).get(0), 1, 0, -5);
        assertEdge(graph.neighbors(1).get(1), 1, 0, 8);
        assertEdge(graph.neighbors(1).get(2), 1, 1, 0);
        assertEdge(graph.neighbors(1).get(3), 1, 1, 0);
        assertTrue(graph.neighbors(2).isEmpty());
    }

    @Test
    void readsZeroVerticesAndGraphsWithOnlyIsolatedVertices() throws IOException {
        Graph empty = read("0 0 directed");
        assertEquals(0, empty.vertexCount());
        assertTrue(empty.isDirected());

        Graph isolated = read("3 0 undirected\n");
        assertEquals(3, isolated.vertexCount());
        assertFalse(isolated.isDirected());
        for (int vertex = 0; vertex < 3; vertex++) {
            assertTrue(isolated.neighbors(vertex).isEmpty());
        }
    }

    @Test
    void acceptsWhitespaceTrailingBlankLinesAndNoFinalNewline() throws IOException {
        Graph graph = read("  2\t1  directed  \n 0\t1 -2");
        assertEdge(graph.neighbors(0).get(0), 0, 1, -2);

        Graph trailing = read("2 1 undirected\n0 1 6\n\n \t\n");
        assertEdge(trailing.neighbors(1).get(0), 1, 0, 6);
    }

    @Test
    void rejectsMissingOrMalformedHeaders() throws IOException {
        for (String input : new String[]{"", "\n", "2 0", "2 0 directed extra",
                "-1 0 directed", "2 -1 directed", "2 0 Directed", "2 0 unknown",
                "two 0 directed", "2 none directed", "2147483648 0 directed",
                "2 2147483648 directed", "-2147483649 0 directed"}) {
            assertInvalid(input, 1);
        }
    }

    @Test
    void rejectsMissingOrBlankEdgeLines() throws IOException {
        assertInvalid("2 1 directed\n", 2);
        assertInvalid("2 2 directed\n0 1 5\n", 3);
        assertInvalid("2 1 directed\n\n0 1 5", 2);
        assertInvalid("2 1 directed\n \t\n", 2);
    }

    @Test
    void rejectsMalformedEdgeTokensAndIntegerOverflow() throws IOException {
        for (String edge : new String[]{"0 1", "0 1 5 extra", "zero 1 5", "0 one 5",
                "0 1 weight", "2147483648 1 5", "0 -2147483649 5",
                "0 1 2147483648", "0 1 -2147483649"}) {
            assertInvalid("2 1 directed\n" + edge, 2);
        }
    }

    @Test
    void rejectsBothOutOfRangeEndpointsInBothModes() throws IOException {
        for (String mode : new String[]{"directed", "undirected"}) {
            for (String edge : new String[]{"-1 0 5", "2 0 5", "0 -1 5", "0 2 5"}) {
                assertInvalid("2 1 " + mode + "\n" + edge, 2);
            }
        }
        assertInvalid("0 1 directed\n0 0 5", 2);
    }

    @Test
    void rejectsExtraNonblankInputEvenAfterBlankLines() throws IOException {
        assertInvalid("2 0 directed\n0 1 5", 2);
        assertInvalid("2 1 directed\n0 1 5\n\nextra", 4);
    }

    @Test
    void preservesActualFileIoErrors() {
        assertThrows(NoSuchFileException.class,
                () -> GraphReader.read(directory.resolve("missing.txt")));
    }

    private Graph read(String input) throws IOException {
        Path path = directory.resolve("graph.txt");
        Files.writeString(path, input);
        return GraphReader.read(path);
    }

    private void assertInvalid(String input, int lineNumber) throws IOException {
        IOException error = assertThrows(IOException.class, () -> read(input), input);
        assertTrue(error.getMessage().contains("Line " + lineNumber + ":"), error.getMessage());
    }

    private static void assertEdge(Edge edge, int from, int to, int weight) {
        assertEquals(from, edge.from);
        assertEquals(to, edge.to);
        assertEquals(weight, edge.weight);
    }
}
