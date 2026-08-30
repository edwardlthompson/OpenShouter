import re
import sys
from pathlib import Path

text = Path(sys.argv[1]).read_text(encoding="utf-8")
labels = re.findall(r'text="([^"]+)"', text)
print("count", len(labels))
for label in labels:
    if label.strip():
        print(label)
