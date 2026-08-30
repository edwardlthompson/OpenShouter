import re
import sys
from pathlib import Path

text = Path(sys.argv[1]).read_text(encoding="utf-8")
seen = []
for m in re.finditer(r'\b(?:text|content-desc)="([^"]+)"', text):
    label = m.group(1)
    if not label or not label.isascii():
        continue
    if label in seen:
        continue
    seen.append(label)
print(" | ".join(seen[:40]))
print("count", len(seen))
