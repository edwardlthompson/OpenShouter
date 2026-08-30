import re
import sys
from pathlib import Path

needle = sys.argv[2]
xml = Path(sys.argv[1]).read_text(encoding="utf-8")
# Prefer a Switch/CheckBox near the label, else the label itself.
nodes = list(re.finditer(r"<node\b[^>]*>", xml))
label_i = None
for i, node in enumerate(nodes):
    if f'text="{needle}"' in node.group(0):
        label_i = i
        break
if label_i is None:
    raise SystemExit(1)
# search nearby following nodes for a switch
for node in nodes[label_i : label_i + 8]:
    chunk = node.group(0)
    cls = re.search(r'class="([^"]+)"', chunk)
    name = cls.group(1) if cls else ""
    if "Switch" in name or "CheckBox" in name or "switch" in name.lower():
        bounds = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', chunk)
        if bounds:
            x1, y1, x2, y2 = map(int, bounds.groups())
            print((x1 + x2) // 2, (y1 + y2) // 2)
            raise SystemExit(0)
# fallback: far-right of the label row
chunk = nodes[label_i].group(0)
bounds = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', chunk)
x1, y1, x2, y2 = map(int, bounds.groups())
print(x2 + 80, (y1 + y2) // 2)
