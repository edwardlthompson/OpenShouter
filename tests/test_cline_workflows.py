"""Cline workflows exist, point at AGENTS.md, and contain no secrets."""
from __future__ import annotations

import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
WORKFLOWS = ROOT / ".clinerules" / "workflows"
REQUIRED = (
    "tour.md",
    "coach.md",
    "build.md",
    "verify.md",
    "ship.md",
    "bootstrap.md",
    "gates.md",
    "ideas.md",
    "compact.md",
    "setup-local.md",
    "fix.md",
    "adr.md",
)
# Split marker so detect-private-key pre-commit does not flag this test source.
_PEM = "BEGIN " + "PRIVATE KEY"
SECRETISH = re.compile(rf"(API[_-]?KEY|sk-[A-Za-z0-9]{{10,}}|{_PEM})", re.I)


class ClineWorkflowTests(unittest.TestCase):
    def test_core_workflows(self) -> None:
        self.assertTrue(WORKFLOWS.is_dir())
        for name in REQUIRED:
            path = WORKFLOWS / name
            self.assertTrue(path.is_file(), name)
            text = path.read_text(encoding="utf-8")
            self.assertIn("AGENTS.md", text, name)
            self.assertIsNone(SECRETISH.search(text), name)
            self.assertLessEqual(len(text.splitlines()), 40, name)


if __name__ == "__main__":
    unittest.main()
