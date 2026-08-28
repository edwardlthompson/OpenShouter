#!/usr/bin/env bash
# Assert branch-protection check names exist as job-level name: fields.
# Workflow-level names do not create GitHub check contexts (KB-016).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

ERRORS=0

has_job_name() {
  local file="$1"
  local name="$2"
  grep -qE "^    name: ${name}\$" "$file"
}

check_job() {
  local rel="$1"
  local name="$2"
  local file="$ROOT/$rel"
  if [ ! -f "$file" ]; then
    echo "FAIL: $rel missing"
    ERRORS=$((ERRORS + 1))
    return
  fi
  if ! has_job_name "$file" "$name"; then
    echo "FAIL: $rel must define job-level name: $name"
    ERRORS=$((ERRORS + 1))
    return
  fi
  echo "OK   $rel job: $name"
}

check_job ".github/workflows/ci.yml" "CI"
check_job ".github/workflows/ci.yml" "Repo Hygiene"
check_job ".github/workflows/ci.yml" "Feature Gate"
check_job ".github/workflows/security.yml" "Security Scan"
check_job ".github/workflows/codeql.yml" "CodeQL"

if [ "$ERRORS" -gt 0 ]; then
  echo "$ERRORS required-status job name check(s) failed"
  exit 1
fi
echo "Required status job names OK"
