"""Build a Gradle release-signed APK and upload openshouter-X.Y.Z-foss.apk."""
from __future__ import annotations

import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ANDROID = ROOT / "examples" / "android"
RELEASE_APK_DIR = ANDROID / "app" / "build" / "outputs" / "apk" / "release"
VERSION_RE = re.compile(r"^[0-9]+\.[0-9]+\.[0-9]+$")
DEBUG_MARKERS = ("Android Debug", "androiddebugkey", "CN=Android Debug")


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
    missing = [name for name, value in (
        ("store password", store_password),
        ("key alias", key_alias),
        ("key password", key_password),
    ) if not value]
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
    signer = apksigner()
    print(f"Verifying release signature for {asset}...")
    assert_release_signed(dest, signer)
    print(f"Uploading {asset} to {tag}...")
    run(["gh", "release", "upload", tag, str(dest), "--clobber"])
    print(f"OK   published {asset} on {tag}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
