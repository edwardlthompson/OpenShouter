import re
import sys
from pathlib import Path

labels = [
    text
    for text in re.findall(r'text="([^"]+)"', Path(sys.argv[1]).read_text(encoding="utf-8"))
    if text.strip()
]
print("labels", len(labels))
for label in labels:
    if label.startswith(("Time", "Battery", "Calendar", "Show", "Announcement", "Items", "Actions")):
        print("HEAD", label, "len", len(label))
    elif "\n" in label or len(label) > 24:
        print("SUPPORT_LEN", len(label), "lines", label.count("\n") + 1)
