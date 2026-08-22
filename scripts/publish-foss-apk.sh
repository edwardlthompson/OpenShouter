#!/usr/bin/env bash
# Wrapper: sign + upload openshouter-X.Y.Z-foss.apk (logic in scripts/lib/publish_foss_apk.py).
# Uses This Computer's debug keystore. Do not upload CI-built APKs.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PY="${AGENT_PYTHON:-python3}"
exec "$PY" "$ROOT/scripts/lib/publish_foss_apk.py" "$@"
