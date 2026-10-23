#!/usr/bin/env python3
"""Create the assignment's three SVG plots from results/results.csv."""

import csv
import math
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CSV_PATH = ROOT / "results" / "results.csv"
OUT = ROOT / "results" / "plots"
COLORS = ["#2457a7", "#d04a35", "#16835d", "#8b55a1", "#c07800"]
MARKERS = ["circle", "square", "diamond", "triangle", "cross"]


def load_rows():
    with CSV_PATH.open(newline="", encoding="utf-8") as source:
        rows = list(csv.DictReader(source))
    for row in rows:
        for key in ("n", "m", "time_ms", "dsu_ops", "edge_scans", "comparisons", "max_depth", "result"):
            row[key] = int(row[key])
    if len(rows) != 50:
        raise ValueError(f"Expected 50 CSV rows, found {len(rows)}")
    return rows


def work(row):
    """Sum the counters relevant to each algorithm; definitions are explained in REPORT.md."""
    if row["algorithm"] == "Kruskal":
        return row["dsu_ops"] + row["edge_scans"] + row["comparisons"]
    if row["algorithm"] == "Prim":
        return row["edge_scans"] + row["comparisons"]
    if row["algorithm"] == "DAG shortest paths":
        return row["edge_scans"] + row["comparisons"]
    return row["edge_scans"]


def esc(value):
    return (str(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace('"', "&quot;"))


def marker(shape, x, y, color):
    if shape == "circle":
        return f'<circle cx="{x:.1f}" cy="{y:.1f}" r="4" fill="{color}" />'
    if shape == "square":
        return f'<rect x="{x-3.5:.1f}" y="{y-3.5:.1f}" width="7" height="7" fill="{color}" />'
    if shape == "diamond":
        return f'<path d="M{x:.1f},{y-4:.1f} L{x+4:.1f},{y:.1f} L{x:.1f},{y+4:.1f} L{x-4:.1f},{y:.1f} Z" fill="{color}" />'
    if shape == "triangle":
        return f'<path d="M{x:.1f},{y-4:.1f} L{x+4:.1f},{y+3.5:.1f} L{x-4:.1f},{y+3.5:.1f} Z" fill="{color}" />'
    return f'<path d="M{x-3:.1f},{y-3:.1f} L{x+3:.1f},{y+3:.1f} M{x-3:.1f},{y+3:.1f} L{x+3:.1f},{y-3:.1f}" stroke="{color}" stroke-width="1.7" />'


def chart(path, title, x_label, y_label, series, x_log=True, y_log=True, legend_cols=2):
    width, height = 1200, 800
    left, right, top, bottom = 104, 1125, 76, 570
    values_x = [x for points, _, _, _ in series for x, _ in points]
    values_y = [y for points, _, _, _ in series for _, y in points]
    xmin, xmax = min(values_x), max(values_x)
    ymin, ymax = min(values_y), max(values_y)
    if ymin == ymax:
        ymin, ymax = ymin * 0.7, ymax * 1.3
    if y_log:
        ymin, ymax = 10 ** math.floor(math.log10(ymin)), 10 ** math.ceil(math.log10(ymax))
    else:
        padding = (ymax - ymin) * 0.08
        ymin, ymax = ymin - padding, ymax + padding

    def sx(v):
        a, b = (math.log10(v), math.log10(xmin)), (math.log10(xmax), math.log10(xmin))
        return left + (a[0] - a[1]) / (b[0] - b[1]) * (right - left)

    def sy(v):
        if y_log:
            return bottom - (math.log10(v) - math.log10(ymin)) / (math.log10(ymax) - math.log10(ymin)) * (bottom - top)
        return bottom - (v - ymin) / (ymax - ymin) * (bottom - top)

    parts = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
             '<rect width="100%" height="100%" fill="white"/>',
             '<style>text{font-family:Arial,Helvetica,sans-serif;fill:#202a35}.grid{stroke:#dce2e8;stroke-width:1}.axis{stroke:#536170;stroke-width:1.2}</style>',
             f'<text x="{width/2}" y="34" text-anchor="middle" font-size="23" font-weight="600">{esc(title)}</text>']
    if x_log:
        p = math.floor(math.log10(xmin))
        while 10 ** p <= xmax:
            tick = 10 ** p
            if tick >= xmin:
                x = sx(tick)
                parts += [f'<line class="grid" x1="{x:.1f}" y1="{top}" x2="{x:.1f}" y2="{bottom}"/>',
                          f'<text x="{x:.1f}" y="{bottom+23}" text-anchor="middle" font-size="13">10^{p}</text>']
            p += 1
    if y_log:
        p = math.floor(math.log10(ymin))
        while 10 ** p <= ymax:
            tick = 10 ** p
            y = sy(tick)
            parts += [f'<line class="grid" x1="{left}" y1="{y:.1f}" x2="{right}" y2="{y:.1f}"/>',
                      f'<text x="{left-12}" y="{y+4:.1f}" text-anchor="end" font-size="13">10^{p}</text>']
            p += 1
    else:
        for tick in (0, 0.25, 0.5, 0.75, 1):
            yv = ymin + tick * (ymax-ymin)
            y = sy(yv)
            parts += [f'<line class="grid" x1="{left}" y1="{y:.1f}" x2="{right}" y2="{y:.1f}"/>',
                      f'<text x="{left-12}" y="{y+4:.1f}" text-anchor="end" font-size="13">{yv:.2f}</text>']
    parts += [f'<line class="axis" x1="{left}" y1="{bottom}" x2="{right}" y2="{bottom}"/>',
              f'<line class="axis" x1="{left}" y1="{top}" x2="{left}" y2="{bottom}"/>',
              f'<text x="{(left+right)/2}" y="{bottom+52}" text-anchor="middle" font-size="16">{esc(x_label)}</text>',
              f'<text x="25" y="{(top+bottom)/2}" text-anchor="middle" font-size="16" transform="rotate(-90 25 {(top+bottom)/2})">{esc(y_label)}</text>']
    for points, label, color, shape in series:
        coords = " ".join(f"{sx(x):.1f},{sy(y):.1f}" for x, y in points)
        parts.append(f'<polyline points="{coords}" fill="none" stroke="{color}" stroke-width="2.2"/>')
        for x, y in points:
            parts.append(marker(shape, sx(x), sy(y), color))
    rows = math.ceil(len(series) / legend_cols)
    legend_y = bottom + 91
    col_width = (right - left) / legend_cols
    for i, (_, label, color, shape) in enumerate(series):
        col, row = i // rows, i % rows
        lx, ly = left + col * col_width, legend_y + row * 24
        parts += [f'<line x1="{lx}" y1="{ly}" x2="{lx+27}" y2="{ly}" stroke="{color}" stroke-width="2.2"/>',
                  marker(shape, lx+13.5, ly, color),
                  f'<text x="{lx+35}" y="{ly+5}" font-size="13">{esc(label)}</text>']
    parts.append('</svg>')
    path.write_text("\n".join(parts), encoding="utf-8")


