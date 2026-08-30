import re
import sys
from pathlib import Path

needle = sys.argv[2]
text = Path(sys.argv[1]).read_text(encoding="utf-8")
for node in re.finditer(r"<node\b[^>]*>", text):
    chunk = node.group(0)
    if f'content-desc="{needle}"' not in chunk and f'text="{needle}"' not in chunk:
        continue
    bounds = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', chunk)
    if not bounds:
        continue
    x1, y1, x2, y2 = map(int, bounds.groups())
    print((x1 + x2) // 2, (y1 + y2) // 2)
    raise SystemExit(0)
raise SystemExit(1)
