import sys
from pathlib import Path

needle = sys.argv[2]
text = Path(sys.argv[1]).read_text(encoding="utf-8")
ok = needle in text
print("found" if ok else "missing")
raise SystemExit(0 if ok else 1)
