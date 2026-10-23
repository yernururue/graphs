package benchmark;

import algorithms.AlgorithmMetrics;
import algorithms.DagShortestPath;
import algorithms.MST;
import algorithms.SCC;
import algorithms.TopologicalSort;
import graph.Edge;
import graph.Graph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public final class Benchmark {
    private static final int[] SIZES = {1_000, 3_162, 10_000, 31_623, 100_000};
    private static final int RUNS = 5;
    private static final String HEADER = "algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result";
    private static final long[] MST_REFERENCES = {147970,30363,468911,97589,1486139,309550,4715515,968517,15012094,3056681};
    private static final long[] SCC_REFERENCES = {27,1,137,1,373,1,1188,1,3923,1};
    private static final long[] DAG_REFERENCES = {396274,252411,1271911,723157,3941828,2381342,12551891,7376612,40096539,23512087};

    private Benchmark() { }

    public record Row(String algorithm, String graph, int n, int m, long timeMs,
                      long dsuOps, long edgeScans, long comparisons, int maxDepth, long result) {
        public AlgorithmMetrics metrics() {
            return new AlgorithmMetrics(dsuOps, edgeScans, comparisons, maxDepth);
        }
    }

    public static void main(String[] args) throws IOException {
        Path output = args.length == 0 ? Path.of("results", "results.csv") : Path.of(args[0]);
        List<Row> rows = run();
        writeCsv(output, rows);
    }

    public static List<Row> run() {
        List<Row> rows = new ArrayList<>(50);
        long[] mstReferences = new long[10];
        long[] sccReferences = new long[10];
        long[] dagReferences = new long[10];
        int referenceIndex = 0;
        int graphReferenceIndex = 0;

        for (int n : SIZES) {
            for (int multiplier : new int[]{4, 20}) {
                int m = Math.multiplyExact(n, multiplier);
                Graph undirected = GraphGenerator.undirected(n, m);
                Measured<MST.Result> kruskal = measure(MST::kruskal, undirected, MST.Result::totalWeight);
                Measured<MST.Result> prim = measure(MST::prim, undirected, MST.Result::totalWeight);
                require(kruskal.result.totalWeight() == prim.result.totalWeight(),
                        "Kruskal and Prim weights differ for n=" + n + ", m=" + m);
                mstReferences[referenceIndex++] = kruskal.result.totalWeight();
                rows.add(row("Kruskal", "undirected", n, m, kruskal));
                rows.add(row("Prim", "undirected", n, m, prim));

                Graph directed = GraphGenerator.directed(n, m);
                Measured<SCC.Result> scc = measure(SCC::kosaraju, directed, result -> result.count());
                sccReferences[graphReferenceIndex] = scc.result.count();
                require(scc.result.edgeScans() == 2L * m, "Kosaraju edge scan count mismatch");
                rows.add(row("Kosaraju", "directed", n, m, scc));

                Graph dag = GraphGenerator.dag(n, m);
                Measured<TopologicalSort.Result> topological = measure(TopologicalSort::kahn, dag,
                        result -> result.order().size());
                validateTopologicalOrder(dag, topological.result.order());
                require(topological.result.edgeScans() == m, "Kahn edge scan count mismatch");
                rows.add(row("Kahn", "dag", n, m, topological));

                Measured<DagShortestPath.Result> shortest = measure(DagShortestPath::shortestPaths, dag,
                        DagShortestPath.Result::totalDistance);
                require(shortest.result.edgeScans() == m, "DAG relaxation edge scan count mismatch");
                dagReferences[graphReferenceIndex] = shortest.result.totalDistance();
                rows.add(row("DAG shortest paths", "dag", n, m, shortest));
                graphReferenceIndex++;
            }
        }
        verifyReferences(mstReferences, sccReferences, dagReferences);
        return List.copyOf(rows);
    }

    public static void writeCsv(Path target, List<Row> rows) throws IOException {
        writeCsv(target, rows, (source, destination) -> Files.move(source, destination,
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING));
    }

    @FunctionalInterface
    interface AtomicMove {
        void move(Path source, Path target) throws IOException;
    }

    static void writeCsv(Path target, List<Row> rows, AtomicMove atomicMove) throws IOException {
        if (rows.size() != 50) throw new IllegalArgumentException("Expected exactly 50 benchmark rows");
        StringBuilder csv = new StringBuilder(HEADER).append('\n');
        for (Row row : rows) {
            csv.append(row.algorithm()).append(',').append(row.graph()).append(',')
                    .append(row.n()).append(',').append(row.m()).append(',').append(row.timeMs()).append(',')
                    .append(row.dsuOps()).append(',').append(row.edgeScans()).append(',')
                    .append(row.comparisons()).append(',').append(row.maxDepth()).append(',')
                    .append(row.result()).append('\n');
        }
        Path absolute = target.toAbsolutePath();
        Path parent = absolute.getParent();
        Files.createDirectories(parent);
        Path temporary = parent.resolve(absolute.getFileName() + "." + UUID.randomUUID() + ".tmp");
        try {
            Files.writeString(temporary, csv, StandardCharsets.UTF_8);
            atomicMove.move(temporary, absolute);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public static String csvHeader() { return HEADER; }

    private static <T> Measured<T> measure(Function<Graph, T> algorithm, Graph graph,
                                           ToLongFunction<T> resultValue) {
        long[] elapsedNanos = new long[RUNS];
        T first = null;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            T result = algorithm.apply(graph);
            elapsedNanos[i] = System.nanoTime() - start;
            if (i == 0) first = result;
            else require(resultValue.applyAsLong(result) == resultValue.applyAsLong(first),
                    "Algorithm result changed between timed runs");
        }
        Arrays.sort(elapsedNanos);
        long medianMillis = elapsedNanos[RUNS / 2] / 1_000_000L;
        return new Measured<>(first, medianMillis);
    }

    private static <T> Row row(String algorithm, String graph, int n, int m, Measured<T> measured) {
        AlgorithmMetrics metrics;
        long value;
        Object result = measured.result;
        if (result instanceof MST.Result mst) { metrics = mst.metrics(); value = mst.totalWeight(); }
        else if (result instanceof SCC.Result scc) { metrics = scc.metrics(); value = scc.count(); }
        else if (result instanceof TopologicalSort.Result topo) { metrics = topo.metrics(); value = topo.order().size(); }
        else if (result instanceof DagShortestPath.Result dag) { metrics = dag.metrics(); value = dag.totalDistance(); }
        else throw new IllegalArgumentException("Unsupported algorithm result: " + result.getClass());
        return new Row(algorithm, graph, n, m, measured.timeMs, metrics.dsuOps(), metrics.edgeScans(),
                metrics.comparisons(), metrics.maxDepth(), value);
    }

    private static void verifyReferences(long[] mst, long[] scc, long[] dag) {
        require(Arrays.equals(mst, MST_REFERENCES), "MST reference table mismatch: " + Arrays.toString(mst));
        require(Arrays.equals(scc, SCC_REFERENCES), "SCC reference table mismatch: " + Arrays.toString(scc));
        require(Arrays.equals(dag, DAG_REFERENCES), "DAG distance reference table mismatch: " + Arrays.toString(dag));
    }

    static void validateTopologicalOrder(Graph graph, List<Integer> order) {
        require(order.size() == graph.vertexCount(), "Topological order does not contain every vertex");
        int[] position = new int[graph.vertexCount()];
        boolean[] seen = new boolean[graph.vertexCount()];
        for (int i = 0; i < order.size(); i++) {
            int vertex = order.get(i);
            require(vertex >= 0 && vertex < graph.vertexCount(), "Topological order contains an out-of-range vertex");
            require(!seen[vertex], "Topological order contains a duplicate vertex");
            seen[vertex] = true;
            position[vertex] = i;
        }
        for (int from = 0; from < graph.vertexCount(); from++) {
            for (Edge edge : graph.neighbors(from)) {
                require(position[from] < position[edge.to], "Invalid topological order");
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private record Measured<T>(T result, long timeMs) { }
}
