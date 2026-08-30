import re
import subprocess
import sys
from pathlib import Path

adb = r"C:\Users\edwar\AppData\Local\Android\Sdk\platform-tools\adb.exe"
src, dst = sys.argv[1], sys.argv[2]
dump = subprocess.check_output([adb, "-s", dst, "shell", "dumpsys", "telephony.registry"], text=True, errors="replace")
Path(r"C:\Users\edwar\OpenShouter\scratch\adb-qa\telephony-dst.txt").write_text(dump, encoding="utf-8")
nums = []
for pat in (
    r"mLine1Number=([+\d]{7,15})",
    r"mPiiLine1Number=([+\d]{7,15})",
    r"getLine1NumberForDisplay\(\)=([+\d]{7,15})",
):
    nums.extend(re.findall(pat, dump))
# keep unique, prefer +country
uniq = []
for n in nums:
    if n not in uniq:
        uniq.append(n)
if not uniq:
    print("no-number")
    raise SystemExit(2)
# dial first candidate; do not print it
tel = uniq[0]
subprocess.check_call([adb, "-s", src, "shell", "am", "start", "-a", "android.intent.action.CALL", "-d", f"tel:{tel}"])
print("dialed", len(uniq), "candidates")
