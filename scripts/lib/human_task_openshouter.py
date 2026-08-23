"""OpenShouter-specific HUMAN row automation (product copy + FOSS gates)."""
from __future__ import annotations

import re
from pathlib import Path

from human_task_audit import (
    automate_code_scanning_triage,
    automate_donate_smoke,
    automate_first_push_ci,
)
from human_task_core import AttemptResult, run_cmd


def _read(root: Path, rel: str) -> str:
    path = root / rel
    return path.read_text(encoding="utf-8", errors="replace") if path.is_file() else ""


def automate_foss_deps(root: Path, _cfg: dict) -> AttemptResult:
    blob = []
    android = root / "examples/android"
    if android.is_dir():
        for path in android.rglob("*"):
            if "build" in path.parts or path.suffix not in {".kts", ".toml", ".gradle"}:
                continue
            blob.append(path.read_text(encoding="utf-8", errors="replace").lower())
    hits = [name for name in ("play-services", "firebase", "play-core") if name in "\n".join(blob)]
    if hits:
        return AttemptResult(1, "foss-deps", f"forbidden SDK: {', '.join(hits)}", True)
    return AttemptResult(0, "foss-deps", "No Play Services / Firebase in Gradle manifests", False)


def automate_tts_format(root: Path, _cfg: dict) -> AttemptResult:
    text = _read(root, "examples/android/app/src/main/java/org/openshouter/domain/TtsFormat.kt")
    if 'DEFAULT = "%app: %title - %text"' in text:
        return AttemptResult(0, "tts-format", "Default format matches approved string", False)
    return AttemptResult(1, "tts-format", "TtsFormat.DEFAULT mismatch", True)


def automate_location_copy(root: Path, _cfg: dict) -> AttemptResult:
    strings = _read(root, "examples/android/app/src/main/res/values/strings.xml")
    privacy = _read(root, "docs/PRIVACY.md")
    if "setup_location" in strings and "dashboard_background_rationale" in strings and "Location" in privacy:
        return AttemptResult(0, "location-copy", "Background location copy present", False)
    return AttemptResult(1, "location-copy", "Missing location UX or PRIVACY.md", True)


def automate_permission_copy(root: Path, _cfg: dict) -> AttemptResult:
    strings = _read(root, "examples/android/app/src/main/res/values/strings.xml")
    keys = (
        "setup_listener", "setup_phone", "setup_contacts", "setup_call_log",
        "setup_location", "setup_battery", "setup_exact_alarms",
        "setup_calendar", "setup_bluetooth",
    )
    missing = [key for key in keys if key not in strings]
    if missing:
        return AttemptResult(1, "permission-copy", f"missing {', '.join(missing)}", True)
    privacy = _read(root, "docs/PRIVACY.md")
    if "Calendar" not in privacy or "Bluetooth" not in privacy:
        return AttemptResult(1, "permission-copy", "PRIVACY.md missing Calendar/Bluetooth", True)
    return AttemptResult(0, "permission-copy", "Permission rationale strings present", False)


def automate_exact_alarm_copy(root: Path, _cfg: dict) -> AttemptResult:
    strings = _read(root, "examples/android/app/src/main/res/values/strings.xml")
    if "setup_exact_alarms" in strings and "reminder" in strings.lower() and "time_exact" in strings:
        return AttemptResult(0, "exact-alarm-copy", "Exact alarm + reminder copy present", False)
    return AttemptResult(1, "exact-alarm-copy", "Missing exact-alarm or reminder copy", True)


def automate_query_all_packages(root: Path, _cfg: dict) -> AttemptResult:
    manifest = _read(root, "examples/android/app/src/main/AndroidManifest.xml")
    docs = _read(root, "README.md") + _read(root, "modules/android/MODULE.md")
    if "QUERY_ALL_PACKAGES" in manifest and "GitHub Releases only" in docs:
        return AttemptResult(0, "query-all-packages", "QUERY_ALL_PACKAGES + GitHub Releases only", False)
    return AttemptResult(1, "query-all-packages", "Missing permission or Releases-only docs", True)


def automate_oem_copy(root: Path, _cfg: dict) -> AttemptResult:
    strings = _read(root, "examples/android/app/src/main/res/values/strings.xml")
    oem = _read(root, "examples/android/app/src/main/java/org/openshouter/oem/OemAutostart.kt")
    if "oem_help" in strings and "Intent" in oem and "play-services" not in oem.lower():
        return AttemptResult(0, "oem-copy", "OEM copy + vendor intents, no extra SDKs", False)
    return AttemptResult(1, "oem-copy", "OEM copy or SDK check failed", True)


def automate_github_about(root: Path, _cfg: dict) -> AttemptResult:
    about = _read(root, "docs/GITHUB_ABOUT.md")
    desc = next((line.strip() for line in about.splitlines() if line.startswith("OpenShouter")), "")
    if not desc:
        return AttemptResult(1, "github-about", "docs/GITHUB_ABOUT.md missing draft", True)
    code, tail = run_cmd(root, ["gh", "repo", "edit", "--description", desc[:350]])
    if code != 0:
        return AttemptResult(1, "github-about", tail or "gh repo edit failed", True)
    topics: list[str] = []
    capture = False
    for line in about.splitlines():
        if line.startswith("## Topics"):
            capture = True
            continue
        if capture and line.strip() and not line.startswith("#"):
            topics = [part.strip() for part in line.split(",") if part.strip()]
            break
    if topics:
        args = ["gh", "repo", "edit"]
        for topic in topics:
            args.extend(["--add-topic", topic])
        run_cmd(root, args)
    return AttemptResult(0, "github-about", "GitHub About description + topics set", False)


def extra_human_rules() -> list[tuple[re.Pattern[str], str, object]]:
    return [
        (re.compile(r"GITHUB_ABOUT", re.I), "human", automate_github_about),
        (re.compile(r"FOSS deps|Play Services / Firebase|no Play Services", re.I), "human", automate_foss_deps),
        (re.compile(r"default format|%app: %title - %text", re.I), "human", automate_tts_format),
        (re.compile(r"background location|privacy disclosure", re.I), "human", automate_location_copy),
        (re.compile(r"permission rationale|calendar / Bluetooth permission", re.I), "human", automate_permission_copy),
        (re.compile(r"SCHEDULE_EXACT_ALARM|Announce accurately|reminder exact", re.I), "human", automate_exact_alarm_copy),
        (re.compile(r"QUERY_ALL_PACKAGES", re.I), "human", automate_query_all_packages),
        (re.compile(r"OEM autostart", re.I), "human", automate_oem_copy),
        (re.compile(r"code-scanning|Scorecard|action_required|F-009", re.I), "human", automate_code_scanning_triage),
        (re.compile(r"donate note|Venmo from About|first run silent", re.I), "human", automate_donate_smoke),
        (re.compile(r"first push|check-github-ci", re.I), "auto", automate_first_push_ci),
    ]
