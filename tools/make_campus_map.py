#!/usr/bin/env python3
"""
Regenerate DKUApp/campus-map.jpg from the official campus map PDF.

Like make_campus_data.py this is build tooling, not part of the submission. Run
it from the project root:

    python3 tools/make_campus_map.py DKU-Campus-Map-2024-官方最新版.pdf

The crop is read straight out of the generated CampusData.java rather than
computed again here. That is the whole point: the background picture and the
node coordinates have to be cropped to the same window, and the only way to
guarantee that is for one of them to read the other's numbers. If they ever
disagreed, every marker would sit slightly off its building.

The result is stored at twice the canvas size, so it still looks sharp if the
canvas grows, while staying a sensible file size.
"""

import io
import re
import sys
from pathlib import Path

from PIL import Image
from pypdf import PdfReader

SUPERSAMPLE = 2      # the saved image is this many times the canvas
QUALITY = 82


def read_constants(java_path: Path) -> dict:
    """Pull the VIEW_*/MAP_* constants out of the generated Java file."""
    text = java_path.read_text(encoding="utf-8")
    wanted = ["MAP_WIDTH", "MAP_HEIGHT", "VIEW_LEFT", "VIEW_BOTTOM", "VIEW_TOP",
              "VIEW_SCALE", "CANVAS_WIDTH", "CANVAS_HEIGHT"]
    values = {}
    for name in wanted:
        m = re.search(rf"\b{name}\s*=\s*([0-9.]+)\s*;", text)
        if not m:
            raise SystemExit(f"could not find {name} in {java_path}")
        values[name] = float(m.group(1))
    return values


def main() -> int:
    root = Path(__file__).resolve().parent.parent
    pdf = Path(sys.argv[1] if len(sys.argv) > 1 else "DKU-Campus-Map-2024-官方最新版.pdf")
    out = root / "DKUApp" / "campus-map.jpg"

    c = read_constants(root / "DKUApp" / "CampusData.java")

    # The PDF page carries one big JPEG, and the map's coordinate space is that
    # image scaled by a fixed factor -- so the mapping from map units to source
    # pixels is just a ratio.
    src = Image.open(io.BytesIO(PdfReader(str(pdf)).pages[0].images[0].data))
    src_w, src_h = src.size
    k = src_w / c["MAP_WIDTH"]

    # The view window, in map units. y counts up from the bottom of the page.
    right_u = c["VIEW_LEFT"] + c["CANVAS_WIDTH"] / c["VIEW_SCALE"]
    top_u, bottom_u = c["VIEW_TOP"], c["VIEW_BOTTOM"]

    # ...and the same window in source pixels, where y counts down from the top.
    box = (round(c["VIEW_LEFT"] * k),
           round((c["MAP_HEIGHT"] - top_u) * k),
           round(right_u * k),
           round((c["MAP_HEIGHT"] - bottom_u) * k))

    size = (int(c["CANVAS_WIDTH"]) * SUPERSAMPLE, int(c["CANVAS_HEIGHT"]) * SUPERSAMPLE)
    cropped = src.crop(box).convert("RGB").resize(size, Image.LANCZOS)
    cropped.save(out, "JPEG", quality=QUALITY, optimize=True)

    print(f"source   {src_w} x {src_h}  (map units / {k:.5f})")
    print(f"crop     {box}  -> {box[2] - box[0]} x {box[3] - box[1]}")
    print(f"wrote    {out}  {size[0]} x {size[1]}  {out.stat().st_size // 1024} KB")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
