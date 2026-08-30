"""Print DataStore preference key names only (no values that might be PII)."""
import re
import sys
from pathlib import Path

raw = Path(sys.argv[1]).read_bytes()
# DataStore proto stores key names as UTF-8 strings.
keys = sorted(set(re.findall(rb"[\x20-\x7e]{3,40}", raw)))
print("keys:")
for key in keys:
    text = key.decode("ascii")
    if any(part in text.lower() for part in ("time", "msg", "call", "ac=", "channel", "enabled", "package")):
        print(" ", text)
