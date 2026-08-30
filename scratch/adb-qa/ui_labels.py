import re
import sys
from pathlib import Path

needles = [
    "Not now",
    "Voice and device state",
    "Per-channel device states",
    "App name cooldown",
    "30 seconds",
    "Announce message notifications",
    "Loop incoming calls",
    "Time shout",
    "Announcement history",
    "Show spoken text",
    "Time",
    "Announce the time on the hour",
    "Incoming calls",
    "Once",
    "Until answered",
    "Apps to shout",
    "Messages",
    "WhatsApp",
    "Phone",
]
text = Path(sys.argv[1]).read_text(encoding="utf-8")
found = []
for needle in needles:
    if f'text="{needle}"' in text:
        found.append(needle)
print("labels:", ", ".join(found) if found else "(none)")
