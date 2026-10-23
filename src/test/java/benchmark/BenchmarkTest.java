package benchmark;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import graph.Graph;

import static org.junit.jupiter.api.Assertions.*;

class BenchmarkTest {
    @TempDir Path tempDir;

    @Test
    void writesExpectedHeaderAndExactlyFiftyRecords() throws IOException {
        Path output = tempDir.resolve("results.csv");
        Benchmark.writeCsv(output, sampleRows(50));
        List<String> lines = Files.readAllLines(output);
        assertEquals("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result", lines.get(0));
        assertEquals(51, lines.size());
        assertEquals(10, lines.get(1).split(",", -1).length);
    }

    @Test
    void rejectsBadRowCountBeforeReplacingExistingCsv() throws IOException {
        Path output = tempDir.resolve("results.csv");
        Files.writeString(output, "keep existing file\n");
        assertThrows(IllegalArgumentException.class, () -> Benchmark.writeCsv(output, sampleRows(49)));
        assertEquals("keep existing file\n", Files.readString(output));
    }

    @Test
    void rejectsDuplicateVertexInTopologicalOrder() {
        Graph graph = new Graph(3, true);
        assertThrows(IllegalStateException.class,
                () -> Benchmark.validateTopologicalOrder(graph, List.of(0, 0, 2)));
    }

    @Test
    void rejectsMissingVertexInTopologicalOrder() {
        Graph graph = new Graph(3, true);
        assertThrows(IllegalStateException.class,
                () -> Benchmark.validateTopologicalOrder(graph, List.of(0, 2)));
    }

    @Test
    void rejectsOutOfRangeVertexInTopologicalOrder() {
        Graph graph = new Graph(3, true);
        assertThrows(IllegalStateException.class,
                () -> Benchmark.validateTopologicalOrder(graph, List.of(0, 1, 3)));
    }

    @Test
    void rejectsOrderThatRunsAnEdgeBackward() {
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, 1);
        assertThrows(IllegalStateException.class,
                () -> Benchmark.validateTopologicalOrder(graph, List.of(1, 0, 2)));
    }

    @Test
    void atomicMoveFailurePropagatesAndPreservesExistingCsv() throws IOException {
        Path output = tempDir.resolve("results.csv");
        Files.writeString(output, "keep existing file\n");
        Benchmark.AtomicMove unsupportedMove = (source, target) -> {
            throw new java.nio.file.AtomicMoveNotSupportedException(source.toString(), target.toString(), "test");
        };
        assertThrows(java.nio.file.AtomicMoveNotSupportedException.class,
                () -> Benchmark.writeCsv(output, sampleRows(50), unsupportedMove));
        assertEquals("keep existing file\n", Files.readString(output));
    }

    private static List<Benchmark.Row> sampleRows(int count) {
        List<Benchmark.Row> rows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rows.add(new Benchmark.Row("Kruskal", "undirected", 10, 40, 1, 3, 40, 12, 0, 9));
        }
        return rows;
    }
}
