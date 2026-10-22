package algorithms;

public class DSU {
    private int[] parent;
    private int[] size;
    private int components;
    private long findCalls;
    private long successfulUnions;

    public DSU(int n) {
        makeSet(n);
    }

    public void makeSet(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Element count must be non-negative");
        }
        parent = new int[n];
        size = new int[n];
        for (int x = 0; x < n; x++) {
            parent[x] = x;
            size[x] = 1;
        }
        components = n;
        findCalls = 0;
        successfulUnions = 0;
    }

    public int find(int x) {
        checkElement(x);
        findCalls++;
        int root = x;
        while (parent[root] != root) {
            root = parent[root];
        }
        while (parent[x] != x) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    public boolean union(int x, int y) {
        checkElement(x);
        checkElement(y);
        int rootX = find(x);
        int rootY = find(y);
        if (rootX == rootY) {
            return false;
        }
        if (size[rootX] < size[rootY]) {
            int swap = rootX;
            rootX = rootY;
            rootY = swap;
        }
        parent[rootY] = rootX;
        size[rootX] += size[rootY];
        components--;
        successfulUnions++;
        return true;
    }

    public int components() {
        return components;
    }

    public long findCalls() {
        return findCalls;
    }

    public long successfulUnions() {
        return successfulUnions;
    }

    public long dsuOps() {
        return findCalls + successfulUnions;
    }

    private void checkElement(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IndexOutOfBoundsException("Element out of range: " + x);
        }
    }
}
