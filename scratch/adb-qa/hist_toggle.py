import re
import sys
from pathlib import Path

xml = Path(sys.argv[1]).read_text(encoding="utf-8")
for node in re.finditer(r"<node\b[^>]*>", xml):
    chunk = node.group(0)
    if "Show spoken" not in chunk and "spoken" not in chunk.lower():
        continue
    checked = re.search(r'checked="([^"]+)"', chunk)
    selected = re.search(r'selected="([^"]+)"', chunk)
    cls = re.search(r'class="([^"]+)"', chunk)
    text = re.search(r'text="([^"]*)"', chunk)
    desc = re.search(r'content-desc="([^"]*)"', chunk)
    bounds = re.search(r'bounds="([^"]+)"', chunk)
    print(
        "class", cls.group(1) if cls else "",
        "checked", checked.group(1) if checked else "",
        "selected", selected.group(1) if selected else "",
        "text_len", len(text.group(1)) if text else 0,
        "desc_len", len(desc.group(1)) if desc else 0,
        "bounds", bounds.group(1) if bounds else "",
    )
