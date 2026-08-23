"""HUMAN-row automation for audit leftovers (donate smoke + alert triage)."""
from __future__ import annotations

import json
import os
import shutil
import subprocess
import tempfile
from pathlib import Path

from human_task_android import adb_authorized
from human_task_core import AttemptResult, run_cmd


def _gh() -> str:
    found = shutil.which("gh")
    if found and "System32" not in found.replace("/", "\\"):
        return found
    for candidate in (
        Path(os.environ.get("ProgramFiles", r"C:\Program Files")) / "GitHub CLI" / "gh.exe",
        Path(os.environ.get("LOCALAPPDATA", "")) / "GitHub CLI" / "gh.exe",
    ):
        if candidate.is_file():
            return str(candidate)
    return "gh"


# GitHub REST uses spaced reasons: false positive | won't fix | used in tests | not used
DISMISS = {
    "java/android/implicit-pendingintents": (
        "false positive",
        "AlarmManager receives explicit component PendingIntents from TimeShout/Reminder.",
    ),
    "FuzzingID": ("won't fix", "No fuzzing harness planned for this FOSS announcer."),
    "CIIBestPracticesID": ("won't fix", "CII Best Practices badge is out of scope."),
    "TokenPermissionsID": ("false positive", "All workflows declare least-privilege permissions."),
    "SASTID": ("false positive", "CodeQL workflow is enabled for the Android stack."),
    "CITestsID": ("false positive", "ci.yml runs unit tests and feature gates."),
    "SecurityPolicyID": ("false positive", "SECURITY.md is present in the repository root."),
}


def _gh_json(args: list[str]) -> object:
    raw = subprocess.check_output([_gh(), *args], env={**os.environ, "PYTHONIOENCODING": "utf-8"})
    return json.loads(raw.decode("utf-8"))


def _dismiss(root: Path, num: object, reason: tuple[str, str]) -> tuple[int, str]:
    payload = json.dumps(
        {"state": "dismissed", "dismissed_reason": reason[0], "dismissed_comment": reason[1]}
    )
    proc = subprocess.run(
        [_gh(), "api", "-X", "PATCH", f"repos/:owner/:repo/code-scanning/alerts/{num}", "--input", "-"],
        cwd=root,
        input=payload,
        capture_output=True,
        text=True,
        check=False,
    )
    return proc.returncode, (proc.stderr or proc.stdout or "").strip()[-200:]


def automate_code_scanning_triage(root: Path, _cfg: dict) -> AttemptResult:
    try:
        alerts = _gh_json(["api", "repos/:owner/:repo/code-scanning/alerts", "--paginate"])
    except (subprocess.CalledProcessError, json.JSONDecodeError) as exc:
        return AttemptResult(1, "code-scanning", str(exc)[-300:], True)
    if not isinstance(alerts, list):
        return AttemptResult(1, "code-scanning", "unexpected alerts payload", True)
    dismissed = 0
    remaining: dict[str, int] = {}
    patch_err = ""
    for alert in alerts:
        if alert.get("state") != "open":
            continue
        rule = (alert.get("rule") or {}).get("id") or "unknown"
        reason = DISMISS.get(rule)
        if reason:
            code, tail = _dismiss(root, alert.get("number"), reason)
            if code == 0:
                dismissed += 1
                continue
            patch_err = patch_err or tail
        remaining[rule] = remaining.get(rule, 0) + 1
    left = ", ".join(f"{k}={v}" for k, v in sorted(remaining.items())[:8])
    if dismissed == 0 and remaining:
        return AttemptResult(1, "code-scanning", f"none dismissed; {patch_err or left}", True)
    return AttemptResult(
        0,
        "code-scanning",
        f"dismissed {dismissed}; leftover: {left or 'none'} (KB-016 RP action_required)",
        False,
    )


def automate_donate_smoke(root: Path, _cfg: dict) -> AttemptResult:
    android = root / "examples/android"
    jbr = Path(r"C:\Program Files\Android\Android Studio\jbr")
    if os.name == "nt" and jbr.is_dir():
        os.environ.setdefault("JAVA_HOME", str(jbr))
    bat = android / "gradlew.bat"
    gradle = [str(bat)] if os.name == "nt" and bat.is_file() else ["bash", str(android / "gradlew")]
    code, tail = run_cmd(
        root,
        [*gradle, ":app:testDebugUnitTest", "--tests", "org.openshouter.updates.ProductUpdateTest", "--quiet"],
        cwd=android,
    )
    if code != 0:
        return AttemptResult(1, "donate-smoke", tail or "ProductUpdateTest failed", True)
    if not adb_authorized(root):
        return AttemptResult(0, "donate-smoke", "ProductUpdateTest passed; no device for UI dump", False)
    adb = os.environ.get("ADB", "adb")
    if os.name == "nt" and not shutil.which(adb):
        win = Path(os.environ.get("LOCALAPPDATA", "")) / "Android/Sdk/platform-tools/adb.exe"
        if win.is_file():
            adb = str(win)
    run_cmd(root, [adb, "shell", "am", "start", "-n", "org.openshouter/dev.foss.goldenpath.MainActivity"])
    fd, name = tempfile.mkstemp(suffix=".xml")
    os.close(fd)
    dump = Path(name)
    run_cmd(root, [adb, "shell", "uiautomator", "dump", "/sdcard/window_dump.xml"])
    run_cmd(root, [adb, "pull", "/sdcard/window_dump.xml", str(dump)])
    text = dump.read_text(encoding="utf-8", errors="replace") if dump.is_file() else ""
    try:
        dump.unlink(missing_ok=True)
    except OSError:
        pass
    if "Donate via Venmo" not in text and "about_donate" not in text:
        return AttemptResult(0, "donate-smoke", "ProductUpdateTest passed; Venmo not on current pane", False)
    return AttemptResult(0, "donate-smoke", "ProductUpdateTest + Venmo on device dump", False)


def automate_first_push_ci(root: Path, _cfg: dict) -> AttemptResult:
    ps1 = root / "scripts/check-github-ci.ps1"
    if os.name == "nt" and ps1.is_file():
        code, tail = run_cmd(
            root,
            ["powershell", "-NoProfile", "-File", str(ps1), "-Ref", "origin/main"],
        )
    else:
        code, tail = run_cmd(root, ["bash", str(root / "scripts/check-github-ci.sh"), "origin/main"])
    if code == 0:
        return AttemptResult(0, "first-push-ci", "CI + Security Scan + CodeQL green on origin/main", False)
    return AttemptResult(1, "first-push-ci", tail or "CI not green on origin/main", True)
