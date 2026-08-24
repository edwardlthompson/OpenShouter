"""Tests for release APK publish helpers."""
from __future__ import annotations

import tempfile
import unittest
from pathlib import Path

import sys

sys.path.insert(0, str(Path(__file__).resolve().parent))
from publish_foss_apk import DEBUG_MARKERS, release_signing_ready


class PublishFossApkTest(unittest.TestCase):
    def test_release_signing_missing_without_props(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            android = Path(tmp)
            ready, reason = release_signing_ready(android)
            self.assertFalse(ready)
            self.assertIn("missing", reason)

    def test_release_signing_ready_with_props_and_keystore(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            android = Path(tmp)
            (android / "keystore.properties").write_text(
                "\n".join(
                    [
                        "storeFile=test.keystore",
                        "storePassword=secret",
                        "keyAlias=openshouter",
                        "keyPassword=secret",
                    ]
                ),
                encoding="utf-8",
            )
            (android / "test.keystore").write_bytes(b"fake")
            ready, detail = release_signing_ready(android)
            self.assertTrue(ready)
            self.assertTrue(detail.endswith("test.keystore"))

    def test_debug_markers_cover_common_cert_text(self) -> None:
        self.assertIn("Android Debug", DEBUG_MARKERS)


if __name__ == "__main__":
    unittest.main()
