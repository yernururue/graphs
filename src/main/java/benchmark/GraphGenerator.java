package benchmark;

import graph.Graph;

import java.util.Random;

public final class GraphGenerator {
    private static final long SEED = 42L;

    private GraphGenerator() { }

    public static Graph undirected(int vertexCount, int edgeCount) {
        validate(vertexCount, edgeCount, true);
        Random random = new Random(SEED);
        int[] permutation = permutation(vertexCount, random);
        Graph graph = new Graph(vertexCount, false);
        for (int i = 1; i < vertexCount; i++) {
            int from = permutation[random.nextInt(i)];
            int to = permutation[i];
            graph.addEdge(from, to, weight(random));
        }
        for (int i = vertexCount - 1; i < edgeCount; i++) {
            int from = random.nextInt(vertexCount);
            int to = distinctEndpoint(random, vertexCount, from);
            graph.addEdge(from, to, weight(random));
        }
        return graph;
    }

    public static Graph directed(int vertexCount, int edgeCount) {
        validate(vertexCount, edgeCount, false);
        Random random = new Random(SEED);
        Graph graph = new Graph(vertexCount, true);
        for (int i = 0; i < edgeCount; i++) {
            int from = random.nextInt(vertexCount);
            int to = distinctEndpoint(random, vertexCount, from);
            graph.addEdge(from, to, weight(random));
        }
        return graph;
    }

    public static Graph dag(int vertexCount, int edgeCount) {
        validate(vertexCount, edgeCount, false);
        Random random = new Random(SEED);
        int[] rank = permutation(vertexCount, random);
        Graph graph = new Graph(vertexCount, true);
        for (int i = 0; i < edgeCount; i++) {
            int from = random.nextInt(vertexCount);
            int to = distinctEndpoint(random, vertexCount, from);
            int edgeWeight = weight(random);
            if (rank[from] > rank[to]) {
                int swap = from;
                from = to;
                to = swap;
            }
            graph.addEdge(from, to, edgeWeight);
        }
        return graph;
    }

    private static int[] permutation(int size, Random random) {
        int[] values = new int[size];
        for (int i = 0; i < size; i++) values[i] = i;
        for (int i = size - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int swap = values[i];
            values[i] = values[j];
            values[j] = swap;
        }
        return values;
    }

    private static int distinctEndpoint(Random random, int vertexCount, int from) {
        int to;
        do {
            to = random.nextInt(vertexCount);
        } while (to == from);
        return to;
    }

    private static int weight(Random random) {
        return 1 + random.nextInt(1000);
    }

    private static void validate(int vertexCount, int edgeCount, boolean spanningTree) {
        if (vertexCount < 2) throw new IllegalArgumentException("At least two vertices are required");
        if (edgeCount < 0 || (spanningTree && edgeCount < vertexCount - 1)) {
            throw new IllegalArgumentException("Invalid edge count for requested graph");
        }
    }
}
