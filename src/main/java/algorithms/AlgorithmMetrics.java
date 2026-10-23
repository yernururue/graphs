package algorithms;

/** Deterministic operation counts collected by a graph algorithm run. */
public record AlgorithmMetrics(long dsuOps, long edgeScans, long comparisons, int maxDepth) { }
