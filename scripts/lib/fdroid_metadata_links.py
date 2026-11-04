"""Assert F-Droid metadata SourceCode/IssueTracker URLs are https GitHub links."""
from __future__ import annotations

import re
from pathlib import Path

NEEDLES = ("SourceCode:", "IssueTracker:")
HTTPS = re.compile(r"^https://github\.com/[^\s]+$")


def check_repo(root: Path) -> list[str]:
    android = root / "examples" / "android"
    if not android.is_dir():
        return []
    meta_dir = android / "metadata"
    files = sorted(meta_dir.glob("*.yml")) if meta_dir.is_dir() else []
    if not files:
        # OpenShouter ships GitHub Releases only; no F-Droid metadata yet.
        return []
    errors: list[str] = []
    for path in files:
        text = path.read_text(encoding="utf-8")
        for key in NEEDLES:
            line = next((ln for ln in text.splitlines() if ln.strip().startswith(key)), None)
            if line is None:
                errors.append(f"{path.as_posix()}: missing {key}")
                continue
            url = line.split(":", 1)[1].strip()
            if not HTTPS.match(url):
                errors.append(f"{path.as_posix()}: {key} must be https://github.com/... (got {url!r})")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("F-Droid metadata link check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("F-Droid metadata link check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
