package algorithms;

import graph.Graph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmMetricsTest {
    @Test
    void algorithmResultsExposeSharedMetricsWithoutRemovingLegacyCounters() {
        Graph undirected = new Graph(4, false);
        undirected.addEdge(0, 1, 3);
        undirected.addEdge(1, 2, 1);
        undirected.addEdge(2, 3, 2);
        undirected.addEdge(0, 3, 7);
        MST.Result kruskal = MST.kruskal(undirected);
        MST.Result prim = MST.prim(undirected);
        assertEquals(kruskal.totalWeight(), prim.totalWeight());
        assertEquals(4, kruskal.metrics().edgeScans());
        assertTrue(kruskal.metrics().dsuOps() > 0);
        assertTrue(kruskal.metrics().comparisons() > 0);
        assertEquals(8, prim.metrics().edgeScans());
        assertTrue(prim.metrics().comparisons() > 0);

        Graph directed = new Graph(3, true);
        directed.addEdge(0, 1, 4);
        directed.addEdge(0, 2, 8);
        directed.addEdge(1, 2, 1);
        SCC.Result scc = SCC.kosaraju(directed);
        TopologicalSort.Result topo = TopologicalSort.kahn(directed);
        DagShortestPath.Result shortest = DagShortestPath.shortestPaths(directed);
        assertEquals(6, scc.metrics().edgeScans());
        assertEquals(scc.edgeScans(), scc.metrics().edgeScans());
        assertEquals(3, topo.metrics().edgeScans());
        assertEquals(topo.edgeScans(), topo.metrics().edgeScans());
        assertEquals(3, shortest.metrics().edgeScans());
        assertEquals(shortest.edgeScans(), shortest.metrics().edgeScans());
        assertEquals(3, shortest.metrics().comparisons());
    }
}
