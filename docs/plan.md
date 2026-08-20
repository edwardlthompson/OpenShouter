# Implementation Plan

> Active work lives in `BUILD_PLAN.md`. This file is the SDD milestone stub required by bootstrap 0.21.
> Status: 🔲 open · ✅ done · ❌ blocked.

## Milestone — Bootstrap standards (0.21 sync)

| Task | Owner | Tests / fallback |
|------|-------|------------------|
| ✅ Manifest + adapters + SDD stubs | AGENT | `validate-bootstrap.sh --quick` |
| ✅ OpenShouter HUMAN extras kept | AGENT | `scripts/lib/human_task_openshouter.py` |
| ✅ Product semver stays 0.2.4 | AGENT | `check-template-version-sync.sh` |

## Next feature

1. Copy `docs/features/_template.md` → `docs/features/{name}.md`
2. Lock the public API (Sequential)
3. Add unit tests before or with the implementation
4. Run `python3 scripts/agent-run.py watch-agent-gates --once --autofix`

If automated tests are not feasible, write the justification and fallback command in the feature spec before marking the BUILD_PLAN row ✅.
