"""Sign a local release APK and upload openshouter-X.Y.Z-foss.apk."""
from __future__ import annotations

import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ANDROID = ROOT / "examples" / "android"
VERSION_RE = re.compile(r"^[0-9]+\.[0-9]+\.[0-9]+$")


def asset_name(ver: str) -> str:
    return f"openshouter-{ver}-foss.apk"


def die(msg: str, code: int = 1) -> None:
    print(msg, file=sys.stderr)
    raise SystemExit(code)


def version() -> str:
    raw = (ROOT / ".template-version").read_text(encoding="utf-8").strip()
    if not VERSION_RE.match(raw):
        die(f"ERROR: invalid .template-version {raw!r}")
    return raw


def debug_keystore() -> Path:
    home = Path(os.environ.get("USERPROFILE") or os.environ.get("HOME") or "")
    ks = home / ".android" / "debug.keystore"
    if not ks.is_file():
        die("FAIL: debug.keystore not found (expected %USERPROFILE%/.android/debug.keystore)")
    return ks


def apksigner() -> Path:
    which = shutil.which("apksigner")
    if which:
        return Path(which)
    roots = [
        Path(os.environ.get("ANDROID_HOME") or ""),
        Path(os.environ.get("ANDROID_SDK_ROOT") or ""),
        Path(os.environ.get("LOCALAPPDATA") or "") / "Android" / "Sdk",
        Path(os.environ.get("USERPROFILE") or "") / "AppData" / "Local" / "Android" / "Sdk",
    ]
    cands: list[Path] = []
    for sdk in roots:
        if not sdk.is_dir():
            continue
        cands.extend(sorted((sdk / "build-tools").glob("*/apksigner.bat")))
        cands.extend(sorted((sdk / "build-tools").glob("*/apksigner")))
    if not cands:
        die("FAIL: apksigner not found (install Android SDK build-tools)")
    return cands[-1]


def run(cmd: list[str], *, cwd: Path | None = None, env: dict[str, str] | None = None) -> None:
    print("+", " ".join(cmd))
    subprocess.run(cmd, cwd=cwd, env=env, check=True)


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
    print(f"Building release APK (SOURCE_DATE_EPOCH={env['SOURCE_DATE_EPOCH']})...")
    run([str(gradle), "assembleRelease", "--no-daemon"], cwd=ANDROID, env=env)
    apks = list((ANDROID / "app/build/outputs/apk/release").glob("*.apk"))
    if not apks:
        die("FAIL: no release APK under app/build/outputs/apk/release")
    dest = ROOT / "dist" / asset
    dest.parent.mkdir(parents=True, exist_ok=True)
    signer = apksigner()
    ks = debug_keystore()
    print(f"Signing {asset} with local debug keystore...")
    sign_cmd = [str(signer), "sign", "--ks", str(ks), "--ks-key-alias", "androiddebugkey",
                "--ks-pass", "pass:android", "--key-pass", "pass:android",
                "--out", str(dest), str(apks[0])]
    if signer.suffix.lower() == ".bat":
        sign_cmd = ["cmd.exe", "/c", *sign_cmd]
    run(sign_cmd)
    verify_cmd = [str(signer), "verify", "--verbose", str(dest)]
    if signer.suffix.lower() == ".bat":
        verify_cmd = ["cmd.exe", "/c", *verify_cmd]
    run(verify_cmd)
    print(f"Uploading {asset} to {tag}...")
    run(["gh", "release", "upload", tag, str(dest), "--clobber"])
    print(f"OK   published {asset} on {tag}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
