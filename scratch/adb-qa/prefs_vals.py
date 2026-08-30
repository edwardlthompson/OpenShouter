import re
import sys
from pathlib import Path

data = Path(sys.argv[1]).read_bytes()
# Preferences proto: key UTF-8 then a typed value. Print nearby ints/bools only.
text = data.decode("latin-1")
for key in ("time", "time_every", "time_exact"):
    idx = text.find(key)
    if idx < 0:
        print(key, "missing")
        continue
    window = data[idx : idx + 24]
    print(key, "at", idx, "next", window[len(key) : len(key) + 8].hex())
