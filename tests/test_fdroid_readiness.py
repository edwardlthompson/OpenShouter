"""Repo-root Fastlane + F-Droid screenshot helper."""

from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LIB = ROOT / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from android_signing_runbook import check as signing_check  # noqa: E402
from fdroid_screenshots import ROOTS, check_tree  # noqa: E402

FASTLANE = ROOT / "fastlane" / "metadata" / "android" / "en-US"


class FdroidReadinessTests(unittest.TestCase):
    def test_root_fastlane_listing_files(self) -> None:
        for name in ("title.txt", "short_description.txt", "full_description.txt"):
            path = FASTLANE / name
            self.assertTrue(path.is_file(), msg=path.as_posix())
            self.assertTrue(path.read_text(encoding="utf-8").strip(), msg=path.as_posix())
        short = (FASTLANE / "short_description.txt").read_text(encoding="utf-8").strip()
        self.assertLessEqual(len(short), 80)
        title = (FASTLANE / "title.txt").read_text(encoding="utf-8").strip()
        self.assertLessEqual(len(title), 50)
        self.assertNotIn("Golden Path FOSS Android stub", short)
        full = (FASTLANE / "full_description.txt").read_text(encoding="utf-8")
        self.assertNotIn("no F-Droid listing", full)
        self.assertTrue((FASTLANE / "images" / "icon.png").is_file())
        shots = list((FASTLANE / "images" / "phoneScreenshots").glob("*.png"))
        self.assertGreaterEqual(len(shots), 1)

    def test_changelog_matches_version_code(self) -> None:
        gradle = (ROOT / "examples" / "android" / "app" / "build.gradle.kts").read_text(
            encoding="utf-8",
        )
        code = ""
        for line in gradle.splitlines():
            if "versionCode" in line and "=" in line:
                code = line.split("=", 1)[1].split()[0]
                break
        self.assertTrue(code.isdigit(), msg=code)
        log = FASTLANE / "changelogs" / f"{code}.txt"
        self.assertTrue(log.is_file(), msg=log.as_posix())
        self.assertTrue(log.read_text(encoding="utf-8").strip())

    def test_screenshot_roots_include_repo_fastlane(self) -> None:
        self.assertIn(Path("fastlane") / "metadata" / "android", ROOTS)
        self.assertEqual(check_tree(ROOT), [])

    def test_screenshot_checker_skips_readme_and_flags_dummy(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            shots = (
                root
                / "fastlane"
                / "metadata"
                / "android"
                / "en-US"
                / "images"
                / "phoneScreenshots"
            )
            shots.mkdir(parents=True)
            (shots / "README.md").write_text("add real screenshots\n", encoding="utf-8")
            self.assertEqual(check_tree(root), [])
            (shots / "dummy.png").write_bytes(b"not-a-png")
            errors = check_tree(root)
            self.assertTrue(any("dummy screenshot name" in e for e in errors))

    def test_signing_runbook_accepts_strip_safe_gradle(self) -> None:
        self.assertEqual(signing_check(ROOT), [])


if __name__ == "__main__":
    unittest.main()
