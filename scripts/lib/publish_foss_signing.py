"""Release-signing helpers for publish-foss-apk."""
from __future__ import annotations

import os
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ANDROID = ROOT / "examples" / "android"
RELEASE_APK_DIR = ANDROID / "app" / "build" / "outputs" / "apk" / "release"
DEBUG_MARKERS = ("Android Debug", "androiddebugkey", "CN=Android Debug")


def die(msg: str, code: int = 1) -> None:
    print(msg, file=sys.stderr)
    raise SystemExit(code)


def load_keystore_properties(android_dir: Path = ANDROID) -> dict[str, str]:
    props_file = android_dir / "keystore.properties"
    if not props_file.is_file():
        return {}
    props: dict[str, str] = {}
    for line in props_file.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        props[key.strip()] = value.strip()
    return props


def release_signing_ready(android_dir: Path = ANDROID) -> tuple[bool, str]:
    props = load_keystore_properties(android_dir)
    store_file = os.environ.get("RELEASE_STORE_FILE", "").strip() or props.get("storeFile", "").strip()
    store_password = os.environ.get("RELEASE_STORE_PASSWORD", "").strip() or props.get("storePassword", "").strip()
    key_alias = os.environ.get("RELEASE_KEY_ALIAS", "").strip() or props.get("keyAlias", "").strip()
    key_password = os.environ.get("RELEASE_KEY_PASSWORD", "").strip() or props.get("keyPassword", "").strip()
    if not store_file:
        return False, "missing store file (keystore.properties or RELEASE_STORE_FILE)"
    store_path = Path(store_file)
    if not store_path.is_absolute():
        store_path = android_dir / store_path
    if not store_path.is_file():
        return False, f"keystore not found: {store_path}"
    missing = [
        name
        for name, value in (
            ("store password", store_password),
            ("key alias", key_alias),
            ("key password", key_password),
        )
        if not value
    ]
    if missing:
        return False, "missing " + ", ".join(missing)
    return True, str(store_path)


def find_release_apk() -> Path:
    signed = RELEASE_APK_DIR / "app-release.apk"
    unsigned = RELEASE_APK_DIR / "app-release-unsigned.apk"
    if signed.is_file():
        return signed
    if unsigned.is_file():
        die(
            "FAIL: Gradle produced an unsigned release APK.\n"
            "Configure examples/android/keystore.properties (see keystore.properties.example)\n"
            "or set RELEASE_* env vars, then run scripts/generate-release-keystore.sh once.",
        )
    die(f"FAIL: no release APK under {RELEASE_APK_DIR}")


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


def run_capture(cmd: list[str]) -> str:
    if cmd and cmd[0].endswith(".bat"):
        cmd = ["cmd.exe", "/c", *cmd]
    return subprocess.check_output(cmd, text=True)


def assert_release_signed(apk: Path, signer: Path) -> None:
    verify_cmd = [str(signer), "verify", "--verbose", str(apk)]
    run(verify_cmd if signer.suffix.lower() != ".bat" else ["cmd.exe", "/c", *verify_cmd])
    certs = run_capture([str(signer), "verify", "--print-certs", str(apk)]).lower()
    if any(marker.lower() in certs for marker in DEBUG_MARKERS):
        die("FAIL: APK is debug-signed; release keystore is required")
