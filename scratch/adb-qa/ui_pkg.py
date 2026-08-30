import re
import sys
from pathlib import Path

text = Path(sys.argv[1]).read_text(encoding="utf-8")
m = re.search(r'package="([^"]+)"', text)
print(m.group(1) if m else "unknown")
