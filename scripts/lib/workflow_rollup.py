"""Evaluate GitHub Actions needs JSON for a required-check rollup job."""
from __future__ import annotations

import json
import os
import sys

OK_RESULTS = frozenset({"success", "skipped"})


def parse_allow_failure(raw: str | None) -> set[str]:
    if not raw:
        return set()
    return {part.strip() for part in raw.split(",") if part.strip()}


def failed_jobs(needs: object, allow_failure: set[str]) -> list[str]:
    if not isinstance(needs, dict):
        raise ValueError("needs must be an object")
    failed: list[str] = []
    for name, info in needs.items():
        if name in allow_failure:
            continue
        result = ""
        if isinstance(info, dict):
            result = str(info.get("result") or "").lower()
        elif isinstance(info, str):
            result = info.lower()
        if result not in OK_RESULTS:
            failed.append(f"{name}={result or 'missing'}")
    return failed


def main() -> int:
    raw = os.environ.get("ROLLUP_RESULTS", "")
    if not raw.strip():
        print("ERROR: ROLLUP_RESULTS is empty", file=sys.stderr)
        return 1
    try:
        needs = json.loads(raw)
    except json.JSONDecodeError as exc:
        print(f"ERROR: ROLLUP_RESULTS is not JSON: {exc}", file=sys.stderr)
        return 1
    allow = parse_allow_failure(os.environ.get("ROLLUP_ALLOW_FAILURE"))
    try:
        failed = failed_jobs(needs, allow)
    except ValueError as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1
    if failed:
        print("FAIL rollup:", ", ".join(failed), file=sys.stderr)
        return 1
    print("OK   workflow rollup")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
