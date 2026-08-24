#!/usr/bin/env bash
# Build a Gradle release-signed APK and upload openshouter-X.Y.Z-foss.apk.
# Requires examples/android/keystore.properties or RELEASE_* env vars.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PY="${AGENT_PYTHON:-python3}"
exec "$PY" "$ROOT/scripts/lib/publish_foss_apk.py" "$@"
