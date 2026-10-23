# Assignment 3: Graph Core and DSU

An educational Java implementation of algorithms for weighted graphs and the
disjoint-set union (DSU) data structure. The project includes graph storage and
file parsing, strongly connected components, topological sorting, minimum
spanning forests, and DSU.

## Build

JDK 17 or later and Maven are required. Run these commands from the project root:

```sh
mvn test
mvn package
mvn clean verify
```

The first command runs the JUnit 5 tests. The second also builds
`target/graph-core-1.0-SNAPSHOT.jar`. The third verifies the project from a clean
build. Sources are compiled for Java 17 compatibility.

## Project structure

- `src/main/java/graph/Edge.java` — one weighted edge.
- `src/main/java/graph/Graph.java` — a graph represented by adjacency lists.
- `src/main/java/graph/GraphReader.java` — reads a graph from a text file.
- `src/test/java/graph/` — tests for the API, error handling, and all sample files.
- `src/main/java/algorithms/DSU.java` — the disjoint-set union data structure.
- `src/test/java/algorithms/DSUTest.java` — DSU tests, including comparison with an independent model.
- `src/main/java/algorithms/SCC.java` — strongly connected components using Kosaraju's algorithm.
- `src/main/java/algorithms/TopologicalSort.java` — topological sorting using Kahn's algorithm.
- `src/test/java/algorithms/SCCAndTopologicalSortTest.java` — tests for SCC and DAG ordering.
- `data/` — three input files.

## File format

The first line contains exactly three tokens:

```text
n m directed
```

The mode must be either `directed` or `undirected` (case-sensitive). `n` is the
number of vertices and `m` is the number of logical edges; both are nonnegative
`int` values. Vertices are numbered from `0` to `n - 1`. Both `n = 0` and `m = 0`
are allowed. The next `m` lines have this form:

```text
from to weight
```

All three values are `int` values. A weight can be zero, negative, or any other
value from `-2147483648` to `2147483647`. Self-loops and parallel edges are
allowed. Spaces and tabs separate tokens. Blank lines in the header or among the
`m` edge lines are not allowed; blank or whitespace-only lines after the edges
are allowed. A final newline is optional. Any extra non-empty input is rejected.

For `undirected`, each line creates two records: `Edge(from, to, weight)` and
`Edge(to, from, weight)`. The adjacency lists contain `2m` records in total; a
self-loop is also stored twice. For `directed`, each line creates one record.

Examples:

| File | Contents |
| --- | --- |
| `data/directed_acyclic.txt` | A DAG with 5 vertices and 5 edges; every edge goes from a lower-numbered vertex to a higher-numbered one, and the weights include `0` and `-2`. |
| `data/directed_cycle.txt` | 3 vertices and 5 edges: cycle `0 → 1 → 2 → 0`, self-loop `1 → 1`, and two edges `0 → 1` with weights `2` and `5`. |
| `data/disconnected_undirected.txt` | 6 vertices and 3 logical edges: components `{0, 1, 2}`, `{3, 4}`, and isolated vertex `5`; the weights include `-3` and `0`. |

## Storage and API

`Edge` has the public fields `public final int from`, `to`, and `weight`. Its
constructor sets them once, so an edge's values cannot be changed after creation.

`Graph(int n, boolean directed)` creates a `List<List<Edge>>` with a separate
outgoing-edge list for every vertex, including isolated vertices. Insertion
order is preserved.

| Method | Purpose |
| --- | --- |
| `vertexCount()` | Returns the number of vertices. |
| `isDirected()` | Returns whether the graph is directed. |
| `addEdge(from, to, weight)` | Adds an edge and, for an undirected graph, its reverse record. |
| `neighbors(vertex)` | Returns a read-only list of outgoing edges. |
| `hasEdge(from, to)` | Checks whether at least one edge has these endpoints. |

