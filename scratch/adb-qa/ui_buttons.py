import re
import sys
from pathlib import Path

allow = (
    "Send", "Next", "Done", "Attach", "Photo", "Photos", "Camera", "Gallery",
    "Not now", "Allow", "While using", "Only this time", "Deny",
    "Messages", "Start chat", "New conversation", "You", "MMS",
    "WhatsApp", "Call", "Voice call", "Video call", "Ringing",
    "Answer", "Decline", "End", "Incoming",
    "Announce the time on the hour", "Announcer",
    "Incoming calls", "Once", "Until answered", "WhatsApp Business",
    "Google Messages", "Phone", "Dialer",
)
text = Path(sys.argv[1]).read_text(encoding="utf-8")
found = []
for needle in allow:
    if f'text="{needle}"' in text or f'content-desc="{needle}"' in text:
        found.append(needle)
print("ui:", ", ".join(found) if found else "(none)")
