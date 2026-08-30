from pathlib import Path

data = Path(sys.argv[1]).read_bytes() if False else None
import sys
data = Path(sys.argv[1]).read_bytes()
# DataStore proto stores key names as UTF-8
needles = (
    b"time", b"time_every", b"time_exact", b"announcer", b"master",
    b"msg_on", b"calls", b"call_repeat",
)
for needle in needles:
    print(needle.decode(), "yes" if needle in data else "no")
print("bytes", len(data))
