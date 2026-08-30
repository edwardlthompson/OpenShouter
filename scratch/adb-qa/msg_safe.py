import re
from pathlib import Path

xml = Path(r"C:\Users\edwar\OpenShouter\scratch\adb-qa\msg.xml").read_text(encoding="utf-8")
pkg = re.search(r'package="([^"]+)"', xml)
print("package", pkg.group(1) if pkg else "?")
print("nodes", xml.count("<node"))
for needle in ("Start chat", "Start Chat", "You", "Message yourself", "yourself", "Photos", "Send"):
    print(("yes" if needle.lower() in xml.lower() else "no"), needle)
