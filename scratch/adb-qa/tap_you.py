import re
import sys
from pathlib import Path

xml = Path(sys.argv[1]).read_text(encoding="utf-8")
cands = []
for node in re.finditer(r"<node\b[^>]*>", xml):
    chunk = node.group(0)
    text = re.search(r'text="([^"]*)"', chunk)
    desc = re.search(r'content-desc="([^"]*)"', chunk)
    if (text and text.group(1) == "You") or (desc and desc.group(1) == "You"):
        bounds = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', chunk)
        if not bounds:
            continue
        x1, y1, x2, y2 = map(int, bounds.groups())
        w, h = x2 - x1, y2 - y1
        cands.append((w * h, (x1 + x2) // 2, (y1 + y2) // 2, w, h))
if not cands:
    raise SystemExit(1)
# prefer a wide conversation row, not a tiny chip
cands.sort(reverse=True)
_, x, y, w, h = cands[0]
print(x, y, w, h)
