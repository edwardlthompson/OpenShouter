#!/usr/bin/env bash
# Create examples/android/openshouter-release.keystore + keystore.properties (gitignored).
# Requires RELEASE_STORE_PASSWORD; optional RELEASE_KEY_PASSWORD, RELEASE_KEY_ALIAS.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ANDROID="$ROOT/examples/android"
KS="$ANDROID/openshouter-release.keystore"
PROPS="$ANDROID/keystore.properties"

if [[ -f "$KS" && -f "$PROPS" ]]; then
  echo "Release keystore already exists: $KS"
  exit 0
fi

if [[ -z "${RELEASE_STORE_PASSWORD:-}" ]]; then
  echo "Set RELEASE_STORE_PASSWORD before running (and optionally RELEASE_KEY_PASSWORD, RELEASE_KEY_ALIAS)." >&2
  exit 1
fi

KEY_PASS="${RELEASE_KEY_PASSWORD:-$RELEASE_STORE_PASSWORD}"
ALIAS="${RELEASE_KEY_ALIAS:-openshouter}"

if ! command -v keytool >/dev/null 2>&1; then
  echo "keytool not found — install JDK 17+ and ensure keytool is on PATH." >&2
  exit 1
fi

keytool -genkeypair -v \
  -keystore "$KS" \
  -alias "$ALIAS" \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -storepass "$RELEASE_STORE_PASSWORD" \
  -keypass "$KEY_PASS" \
  -dname "CN=OpenShouter, OU=FOSS, O=OpenShouter, L=Unknown, ST=Unknown, C=US"

cat > "$PROPS" <<EOF
storeFile=openshouter-release.keystore
storePassword=${RELEASE_STORE_PASSWORD}
keyAlias=${ALIAS}
keyPassword=${KEY_PASS}
EOF

echo "Created $KS and $PROPS (both gitignored)."
echo "Back up the keystore — losing it prevents in-place updates for release-signed installs."
