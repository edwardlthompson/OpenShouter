# Build Plan

> OpenShouter task board with owner labels. **Completed sprints:** `COMPLETED_TASKS.md`.
> Scaffolded from [agent-project-bootstrap](https://github.com/edwardlthompson/agent-project-bootstrap) v0.17.0.

## Owner Label Legend

| Label   | Owner           | When to use                                                |
| ------- | --------------- | ---------------------------------------------------------- |
| `AGENT` | Cursor Agent    | Code, docs, scaffolding, tests, CI config                  |
| `HUMAN` | Human developer | Approvals, credentials, GitHub settings, product decisions |
| `ADB`   | Human (Android) | Android SDK, emulator/device testing                       |
| `AUTO`  | CI/scripts/bots | GitHub Actions, Dependabot, pre-commit, update checker     |
## Status markers

Use **emoji markers** (not `- [ ]` GitHub checkboxes) so task state reads clearly in Markdown source and Preview. **Applies repo-wide** — `BUILD_PLAN.md`, module checklists, PR template, feature specs, and security triage.

| Marker | State   | Agent action                                                          |
| ------ | ------- | --------------------------------------------------------------------- |
| 🔲     | Open    | Default for new tasks; work or leave queued                           |
| ✅      | Done    | Replace 🔲 when complete; archive sprint rows to `COMPLETED_TASKS.md` |
| ❌      | Blocked | Replace 🔲 when blocked; add brief reason after the description       |
**Task format:** `🔲 [OWNER] Description` · done: `✅ [OWNER] Description` · blocked: `❌ [OWNER] Description — reason`

```bash
grep '\[AGENT\]' BUILD_PLAN.md
grep '\[HUMAN\]' BUILD_PLAN.md
grep '\[ADB\]' BUILD_PLAN.md
grep '\[AUTO\]' BUILD_PLAN.md

```

**Agent rule:** Execute all `[AGENT]` **Sequential** items first, then dispatch **Parallel** agents with isolated file scopes (`docs/PARALLEL_AGENT_SCOPES.md`). Shared schema/types are Sequential-only.

### Parallel dispatch protocol (orchestrator)

| Step | Action                                                                                                                                                                     |
| ---- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1    | Finish all `[AGENT]` **Sequential** items for the active sprint/feature (shared schema/types locked)                                                                       |
| 2    | **Discover** parallelizable work using the decomposition checklist below; add Parallel table rows with non-overlapping `path/**` scopes                                  |
| 3    | Run `bash scripts/plan-parallel-dispatch.sh` → read **agent_count**                                                                                                        |
| 4    | If `agent_count >= 2`, run `/scope` (auto Task dispatch); if `1`, execute inline; if `0`, run `--suggest` and expand the Parallel table (or document `parallel_exception`) |
| 5    | Sequential owner merges results, runs `watch-agent-gates.sh`, updates BUILD_PLAN (Parallel agents never edit BUILD_PLAN)                                                   |
**Planning (Plan Mode):** Every BUILD_PLAN proposal must include `### Parallelization` with `agent_count_target`, decomposition table, and dry-run from `plan-parallel-dispatch.sh`. Run `check-build-plan-parallel.sh` before human approval.

**Autonomous `/build`:** Runs all `[AGENT]`/`[AUTO]` and Parallel work first, then attempts the grouped **Human & device (after automation)** section via `scripts/attempt-build-plan-row.sh`. Success marks ✅; failure appends `HUMAN_BACKLOG.md` and continues — never halts on human labels.

---

## Child Repo Playbook

> **Sprint Audit — 2026-08-13** archived in COMPLETED_TASKS.md.
> **Sprints 3–5 and 8** archived in COMPLETED_TASKS.md @ `247faf2`.
> **Sprint 11 app speak list** archived in COMPLETED_TASKS.md @ `247faf2`.

### Sprint 0 — Bootstrap & identity

<!-- parallel_exception: AGENT parallel listing copy archived to COMPLETED_TASKS.md -->

> Local AGENT/AUTO gates archived in COMPLETED_TASKS.md @ `247faf2`.

### Sequential (must complete in order)

1. ❌ [AUTO] After first push to `main`: `check-github-ci.sh --wait 300` (CI + Security Scan + CodeQL) — Web/Node + CodeQL JS red on pruned stacks; local `if:` guards pending `/push`

> **Sprint 0 HUMAN** archived in COMPLETED_TASKS.md (2026-08-16 `/build`).

---

### Sprint 1 — Android Gradle scaffold

<!-- parallel_exception: AGENT Gradle scaffold archived to COMPLETED_TASKS.md -->

> AGENT/ADB rows archived in COMPLETED_TASKS.md @ `247faf2`.

> **Sprint 1 HUMAN** archived in COMPLETED_TASKS.md (2026-08-16 `/build`).

---

### Sprint 2 — TTS engine + notification listener

<!-- parallel_exception: AGENT TTS/listener work archived to COMPLETED_TASKS.md -->

> AGENT/ADB rows archived in COMPLETED_TASKS.md @ `247faf2`.

> **Sprint 2 HUMAN** archived in COMPLETED_TASKS.md (2026-08-16 `/build`).

---

### Sprint 6 — FOSS geofencing (no Play Services)

<!-- parallel_exception: AGENT geofence work archived to COMPLETED_TASKS.md -->

> AGENT/ADB rows archived in COMPLETED_TASKS.md @ `247faf2`.

> **Sprint 6 HUMAN** archived in COMPLETED_TASKS.md (2026-08-16 `/build`).

---

### Sprint 7 — Compose settings UI

<!-- parallel_exception: AGENT settings UI archived to COMPLETED_TASKS.md -->

> AGENT/ADB rows archived in COMPLETED_TASKS.md @ `247faf2`.

> **Sprint 7 HUMAN** archived in COMPLETED_TASKS.md (2026-08-16 `/build`).

---

### Sprint 9 — Notification TTS quality + unused settings UI

<!-- parallel_exception: AGENT TTS quality + settings UI archived to COMPLETED_TASKS.md -->

> **Sprint 9** AGENT/HUMAN archived in COMPLETED_TASKS.md @ `2ea76a1`.

> **Sprint 9 ADB** archived in COMPLETED_TASKS.md (2026-08-16, CPH2655).

---

### Sprint 10 — Shouter shout channels

<!-- parallel_exception: AGENT shout-channel work archived to COMPLETED_TASKS.md -->

> **Sprint 10** AGENT archived in COMPLETED_TASKS.md (2026-08-14).

> **Sprint 10 ADB** archived in COMPLETED_TASKS.md (2026-08-16, CPH2655).
> **Sprint 10 HUMAN** archived in COMPLETED_TASKS.md (2026-08-16 `/build`).

---

### Sprint 11 — Per-app overrides + backup

<!-- parallel_exception: AGENT override + backup work archived to COMPLETED_TASKS.md -->

> **Sprint 11** AGENT archived in COMPLETED_TASKS.md (2026-08-14). App speak list @ `247faf2`.

> **Sprint 11 ADB** archived in COMPLETED_TASKS.md (2026-08-16, CPH2655).
> **Sprint 11 HUMAN** archived in COMPLETED_TASKS.md (2026-08-16 `/build`).

---

### Sprint 12 — Close notification + TTS quality gaps

<!-- parallel_exception: AGENT notification/TTS quality work archived to COMPLETED_TASKS.md -->

> **Sprint 12** AGENT archived in COMPLETED_TASKS.md (2026-08-14).

> **Sprint 12 ADB** archived in COMPLETED_TASKS.md (2026-08-16, CPH2655).
> **Sprint 12 HUMAN** archived in COMPLETED_TASKS.md (2026-08-16 `/build`).

---

### Sprint 13 — Finish shout channels + backup + overrides

<!-- parallel_exception: AGENT shout-channel + backup work archived to COMPLETED_TASKS.md -->

> **Sprint 13** AGENT archived in COMPLETED_TASKS.md (2026-08-14).

> **Sprint 13 ADB** archived in COMPLETED_TASKS.md (2026-08-16, CPH2655).
> **Sprint 13 HUMAN** archived in COMPLETED_TASKS.md (2026-08-16 `/build`).

---

### Sprint 14 — Close remaining Partial rows

<!-- parallel_exception: AGENT parity close-out archived to COMPLETED_TASKS.md -->

> **Sprint 14** AGENT archived in COMPLETED_TASKS.md (2026-08-15).
> **/ship v0.2.1** archived in COMPLETED_TASKS.md @ `da4d76a`.
> **/ship v0.2.2** archived in COMPLETED_TASKS.md @ `9e2043d`.
> **/ship v0.2.3** archived in COMPLETED_TASKS.md @ `8980374`.
> **/ship v0.2.4** archived in COMPLETED_TASKS.md @ `7490451`.

> **Sprint 14 ADB** archived in COMPLETED_TASKS.md (2026-08-16, CPH2655).

---

### Sprint 15 — OpenShouter extras (ideas 4–7)

<!-- parallel_exception: AGENT extras archived to COMPLETED_TASKS.md -->

> **Sprint 15** AGENT/HUMAN/ADB archived in COMPLETED_TASKS.md (2026-08-20 `/build`).

---

## Ongoing Maintenance (recurring)

### Weekly

- 🔲 [AUTO] `check-security-triage.sh --wait-ci 300` (Dependabot + CI + Scorecard)
- 🔲 [AGENT] Apply Dependabot bumps; triage Scorecard SARIF findings
- 🔲 [AUTO] CI matrix + Repo Hygiene + Feature Gate green on `main`

### Monthly

- 🔲 [AUTO] `simulate-template-upgrade.sh`
- 🔲 [AUTO] `check-license-compliance.sh` + SBOM on latest release

### Pre-release (every version)

- 🔲 [AUTO] `pre-release-gate.sh`
- 🔲 [HUMAN] Approve release tag when product-ready

---

## Archived Sprints

| Sprint | Status | Archive |
| ------ | ------ | ------- |
| Sprint Audit — 2026-08-13 | Complete | `COMPLETED_TASKS.md` |
| Sprints 3–5 and 8 | Complete | `COMPLETED_TASKS.md` |
| Sprint 9 — Notification TTS quality | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 10 — Shouter shout channels | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 11 — Overrides + backup | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 12 — Close notification + TTS quality | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 13 — Finish shout channels + backup | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 14 — Close remaining Partial rows | Complete (AGENT) | `COMPLETED_TASKS.md` |
| /ship v0.2.1 | Complete | `COMPLETED_TASKS.md` |
| /ship v0.2.2 | Complete | `COMPLETED_TASKS.md` |
| /ship v0.2.3 | Complete | `COMPLETED_TASKS.md` |
| /ship v0.2.4 | Complete | `COMPLETED_TASKS.md` |
| /ship v0.2.5 | Complete | `COMPLETED_TASKS.md` |
| Sprint 15 — OpenShouter extras (ideas 4–7) | Complete | `COMPLETED_TASKS.md` |
| Sprints 9–14 ADB QA (CPH2655) | Complete | `COMPLETED_TASKS.md` |
| Sprints 0–13 HUMAN confirmations | Complete | `COMPLETED_TASKS.md` |
| Sprint 11 app speak list | Complete | `COMPLETED_TASKS.md` |
| Template maintainer sprints (v0.9.0–v0.17.0) | Complete (upstream) | `COMPLETED_TASKS.md` (provenance from agent-project-bootstrap) |
