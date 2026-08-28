#!/usr/bin/env bash
# Fail a required-check rollup when any needed job failed or was cancelled.
# Inputs: ROLLUP_RESULTS (JSON from toJSON(needs)), optional ROLLUP_ALLOW_FAILURE.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
exec python3 "$ROOT/scripts/lib/workflow_rollup.py"
