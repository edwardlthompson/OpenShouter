"""Build a Gradle release-signed APK and upload openshouter-X.Y.Z-foss.apk."""
from __future__ import annotations

import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

from publish_foss_signing import (
    ANDROID,
    DEBUG_MARKERS,
    ROOT,
    apksigner,
    assert_release_signed,
    die,
    find_release_apk,
    release_signing_ready,
    run,
)

VERSION_RE = re.compile(r"^[0-9]+\.[0-9]+\.[0-9]+$")


def asset_name(ver: str) -> str:
    return f"openshouter-{ver}-foss.apk"


def version() -> str:
    raw = (ROOT / ".template-version").read_text(encoding="utf-8").strip()
    if not VERSION_RE.match(raw):
        die(f"ERROR: invalid .template-version {raw!r}")
    return raw


def main(argv: list[str]) -> int:
    tag = ""
    force = False
    i = 0
    while i < len(argv):
        if argv[i] == "--tag":
            tag = argv[i + 1] if i + 1 < len(argv) else ""
            i += 2
            continue
        if argv[i] == "--force":
            force = True
            i += 1
            continue
        if argv[i] in {"-h", "--help"}:
            print("Usage: publish-foss-apk [--tag vX.Y.Z] [--force]")
            return 0
        die(f"Unknown option: {argv[i]}")
    ready, detail = release_signing_ready()
    if not ready:
        die(
            "FAIL: release signing not configured — "
            f"{detail}.\n"
            "Run: bash scripts/generate-release-keystore.sh (once), then retry.",
        )
    ver = version()
    tag = tag or f"v{ver}"
    asset = asset_name(ver)
    if not shutil.which("gh"):
        die("ERROR: gh CLI required")
    if subprocess.run(["gh", "release", "view", tag], capture_output=True).returncode != 0:
        die(f"FAIL: GitHub Release {tag} not found (merge Release Please first)")
    listed = subprocess.check_output(
        ["gh", "release", "view", tag, "--json", "assets", "-q", ".assets[].name"],
        text=True,
    )
    if (not force) and asset in listed.splitlines():
        print(f"OK   {asset} already on {tag}")
        return 0
    if not (ANDROID / "gradlew").is_file():
        die(f"ERROR: missing {ANDROID / 'gradlew'}")
    env = os.environ.copy()
    env.setdefault("SOURCE_DATE_EPOCH", "1700000000")
    gradle = ANDROID / ("gradlew.bat" if os.name == "nt" else "gradlew")
    print(f"Building release-signed APK (SOURCE_DATE_EPOCH={env['SOURCE_DATE_EPOCH']})...")
    print(f"Release keystore: {detail}")
    run([str(gradle), "assembleRelease", "--no-daemon"], cwd=ANDROID, env=env)
    built = find_release_apk()
    dest = ROOT / "dist" / asset
    dest.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(built, dest)
    print(f"Verifying release signature for {asset}...")
    assert_release_signed(dest, apksigner())
    print(f"Uploading {asset} to {tag}...")
    run(["gh", "release", "upload", tag, str(dest), "--clobber"])
    print(f"OK   published {asset} on {tag}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