`neighbors` returns a view of the list: later additions are visible, but elements
cannot be added, removed, or replaced through the view. A negative `n` causes
`IllegalArgumentException`; an invalid vertex index in graph methods causes
`IndexOutOfBoundsException`. `addEdge` validates both endpoints before changing
the lists.

Java example, run from the project root. It prints the vertex count `5`, whether
an edge exists (`true`), and the outgoing edges of vertex `0`:

```java
import graph.Edge;
import graph.Graph;
import graph.GraphReader;
import java.io.IOException;
import java.nio.file.Path;

public class GraphExample {
    public static void main(String[] args) throws IOException {
        Graph graph = GraphReader.read(Path.of("data", "directed_acyclic.txt"));
        System.out.println(graph.vertexCount());
        System.out.println(graph.hasEdge(0, 1));
        for (Edge edge : graph.neighbors(0)) {
            System.out.println(edge.from + " -> " + edge.to + ", weight = " + edge.weight);
        }
    }
}
```

`GraphReader.read(Path)` closes the file automatically. Invalid format, missing
lines, invalid vertices, or values outside the `int` range result in an
`IOException` that includes the line number. File access errors are also
propagated as I/O errors.

The graph constructor takes `O(n)` time, `addEdge` takes amortized `O(1)`, and
`hasEdge` takes `O(outdegree(from))`, which is the length of the source's outgoing
list. Space usage is `O(n + m)`; doubling the records in an undirected graph does
not change the asymptotic bound.

## DSU

`DSU(int n)` creates `n` separate sets for elements from `0` to `n - 1`. The
`parent` array stores parents, and `size` stores the sizes of the trees at their
roots. `union` attaches the smaller tree to the larger one. `find` iteratively
searches for the root, then makes a second pass to point each element on the
traversed path directly to the root: full path compression without recursion.

| Method | Purpose |
| --- | --- |
| `makeSet(n)` | Creates `n` separate sets again and resets both counters. |
| `find(x)` | Returns the representative of the set containing `x` and compresses the path. |
| `union(x, y)` | Returns `true` when two different sets are merged; otherwise returns `false`. |
| `components()` | Returns the current number of sets. |
| `findCalls()` | Returns the number of valid `find` calls, including calls made inside `union`. |
| `successfulUnions()` | Returns the number of actual merges of different sets. |
| `dsuOps()` | Returns the sum of `findCalls()` and `successfulUnions()`. |

Every valid `union` calls `find` twice, including repeated unions and
`union(x, x)`. Only an actual merge increments `successfulUnions` and decreases
`components`. Reading the counters or the number of components does not change
them. `makeSet` accepts `0` and can change the size. A negative size causes
`IllegalArgumentException`; an invalid element causes `IndexOutOfBoundsException`.
Both arguments to `union` are validated before searching: invalid operations do
not change the state or counters, and a negative size does not reset the existing
DSU.

This example prints `true`, `false`, `3`, `4`, `1`, and `5`:

```java
import algorithms.DSU;

public class DSUExample {
    public static void main(String[] args) {
        DSU dsu = new DSU(4);
        System.out.println(dsu.union(0, 1));
        System.out.println(dsu.union(1, 0));
        System.out.println(dsu.components());
        System.out.println(dsu.findCalls());
        System.out.println(dsu.successfulUnions());
        System.out.println(dsu.dsuOps());
    }
}
```

Initialization and `makeSet` take `O(n)` time; DSU uses `O(n)` space. With
union by size and path compression, `find` and `union` take amortized
`O(α(n))`, where `α` is the inverse Ackermann function. Tests compare random
unions (`Random(42)`) against an independent label-based model, check counters
and path compression, and also exercise 100000 elements.

## Minimum spanning tree

`algorithms.MST.kruskal(graph)` and `algorithms.MST.prim(graph)` build a minimum
spanning forest for an undirected graph. For a connected graph, the result is a
tree; for a disconnected graph, it is a forest with one tree per component,
including isolated vertices. A directed graph causes `IllegalArgumentException`.

