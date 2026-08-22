"""FOSS installer filename for GitHub Releases / in-app Install."""
from pathlib import Path
import sys

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts" / "lib"))
from publish_foss_apk import VERSION_RE, asset_name  # noqa: E402


def test_asset_name_matches_in_app_parser() -> None:
    assert asset_name("0.6.0") == "openshouter-0.6.0-foss.apk"


def test_semver_required() -> None:
    assert VERSION_RE.match("0.6.0")
    assert not VERSION_RE.match("v0.6.0")
    assert not VERSION_RE.match("")
