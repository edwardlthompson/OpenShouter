"""Hostname-only Venmo label; query-string decoys must not match."""
from __future__ import annotations

import importlib.util
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
_SPEC = importlib.util.spec_from_file_location(
    "sync_stack_config",
    ROOT / "scripts" / "sync-stack-config.py",
)
assert _SPEC and _SPEC.loader
sync_stack_config = importlib.util.module_from_spec(_SPEC)
_SPEC.loader.exec_module(sync_stack_config)


class DonationLabelTest(unittest.TestCase):
    def test_venmo_host(self) -> None:
        self.assertEqual(
            sync_stack_config.donation_label("https://venmo.com/u/openshouter"),
            "Donate via Venmo",
        )
        self.assertEqual(
            sync_stack_config.donation_label("https://www.venmo.com/u/openshouter"),
            "Donate via Venmo",
        )

    def test_query_decoy_is_not_venmo(self) -> None:
        self.assertEqual(
            sync_stack_config.donation_label("https://evil.example/?q=venmo.com"),
            "Donate",
        )
