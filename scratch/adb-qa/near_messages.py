import re
from pathlib import Path

xml = Path(r"C:\Users\edwar\OpenShouter\scratch\adb-qa\op13-apps.xml").read_text(encoding="utf-8")
nodes = list(re.finditer(r"<node\b[^>]*>", xml))
for i, node in enumerate(nodes):
    chunk = node.group(0)
    text = re.search(r'text="([^"]*)"', chunk)
    if not text or text.group(1) != "Messages":
        continue
    print("label", i, re.search(r'bounds="([^"]+)"', chunk).group(1))
    for j in range(i, min(len(nodes), i + 16)):
        other = nodes[j].group(0)
        cls = re.search(r'class="([^"]+)"', other)
        name = (cls.group(1) if cls else "").rsplit(".", 1)[-1]
        checked = re.search(r'checked="([^"]+)"', other)
        clickable = re.search(r'clickable="([^"]+)"', other)
        t = re.search(r'text="([^"]*)"', other)
        d = re.search(r'content-desc="([^"]*)"', other)
        raw_t = t.group(1) if t else ""
        raw_d = d.group(1) if d else ""
        if name not in ("Switch", "CheckBox", "View", "TextView"):
            continue
        if raw_t and raw_t != "Messages" and not raw_t.startswith("Messages,") and raw_t not in (
            "App name", "Notification", "Once", "Incoming calls",
        ):
            continue
        print(
            f"  +{j-i}",
            name,
            "chk", checked.group(1) if checked else "?",
            "clk", clickable.group(1) if clickable else "?",
            "t", raw_t if raw_t.isascii() and len(raw_t) < 40 else f"tlen={len(raw_t)}",
            "d", raw_d if raw_d.isascii() and len(raw_d) < 50 else f"dlen={len(raw_d)}",
        )
