"""HUMAN leftover automation — GitHub dismiss reasons stay valid."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from human_task_audit import DISMISS  # noqa: E402

ALLOWED = {"false positive", "won't fix", "used in tests", "not used"}


class HumanTaskAuditTests(unittest.TestCase):
    def test_dismiss_reasons_match_github(self) -> None:
        self.assertTrue(DISMISS)
        self.assertLessEqual({reason for reason, _ in DISMISS.values()}, ALLOWED)
