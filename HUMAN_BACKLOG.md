# Human Backlog

> Items automation attempted during autonomous `/build` but could not complete. BUILD_PLAN rows stay open until a human finishes them.

| Deferred | Sprint | Owner | Task | Reason |
|----------|--------|-------|------|--------|
| 2026-08-13 | Sprint 0 | HUMAN | Install GitHub CLI (`gh`) and run `scripts/setup-github-repo.ps1` | `gh` not on PATH during bootstrap; workflow action pin validation skipped |
| 2026-08-13 | Sprint 0 | HUMAN | Enable Dependabot alerts, private vulnerability reporting, branch protection | Requires GitHub repo settings |
| 2026-08-13 | Sprint 0 | HUMAN | Fill `.app-update.json` `release_repo` and optional `donations.json` | Product URLs unknown at bootstrap |