Both methods return `MST.Result`: `edges()` is an immutable list of selected
edges, `totalWeight()` is the total weight of type `long`, and `components()` is
the number of components in the result. `metrics()` returns a shared
`AlgorithmMetrics` record with the fields `dsuOps`, `edgeScans`, `comparisons`,
and `maxDepth`. For Kruskal, `edgeScans` is the number of logical edges processed
by the loop, including edges rejected because they form a cycle; `comparisons` is
the number of sorting comparator calls, `dsuOps` is the DSU counter, and
`maxDepth` is `0`. For Prim, `edgeScans` counts candidate removals from the
priority queue, including stale entries for already visited vertices;
`comparisons` is the number of queue comparator calls, and `dsuOps` and `maxDepth`
are `0`. Kruskal sorts edges in `O(m log m)` time and uses DSU in amortized
`O(m α(n))` time; Prim uses a priority queue in `O((n + m) log(n + m))` time. Both
algorithms use `O(n + m)` space.

## Strongly connected components and topological sorting

`algorithms.SCC.kosaraju(graph)` applies Kosaraju's algorithm to a directed
graph. Both DFS passes are iterative, so long paths do not require deep
recursion. `SCC.Result.components()` returns an immutable list of components,
`count()` returns their number, `edgeScans()` returns the number of edge records
examined in both DFS passes, and `maxDepth()` returns the maximum size of the
explicit traversal stack. `metrics()` stores these counters in the shared record:
`edgeScans` covers both DFS passes (exactly `2m`), `maxDepth` is the maximum
explicit stack size, and the other fields are `0`. Building the reverse graph is
not included in `edgeScans`. Time and space are `O(n + m)`.

`algorithms.TopologicalSort.kahn(graph)` returns a topological ordering of a DAG
in `TopologicalSort.Result.order()`. Parallel edges are counted separately. If
the graph contains a cycle, including a self-loop, the method throws
`IllegalArgumentException`. `edgeScans()` counts outgoing edges examined while
processing the queue (exactly `m` for a DAG); the initial pass that counts
indegrees is not included. `metrics()` is also available; for Kahn,
`comparisons`, `dsuOps`, and `maxDepth` are `0`. Both algorithms reject null and
undirected graphs with `IllegalArgumentException`.

`algorithms.DagShortestPath.shortestPaths(graph)` computes shortest paths in a
DAG in `O(V + E)`: after topological sorting, each vertex's outgoing edges are
relaxed once. Every vertex with indegree zero serves as a source with distance
`0`, which is equivalent to a virtual super-source and allows disconnected
graphs to be processed. Negative weights are allowed. `DagShortestPath.Result`
returns defensively copied `distances()` and `parents()` arrays, the sum of
distances `totalDistance()`, the number of relaxations `edgeScans()` (exactly
`m`), and an immutable reconstructed path `pathTo(target)`.
`metrics().comparisons()` counts distance-candidate checks for every relaxed
edge; building indegrees and calling the topological sort are not added to
`edgeScans` or `comparisons`. `dsuOps` and `maxDepth` are `0`.
Distances and their sum have type `long`; null, undirected, and cyclic graphs are
rejected with `IllegalArgumentException`.

## Benchmark and counters

`algorithms.AlgorithmMetrics` is the shared immutable counter type for all five
result types. `comparisons` means actual weight-comparator calls for Kruskal and
Prim, and actual distance-candidate comparisons for DAG shortest paths; vertices
ready for Kahn's algorithm are not compared. `maxDepth` is nonzero only for
Kosaraju, which uses an explicit stack.

`benchmark.Benchmark` builds ten seeded graphs for each family, checks MST
weights, Kahn orderings, exact reference values, and edge-scan counts, and only
then writes `results/results.csv`. The table contains 50 rows. The file is
replaced by an atomic move of a temporary file; if the file system does not
support `ATOMIC_MOVE`, the error is propagated and the previous CSV remains in
place.
