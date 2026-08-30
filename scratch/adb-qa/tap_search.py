import re
from pathlib import Path

xml = Path(r"C:\Users\edwar\OpenShouter\scratch\adb-qa\op13-apps.xml").read_text(encoding="utf-8")
for needle in ("Search apps", "Search"):
    for node in re.finditer(r"<node\b[^>]*>", xml):
        chunk = node.group(0)
        if f'text="{needle}"' not in chunk and f'content-desc="{needle}"' not in chunk:
            continue
        bounds = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', chunk)
        if not bounds:
            continue
        x1, y1, x2, y2 = map(int, bounds.groups())
        print((x1 + x2) // 2, (y1 + y2) // 2)
        raise SystemExit(0)
raise SystemExit(1)
