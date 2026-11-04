#!/usr/bin/env bash
# Fail when .template-version drifts from TEMPLATE_INDEX.
# On the template repo, also require match with release-please / CITATION / plugin.
# Child products keep product semver in release-please/CITATION; .template-version
# tracks upstream agent-project-bootstrap provenance only.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

if [ ! -f .template-version ]; then
  echo "MISSING: .template-version"
  exit 1
fi

VERSION="$(tr -d '[:space:]' < .template-version)"
export PYTHONPATH="$ROOT/scripts/lib${PYTHONPATH:+:$PYTHONPATH}"
IS_TEMPLATE="$("$PY" -c "from pathlib import Path; from build_sprint_model import is_template_repo; print('1' if is_template_repo(Path('.')) else '0')")"

IDX="$("$PY" -c "import json; print(json.load(open('TEMPLATE_INDEX.json'))['template_version'])")"
if [ "$IDX" != "$VERSION" ]; then
  echo "FAIL: TEMPLATE_INDEX template_version ($IDX) != .template-version ($VERSION)"
  exit 1
fi

if [ "$IS_TEMPLATE" != "1" ]; then
  echo "OK: child repo — product semver stays in release-please/CITATION; template provenance is $VERSION"
  echo "Template version sync OK ($VERSION)"
  exit 0
fi

if [ ! -f .release-please-manifest.json ]; then
  echo "MISSING: version manifest"
  exit 1
fi
MANIFEST="$("$PY" -c "import json; print(json.load(open('.release-please-manifest.json'))['.'].strip())")"
if [ "$MANIFEST" != "$VERSION" ]; then
  echo "FAIL: .template-version ($VERSION) != manifest ($MANIFEST)"
  echo "Fix: bash scripts/sync-template-version.sh"
  exit 1
fi

CFF="$("$PY" -c "
import re
from pathlib import Path
text = Path('CITATION.cff').read_text(encoding='utf-8')
m = re.search(r'(?m)^version:\\s*([\\d.]+)', text)
print(m.group(1) if m else '')
")"
if [ -z "$CFF" ] || [ "$CFF" != "$VERSION" ]; then
  echo "FAIL: CITATION.cff version ($CFF) != .template-version ($VERSION)"
  echo "Fix: bash scripts/sync-template-version.sh"
  exit 1
fi

CFF_DATE="$("$PY" -c "
import re
from pathlib import Path
text = Path('CITATION.cff').read_text(encoding='utf-8')
m = re.search(r'(?m)^date-released:\\s*(\\d{4}-\\d{2}-\\d{2})', text)
print(m.group(1) if m else '')
")"
if [ -z "$CFF_DATE" ]; then
  echo "FAIL: CITATION.cff missing date-released: YYYY-MM-DD"
  echo "Fix: bash scripts/sync-template-version.sh"
  exit 1
fi

PLUGIN="$("$PY" -c "import json; print(json.load(open('.cursor-plugin/plugin.json', encoding='utf-8')).get('version',''))")"
if [ "$PLUGIN" != "$VERSION" ]; then
  echo "FAIL: .cursor-plugin/plugin.json version ($PLUGIN) != .template-version ($VERSION)"
  echo "Fix: bash scripts/sync-template-version.sh"
  exit 1
fi

RP_PLUGIN="$("$PY" -c "
import json
cfg = json.load(open('release-please-config.json', encoding='utf-8'))
ok = False
for item in cfg['packages']['.']['extra-files']:
    if isinstance(item, dict) and item.get('path') == '.cursor-plugin/plugin.json' and item.get('jsonpath') == '\$.version':
        ok = True
print('ok' if ok else '')
")"
if [ "$RP_PLUGIN" != "ok" ]; then
  echo "FAIL: release-please-config extra-files must bump .cursor-plugin/plugin.json \$.version"
  exit 1
fi

echo "Template version sync OK ($VERSION)"
