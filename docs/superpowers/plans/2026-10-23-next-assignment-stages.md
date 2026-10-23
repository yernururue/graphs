# Remaining Assignment Stages Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete Assignment 3 after MST: SCC, topological ordering, DAG shortest paths, deterministic benchmarks/metrics, and the report.

**Architecture:** Implement iterative Kosaraju and Kahn on the existing Graph API, then DAG shortest paths using Kahn's order. Add in-algorithm counters, a seeded graph generator, self-checking benchmark output in CSV, tests against brute-force/Bellman-Ford and fixed reference values, and a concise REPORT.md.

**Tech Stack:** Java 17, Maven, JUnit 5, existing Graph/DSU/MST.

**Spec:** Assignment PDF sections 4–10.

## Global Constraints

- SCC DFS is iterative and stack-safe for 100,000 vertices.
- Kahn must reject cyclic input instead of returning an invalid order; validate every returned edge ordering.
- DAG shortest paths allow negative edges, initialize every indegree-zero vertex to distance zero, and retain parents for path restoration.
- Counters (`dsuOps`, `edgeScans`, `comparisons`, `maxDepth`) are counted inside the algorithms.
- Seeded graphs use fresh `Random(42)` and sizes 1,000, 3,162, 10,000, 31,623, 100,000 with m=4n and m=20n.
- Benchmark checks Kruskal/Prim weight agreement, valid topological order, and reference values before writing CSV.
- Preserve original user staged files and unrelated untracked files; only integrator updates tracker and commits.

## Tasks

### Task 1: SCC and topological ordering

Files: create `src/main/java/algorithms/SCC.java`, `TopologicalSort.java`; add corresponding JUnit tests; update README.

Tests: random SCCs against all-pairs reachability for n<=200; 100,000-vertex path stack-safety; DAG order validity; cycle rejection.

Commit: `feat(scc): add iterative Kosaraju and Kahn ordering` plus a separate test commit if useful.

### Task 2: DAG shortest paths

Files: create `DagShortestPath.java` and tests; update README.

Tests: compare seeded small DAGs including negative edges to Bellman-Ford; verify all source initialization and restored paths.

Commit: `feat(paths): add DAG shortest paths`.

### Task 3: Counters, deterministic generator, benchmark

Files: create a shared metrics type and benchmark/generator classes; update MST counters; add tests.

Requirements: sizes and densities exactly as specified, fresh `Random(42)`, five-run median time, 50 CSV rows with the specified columns. Fail loudly on MST disagreement, invalid topological order, or reference mismatch.

Commit: `feat(metrics): add reproducible benchmark and counters`.

### Task 4: Report

Files: create `REPORT.md` and plots under `results/plots/` from `results/results.csv`.

Include complexity table, counter ratios, correctness arguments, algorithm choice table, log-log slopes, three required plots, and measured-time caveat.

Commit: `docs(report): add benchmark analysis and plots`.

## Review Focus

- Duplicate edges, self-loops, isolated vertices and empty graphs in directed algorithms.
- SCC correctness when component members are reachable in both directions.
- Kahn's handling of parallel arcs and cycles.
- Negative weights, unreachable sentinel behavior, and parent reconstruction in DAG paths.
- Deterministic PRNG call order and exact logical edge counts in benchmark generators.
- CSV escaping/column order and refusal to emit results after a failed self-check.
