#!/usr/bin/env python3
"""
Regenerate DKUApp/CampusData.java from the exported campus graph JSON.

This script is NOT part of the submission -- it lives outside DKUApp/ so the
submitted folder stays free of build tooling. Run it from the project root:

    python3 tools/make_campus_data.py campus-graph-20261011-0015.json

What it does
------------
The JSON stores every node in "raw map units": origin at the bottom-left of the
map page, +y pointing up, 6229 x 3494. Swing has the origin at the top-left with
+y pointing down, so the two disagree. Rather than bake that flip into the data
(which would also change the meaning of the distances), the raw units are kept
and the view is given a projection. This script therefore emits, alongside the
node table, the crop-and-scale window that CanvasPanel uses to map raw units
onto the canvas.

The distances themselves are never copied out of the JSON: CampusGraph.connect()
works them out from the coordinates. That is deliberate -- it means the map
picture and the route distances cannot drift apart.
"""

import json
import sys
from pathlib import Path

# --- view window -------------------------------------------------------------
# The nodes only occupy part of the page (the rest is margin and the legend), so
# the view is cropped to the node bounding box plus this much slack, then scaled
# to fit TARGET_HEIGHT. Changing TARGET_HEIGHT is the one knob for canvas size.
MARGIN = 0.08
TARGET_HEIGHT = 780

# --- scale --------------------------------------------------------------------
# Metres per raw map unit. The 2024 map sheet is 8305 x 4658 at its source
# resolution and one of its pixels measures 0.163 m on the ground; these
# coordinates are that sheet scaled by 0.75, so one raw unit is 0.163 / 0.75.
# Nothing on the back end uses this: distances are compared in raw units
# everywhere, and the conversion happens only where one is printed for a person.
METRES_PER_UNIT = 0.163 / 0.75


def java_string(s: str) -> str:
    """A Java string literal for s, quotes and backslashes escaped."""
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"') + '"'


def java_number(v: float) -> str:
    """Trim a float to two decimals without a trailing '.0'."""
    r = round(float(v), 2)
    if r == int(r):
        return str(int(r))
    return f"{r:.2f}"


