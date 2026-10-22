package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Weighted graph with vertices numbered from 0 to n - 1. */
public class Graph {
    private final List<List<Edge>> adjacency;
    private final boolean directed;

    public Graph(int n, boolean directed) {
        if (n < 0) {
            throw new IllegalArgumentException("Vertex count must be non-negative");
        }
        this.directed = directed;
        adjacency = new ArrayList<>(n);
        for (int vertex = 0; vertex < n; vertex++) {
            adjacency.add(new ArrayList<>());
        }
    }

    /** Adds an edge, preserving parallel edges and insertion order. */
    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);
        adjacency.get(from).add(new Edge(from, to, weight));
        if (!directed) {
            // A self-loop also gets two entries in an undirected graph.
            adjacency.get(to).add(new Edge(to, from, weight));
        }
    }

    /** Returns a read-only view that reflects later additions to this vertex. */
    public List<Edge> neighbors(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }

    public boolean hasEdge(int from, int to) {
        checkVertex(from);
        checkVertex(to);
        for (Edge edge : adjacency.get(from)) {
            if (edge.to == to) {
                return true;
            }
        }
        return false;
    }

    public int vertexCount() {
        return adjacency.size();
    }

    public boolean isDirected() {
        return directed;
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount()) {
            throw new IndexOutOfBoundsException("Vertex out of range: " + vertex);
        }
    }
}
