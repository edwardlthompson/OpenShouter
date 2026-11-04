"""Optional init writers: FUNDING.yml, GitHub About topics, Android SDK props."""
from __future__ import annotations

import os
import re
from pathlib import Path

PLACEHOLDER_RE = re.compile(r"\[INSERT[^\]]*\]", re.I)


def detect_android_sdk() -> Path | None:
    """Return the first existing Android SDK root from env or common paths."""
    for key in ("ANDROID_HOME", "ANDROID_SDK_ROOT"):
        raw = (os.environ.get(key) or "").strip()
        if raw:
            path = Path(raw).expanduser()
            if path.is_dir():
                return path
    home = Path.home()
    for candidate in (
        home / "Android" / "Sdk",
        home / "Library" / "Android" / "sdk",
        home / "AppData" / "Local" / "Android" / "Sdk",
    ):
        if candidate.is_dir():
            return candidate
    return None


def write_android_local_properties(root: Path, sdk: Path | None) -> Path | None:
    """Write gitignored examples/android/local.properties when SDK is known."""
    if sdk is None:
        return None
    android_dir = root / "examples" / "android"
    if not android_dir.is_dir():
        return None
    dest = android_dir / "local.properties"
    # Gradle on Windows accepts forward slashes in sdk.dir.
    sdk_dir = str(sdk.resolve()).replace("\\", "/")
    dest.write_text(f"sdk.dir={sdk_dir}\n", encoding="utf-8", newline="\n")
    return dest


def donation_url_usable(url: str) -> bool:
    text = (url or "").strip()
    if not text or PLACEHOLDER_RE.search(text):
        return False
    return text.startswith(("https://", "http://"))


def write_funding_yml(root: Path, url: str) -> Path | None:
    if not donation_url_usable(url):
        return None
    dest = root / ".github" / "FUNDING.yml"
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(f"custom:\n  - {url.strip()}\n", encoding="utf-8")
    return dest


def merge_topics(about_text: str, topics: list[str]) -> str:
    clean = [t.strip().lower().replace(" ", "-") for t in topics if t.strip()]
    if not clean:
        return about_text
    line = ", ".join(clean)
    if "## Topics" in about_text:
        before, rest = about_text.split("## Topics", 1)
        after = rest.split("\n", 1)[1] if "\n" in rest else ""
        # keep remainder after the first paragraph
        parts = after.split("\n\n", 1)
        tail = parts[1] if len(parts) > 1 else ""
        mid = f"## Topics\n\n{line}\n\nSuggested for GitHub discoverability (Settings → About).\n"
        return before + mid + (("\n" + tail) if tail else "")
    return about_text.rstrip() + f"\n\n## Topics\n\n{line}\n"


def write_topics(root: Path, topics: list[str]) -> Path | None:
    path = root / "docs" / "GITHUB_ABOUT.md"
    if not path.is_file() or not topics:
        return None
    text = merge_topics(path.read_text(encoding="utf-8"), topics)
    path.write_text(text, encoding="utf-8")
    return path


def gh_topics_command(topics: list[str]) -> str:
    clean = [t.strip().lower().replace(" ", "-") for t in topics if t.strip()]
    if not clean:
        return ""
    joined = ",".join(clean)
    return f"gh repo edit --add-topic {joined}"