def main():
    rows = load_rows()
    OUT.mkdir(parents=True, exist_ok=True)
    grouped = defaultdict(list)
    for row in rows:
        density = row["m"] // row["n"]
        grouped[(row["algorithm"], density)].append(row)
    algorithms = ["Kruskal", "Prim", "Kosaraju", "Kahn", "DAG shortest paths"]

    series = []
    for density in (4, 20):
        for idx, algorithm in enumerate(algorithms):
            data = sorted(grouped[(algorithm, density)], key=lambda r: r["n"])
            series.append(([(r["n"], work(r)) for r in data],
                           f"{algorithm} ({'sparse' if density == 4 else 'dense'})",
                           COLORS[idx], MARKERS[idx]))
    chart(OUT / "operations_vs_n.svg", "Measured operation counters vs. vertices",
          "Vertices n (log scale)", "Algorithm-specific counter sum (log scale)", series)

    series = []
    for density in (4, 20):
        for idx, algorithm in enumerate(algorithms):
            data = sorted(grouped[(algorithm, density)], key=lambda r: r["n"])
            series.append(([(r["n"], r["edge_scans"] / r["m"]) for r in data],
                           f"{algorithm} ({'sparse' if density == 4 else 'dense'})",
                           COLORS[idx], MARKERS[idx]))
    chart(OUT / "scans_per_edge_vs_n.svg", "Edge scans per input edge",
          "Vertices n (log scale)", "edge_scans / m (log scale)", series)

    series = []
    for idx, density in enumerate((4, 20)):
        data = sorted(grouped[("Kruskal", density)], key=lambda r: r["m"])
        series.append(([(r["m"], r["edge_scans"] / r["m"]) for r in data],
                       "Sparse (m = 4n)" if density == 4 else "Dense (m = 20n)",
                       COLORS[idx], MARKERS[idx]))
    chart(OUT / "kruskal_fraction_vs_m.svg", "Fraction of edges examined by Kruskal",
          "Input edges m (log scale)", "edge_scans / m", series, y_log=False, legend_cols=2)


if __name__ == "__main__":
    main()
