#!/usr/bin/env bash
# Validate F-Droid/Fastlane metadata scaffold (AGENT gate; APK hashes remain ADB).
# Usage: scripts/verify-fdroid-metadata.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

META="$ROOT/examples/android/metadata/en-US"
FASTLANE="$ROOT/fastlane/metadata/android/en-US"
GRADLE="$ROOT/examples/android/app/build.gradle.kts"
ERRORS=0

fail() {
  echo "FAIL: $1"
  ERRORS=$((ERRORS + 1))
}

ok() {
  echo "OK   $1"
}

if [ ! -d "$META" ]; then
  fail "missing $META"
else
  ok "metadata directory present"
fi

for f in title.txt short_description.txt full_description.txt; do
  if [ ! -s "$META/$f" ]; then
    fail "missing or empty $META/$f"
  else
    ok "$f present"
  fi
done

if [ ! -f "$GRADLE" ]; then
  fail "missing $GRADLE"
else
  VERSION_CODE="$(grep -E 'versionCode\s*=' "$GRADLE" | head -1 | sed -E 's/.*=\s*([0-9]+).*/\1/')"
  if [ -z "${VERSION_CODE:-}" ]; then
    fail "could not parse versionCode from build.gradle.kts"
  elif [ ! -s "$META/changelogs/${VERSION_CODE}.txt" ]; then
    fail "missing changelog $META/changelogs/${VERSION_CODE}.txt"
  else
    ok "changelog for versionCode ${VERSION_CODE}"
  fi
fi

if [ ! -d "$META/images" ]; then
  fail "missing $META/images/ (add README + assets before submit)"
else
  ok "images directory present"
fi

if [ -f "$ROOT/LICENSE" ]; then
  ok "root LICENSE present (MIT for template)"
else
  fail "missing root LICENSE"
fi

if [ ! -d "$FASTLANE" ]; then
  fail "missing $FASTLANE (F-Droid scrapes repo-root fastlane/)"
else
  ok "repo-root fastlane metadata present"
fi

for f in title.txt short_description.txt full_description.txt; do
  if [ ! -s "$FASTLANE/$f" ]; then
    fail "missing or empty $FASTLANE/$f"
  else
    ok "fastlane $f present"
  fi
done

if [ -n "${VERSION_CODE:-}" ]; then
  if [ ! -s "$FASTLANE/changelogs/${VERSION_CODE}.txt" ]; then
    fail "missing fastlane changelog $FASTLANE/changelogs/${VERSION_CODE}.txt"
  else
    ok "fastlane changelog for versionCode ${VERSION_CODE}"
  fi
fi

if [ ! -f "$FASTLANE/images/icon.png" ]; then
  fail "missing $FASTLANE/images/icon.png"
else
  ok "fastlane icon.png present"
fi

shot_count=0
if [ -d "$FASTLANE/images/phoneScreenshots" ]; then
  shot_count="$(find "$FASTLANE/images/phoneScreenshots" -maxdepth 1 -type f -name '*.png' | wc -l | tr -d ' ')"
fi
if [ "${shot_count:-0}" -lt 1 ]; then
  fail "missing $FASTLANE/images/phoneScreenshots/*.png"
else
  ok "fastlane phoneScreenshots (${shot_count} png)"
fi

if [ -d "$ROOT/examples/android/fastlane/metadata/android/en-US" ]; then
  ok "fastlane metadata mirror present"
fi

echo ""
echo "SKIP [ADB] reproducible APK hash verification — run on device/emulator per modules/android/MODULE.md"

# Dummy/placeholder screenshots fail even when metadata text is present.
if ! bash "$ROOT/scripts/check-fdroid-screenshots.sh"; then
  fail "dummy F-Droid screenshots are not allowed"
fi

if [ "$ERRORS" -gt 0 ]; then
  echo "${ERRORS} F-Droid metadata check(s) failed"
  exit 1
fi

echo "F-Droid metadata scaffold verified"
