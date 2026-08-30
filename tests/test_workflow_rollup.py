"""Required-check rollup helper."""
from __future__ import annotations

import json
import os
import subprocess
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LIB = ROOT / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from workflow_rollup import failed_jobs, main, parse_allow_failure  # noqa: E402


class WorkflowRollupTests(unittest.TestCase):
    def test_success_and_skipped_pass(self) -> None:
        needs = {
            "feature-gate": {"result": "success"},
            "web": {"result": "skipped"},
        }
        self.assertEqual(failed_jobs(needs, set()), [])

    def test_failure_and_cancelled_reported(self) -> None:
        needs = {
            "trivy": {"result": "failure"},
            "gitleaks": {"result": "cancelled"},
        }
        self.assertEqual(
            failed_jobs(needs, set()),
            ["trivy=failure", "gitleaks=cancelled"],
        )

    def test_allow_failure_ignores_named_job(self) -> None:
        needs = {"template-update-check": {"result": "failure"}}
        self.assertEqual(
            failed_jobs(needs, {"template-update-check"}),
            [],
        )

    def test_empty_result_is_missing(self) -> None:
        self.assertEqual(failed_jobs({"ci": {}}, set()), ["ci=missing"])

    def test_non_object_needs_raises(self) -> None:
        with self.assertRaises(ValueError):
            failed_jobs(["success"], set())

    def test_parse_allow_failure(self) -> None:
        self.assertEqual(parse_allow_failure(None), set())
        self.assertEqual(
            parse_allow_failure("a, b"),
            {"a", "b"},
        )

    def test_main_reads_env(self) -> None:
        os.environ["ROLLUP_RESULTS"] = json.dumps(
            {"repo-hygiene": {"result": "success"}}
        )
        os.environ.pop("ROLLUP_ALLOW_FAILURE", None)
        self.assertEqual(main(), 0)
        os.environ["ROLLUP_RESULTS"] = json.dumps(
            {"repo-hygiene": {"result": "failure"}}
        )
        self.assertEqual(main(), 1)
        os.environ["ROLLUP_RESULTS"] = ""
        self.assertEqual(main(), 1)

    def test_required_status_job_names_script(self) -> None:
        proc = subprocess.run(
            ["bash", "scripts/check-required-status-jobs.sh"],
            cwd=ROOT,
            check=False,
            capture_output=True,
            text=True,
        )
        self.assertEqual(proc.returncode, 0, proc.stdout + proc.stderr)
        self.assertIn("Required status job names OK", proc.stdout)


if __name__ == "__main__":
    unittest.main()
