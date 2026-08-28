"""Release Please config: chore must not mint patches."""
from __future__ import annotations

import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CONFIG = ROOT / "release-please-config.json"


class ReleasePleaseConfigTests(unittest.TestCase):
    def setUp(self) -> None:
        self.data = json.loads(CONFIG.read_text(encoding="utf-8"))

    def test_non_release_types_are_not_changelog_sections(self) -> None:
        types = [row.get("type") for row in self.data.get("changelog-sections", [])]
        self.assertNotIn("chore", types)
        self.assertNotIn("docs", types)

    def test_gradle_version_name_is_extra_file(self) -> None:
        extras = self.data["packages"]["."]["extra-files"]
        self.assertIn("examples/android/app/build.gradle.kts", extras)

    def test_gradle_has_release_please_marker(self) -> None:
        gradle = ROOT / "examples/android/app/build.gradle.kts"
        if not gradle.is_file():
            self.skipTest("android example pruned")
        self.assertIn("x-release-please-version", gradle.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
