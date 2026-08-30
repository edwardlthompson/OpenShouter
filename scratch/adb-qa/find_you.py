import re
from pathlib import Path

xml = Path(r"C:\Users\edwar\OpenShouter\scratch\adb-qa\msg.xml").read_text(encoding="utf-8")
hits = 0
for node in re.finditer(r"<node\b[^>]*>", xml):
    chunk = node.group(0)
    text = re.search(r'text="([^"]*)"', chunk)
    desc = re.search(r'content-desc="([^"]*)"', chunk)
    t = text.group(1) if text else ""
    d = desc.group(1) if desc else ""
    if "You" not in t and "You" not in d:
        continue
    hits += 1
    cls = re.search(r'class="([^"]+)"', chunk)
    bounds = re.search(r'bounds="([^"]+)"', chunk)
    print(
        "hit", hits,
        "t_len", len(t),
        "d_len", len(d),
        "t_is_you", t == "You",
        "d_is_you", d == "You",
        "t_ascii", t.isascii(),
        (cls.group(1) if cls else "").rsplit(".", 1)[-1],
        bounds.group(1) if bounds else "",
    )
print("hits", hits)