def main() -> int:
    src = Path(sys.argv[1] if len(sys.argv) > 1 else "campus-graph-20261011-0015.json")
    out = Path(__file__).resolve().parent.parent / "DKUApp" / "CampusData.java"

    data = json.loads(src.read_text(encoding="utf-8"))
    meta, nodes, edges = data["meta"], data["nodes"], data["edges"]

    # --- the view window, derived from where the nodes actually are ----------
    xs = [n["x"] for n in nodes]
    ys = [n["y"] for n in nodes]
    span_x, span_y = max(xs) - min(xs), max(ys) - min(ys)

    view_x = min(xs) - span_x * MARGIN
    view_y = min(ys) - span_y * MARGIN
    view_w = span_x * (1 + 2 * MARGIN)
    view_h = span_y * (1 + 2 * MARGIN)
    scale = TARGET_HEIGHT / view_h

    canvas_w = round(view_w * scale)
    canvas_h = round(view_h * scale)

    # --- emit ---------------------------------------------------------------
    L = []
    L.append("import java.util.ArrayList;")
    L.append("import java.util.List;")
    L.append("")
    L.append("/**")
    L.append(" * BACK-END. Every place on the DKU campus, and every path between them.")
    L.append(" *")
    L.append(" * GENERATED FILE -- do not hand-edit. It is written by")
    L.append(" * tools/make_campus_data.py from the exported campus graph JSON, so the")
    L.append(" * table below and the JSON can never disagree. Re-run the script to")
    L.append(" * refresh it.")
    L.append(" *")
    L.append(" * Coordinates are in raw map units: origin at the BOTTOM-LEFT of the map")
    L.append(" * page, +y pointing UP. Swing draws the other way up, so CanvasPanel")
    L.append(" * flips y when it paints; nothing on the back end does.")
    L.append(" *")
    L.append(f" * A node is either a real destination (a building entrance, a gate --")
    L.append(" * what the JSON calls \"public\") or a bare bend in a path (a waypoint).")
    L.append(" * Only the real destinations can be clicked, and only they are named on")
    L.append(" * the map. A gate additionally charges a flat equivalent distance the")
    L.append(" * moment a route passes INTO it, which CampusGraph folds into the road")
    L.append(" * weight.")
    L.append(" */")
    L.append("public class CampusData {")
    L.append("")
    L.append("    /** The raw coordinate space, as exported. */")
    L.append(f"    public static final int MAP_WIDTH  = {meta['map_width']};")
    L.append(f"    public static final int MAP_HEIGHT = {meta['map_height']};")
    L.append("")
    L.append("    /**")
    L.append("     * Metres per raw map unit, so that a distance can be shown to a person.")
    L.append("     *")
    L.append("     * The map sheet is 8305 x 4658 and one of its pixels measures 0.163 m")
    L.append("     * on the ground; these coordinates are that sheet scaled by 0.75, so")
    L.append("     * one raw unit is 0.163 / 0.75 metres.")
    L.append("     *")
    L.append("     * The back end never uses this. Every distance it compares is in raw")
    L.append("     * units; this exists only for the one number the window prints.")
    L.append("     */")
    L.append(f"    public static final double METRES_PER_UNIT = {METRES_PER_UNIT};")
    L.append("")
    L.append("    /**")
    L.append("     * The window of the map the view shows, and how much it is shrunk.")
    L.append("     *")
    L.append("     * The nodes only cover part of the page -- the rest is margin and the")
    L.append("     * legend -- so the view is cropped to them first. CanvasPanel maps a")
    L.append("     * raw coordinate to a canvas pixel with")
    L.append("     *")
    L.append("     *     screenX = (x - VIEW_LEFT) * VIEW_SCALE")
    L.append("     *     screenY = (VIEW_TOP  - y) * VIEW_SCALE")
    L.append("     *")
    L.append("     * The second line is the whole of the y flip, and it lives only here.")
    L.append("     *")
    L.append("     * Note that VIEW_TOP is NOT MAP_HEIGHT: the crop stops short of the")
    L.append("     * top of the page, because nothing is drawn up there. Measuring the")
    L.append("     * view from the map's own edges instead of its own is an easy way to")
    L.append("     * shift every marker up the screen by the size of the margin.")
    L.append("     */")
    L.append(f"    public static final double VIEW_LEFT   = {java_number(view_x)};")
    L.append(f"    public static final double VIEW_BOTTOM = {java_number(view_y)};")
    L.append(f"    public static final double VIEW_TOP    = {java_number(view_y + view_h)};")
    L.append(f"    public static final double VIEW_SCALE  = {round(scale, 6)};")
    L.append(f"    public static final int CANVAS_WIDTH  = {canvas_w};")
    L.append(f"    public static final int CANVAS_HEIGHT = {canvas_h};")
    L.append("")
    L.append("    /**")
    L.append("     * Build the campus road network.")
    L.append("     *")
    L.append("     * Roads are added by naming their two ends only. How long each one is")
    L.append("     * is worked out from the coordinates, so the map can never disagree")
    L.append("     * with itself about how far apart two places are.")
    L.append("     */")
    L.append("    public static CampusGraph build() {")
    L.append("        CampusGraph g = new CampusGraph();")
    L.append("")
    L.append(f"        // ---- {len(nodes)} nodes ----")
    L.append("        // Node(name, mapX, mapY, selectable, gateCost)")
    for n in nodes:
        selectable = "true" if n["selectable"] else "false"
        L.append(
            f"        g.addNode(new Node({java_string(n['name'])}, "
            f"{java_number(n['x'])}, {java_number(n['y'])}, "
            f"{selectable}, {java_number(n['gate_cost'])}));"
        )
    L.append("")
    L.append(f"        // ---- {len(edges)} roads ----")
    L.append("        // connect(a, b) -- the weight is derived, not typed in.")
    for e in edges:
        L.append(f"        g.connect({java_string(e['from_name'])}, {java_string(e['to_name'])});")
    L.append("")
    L.append("        return g;")
    L.append("    }")
    L.append("")
    L.append("    private CampusData() {}   // static-only holder")
    L.append("}")
    L.append("")

    out.write_text("\n".join(L), encoding="utf-8")

    # --- report, so the numbers can be checked against meta -----------------
    public = [n for n in nodes if n["role"] == "public"]
    gates = [n for n in nodes if n["gate"]]
    costs = sorted({n["gate_cost"] for n in nodes})
    print(f"wrote {out}")
    print(f"  nodes   {len(nodes):>4}  (meta says {meta['node_count']})")
    print(f"  edges   {len(edges):>4}  (meta says {meta['edge_count']})")
    print(f"  public  {len(public):>4}  (meta says {meta['public_node_count']})")
    print(f"  waypoint{len(nodes) - len(public):>4}  (meta says {meta['waypoint_count']})")
    print(f"  gates   {len(gates):>4}  (meta says {meta['gate_count']})")
    print(f"  gate costs seen: {costs}")
    print(f"  view    x={view_x:.0f} y={view_y:.0f} scale={scale:.4f} "
          f"-> canvas {canvas_w} x {canvas_h}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
