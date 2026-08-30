import re
import sys
from pathlib import Path

needle = sys.argv[2]
xml = Path(sys.argv[1]).read_text(encoding="utf-8")
nodes = list(re.finditer(r"<node\b[^>]*>", xml))
for i, node in enumerate(nodes):
    if f'text="{needle}"' not in node.group(0):
        continue
    start = max(0, i - 2)
    end = min(len(nodes), i + 12)
    for j in range(start, end):
        chunk = nodes[j].group(0)
        cls = re.search(r'class="([^"]+)"', chunk)
        checked = re.search(r'checked="([^"]+)"', chunk)
        clickable = re.search(r'clickable="([^"]+)"', chunk)
        bounds = re.search(r'bounds="([^"]+)"', chunk)
        text = re.search(r'text="([^"]*)"', chunk)
        desc = re.search(r'content-desc="([^"]*)"', chunk)
        t = text.group(1) if text else ""
        d = desc.group(1) if desc else ""
        safe_t = t if t.isascii() and len(t) < 80 else f"len={len(t)}"
        safe_d = d if d.isascii() and len(d) < 40 else f"dlen={len(d)}"
        print(
            f"{j-i:+d}",
            (cls.group(1) if cls else "").rsplit(".", 1)[-1],
            "chk", checked.group(1) if checked else "?",
            "clk", clickable.group(1) if clickable else "?",
            "t", safe_t,
            "d", safe_d,
            bounds.group(1) if bounds else "",
        )
    raise SystemExit(0)
raise SystemExit(1)
