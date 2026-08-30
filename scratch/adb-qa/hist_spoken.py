import re
import sys
from pathlib import Path

labels = [
    text
    for text in re.findall(r'text="([^"]+)"', Path(sys.argv[1]).read_text(encoding="utf-8"))
    if text.strip()
]
time_rows = [text for text in labels if text.startswith("Time")]
print("time_row_count", len(time_rows))
print("time_rows_have_extra", any(len(text) > 28 for text in time_rows))
print("max_time_row_len", max((len(text) for text in time_rows), default=0))
print("show_spoken_present", "Show spoken text" in labels)
print("battery_rows", sum(1 for text in labels if text.startswith("Battery")))
print("calendar_rows", sum(1 for text in labels if text.startswith("Calendar")))
