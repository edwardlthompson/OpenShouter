import re
import sys
from pathlib import Path

needle = sys.argv[2]
xml = Path(sys.argv[1]).read_text(encoding="utf-8")
nodes = list(re.finditer(r"<node\b[^>]*>", xml))
for i, node in enumerate(nodes):
    if f'text="{needle}"' not in node.group(0):
        continue
    for other in nodes[i : i + 10]:
        chunk = other.group(0)
        cls = re.search(r'class="([^"]+)"', chunk)
        name = cls.group(1) if cls else ""
        checked = re.search(r'checked="([^"]+)"', chunk)
        if "Switch" in name or "CheckBox" in name:
            print(name, "checked", checked.group(1) if checked else "?")
            raise SystemExit(0)
    print("no-switch")
    raise SystemExit(2)
print("missing-label")
raise SystemExit(1)
