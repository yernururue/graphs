package graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Reads a graph from a header followed by the declared number of edge lines. */
public class GraphReader {
    public static Graph read(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("Line 1: missing graph header");
            }
            String[] fields = header.trim().split("\\s+");
            if (fields.length != 3) {
                throw new IOException("Line 1: expected n m directed|undirected");
            }

            int n;
            int m;
            try {
                n = Integer.parseInt(fields[0]);
                m = Integer.parseInt(fields[1]);
            } catch (NumberFormatException error) {
                throw new IOException("Line 1: n and m must be integers in the int range", error);
            }
            if (n < 0 || m < 0) {
                throw new IOException("Line 1: n and m must be non-negative");
            }
            if (!fields[2].equals("directed") && !fields[2].equals("undirected")) {
                throw new IOException("Line 1: mode must be directed or undirected");
            }

            Graph graph = new Graph(n, fields[2].equals("directed"));
            long lineNumber = 1;
            for (int edge = 0; edge < m; edge++) {
                lineNumber++;
                String line = reader.readLine();
                if (line == null) {
                    throw new IOException("Line " + lineNumber + ": missing edge");
                }
                String[] parts = line.trim().split("\\s+");
                if (parts.length != 3) {
                    throw new IOException("Line " + lineNumber + ": expected from to weight");
                }

                int from;
                int to;
                int weight;
                try {
                    from = Integer.parseInt(parts[0]);
                    to = Integer.parseInt(parts[1]);
                    weight = Integer.parseInt(parts[2]);
                } catch (NumberFormatException error) {
                    throw new IOException("Line " + lineNumber
                            + ": from, to and weight must be integers in the int range", error);
                }
                if (from < 0 || from >= n || to < 0 || to >= n) {
                    throw new IOException("Line " + lineNumber + ": edge endpoints must be in [0, " + n + ")");
                }
                graph.addEdge(from, to, weight);
            }

            String extra;
            while ((extra = reader.readLine()) != null) {
                lineNumber++;
                if (!extra.isBlank()) {
                    throw new IOException("Line " + lineNumber + ": unexpected input after " + m + " edges");
                }
            }
            return graph;
        }
    }
}
