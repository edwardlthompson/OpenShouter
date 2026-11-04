# Build Plan

<!-- remaining-tally -->
**Remaining:** AGENT 0 · LOCAL 0 · CLOUD 0 · AUTO 0 · HUMAN 3 · ADB 2 · **5 open**
<!-- /remaining-tally -->
> OpenShouter task board with owner labels. **Completed sprints:** `COMPLETED_TASKS.md`.
> Scaffolded from [agent-project-bootstrap](https://github.com/edwardlthompson/agent-project-bootstrap) v0.17.0.

## Owner Label Legend

| Label   | Owner           | When to use                                                |
| ------- | --------------- | ---------------------------------------------------------- |
| `AGENT` | Cursor Agent    | Code, docs, scaffolding, tests, CI config                  |
| `HUMAN` | Human developer | Approvals, credentials, GitHub settings, product decisions |
| `ADB`   | Human (Android) | Android SDK, emulator/device testing                       |
| `AUTO`  | CI/scripts/bots | GitHub Actions, Dependabot, pre-commit, update checker     |

**Venue (AGENT only):** `[LOCAL]` This Computer · `[CLOUD]` Cursor Cloud — see [`docs/adr/0008-agent-venue.md`](docs/adr/0008-agent-venue.md). Format: `🔲 [AGENT][LOCAL] …`.

## Product (do not drift)

<!-- product-brief-sync:begin -->
FOSS Android TTS announcer for notifications and Shouter-style telephony/hardware.
Keywords: notification, announcer, FOSS, Shouter, TTS, geofence, caller
<!-- product-brief-sync:end -->

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

<!-- local-agent-lane:begin -->
- ✅ [AGENT][LOCAL] (#14) Mixed: Stamp `.template-version` to `1.12.0` after gates pass — scope: `.template-version`
<!-- local-agent-lane:end -->

<!-- cloud-agent-lane:begin -->
_No cloud agent items._
<!-- cloud-agent-lane:end -->

### Sprint Audit leftover (2026-08-23)

<!-- parallel_exception: HUMAN leftover archived -->

> **Sprint Audit — 2026-08-23** AGENT archived in COMPLETED_TASKS.md @ `ad557fe`.
> **F-009 code-scanning triage** archived in COMPLETED_TASKS.md (2026-08-23).

> **Sprint Audit — 2026-08-13** archived in COMPLETED_TASKS.md.
> **Sprints 3–5 and 8** archived in COMPLETED_TASKS.md @ `247faf2`.
> **Sprint 11 app speak list** archived in COMPLETED_TASKS.md @ `247faf2`.

### Sprint 0 — Bootstrap & identity

<!-- parallel_exception: AGENT parallel listing copy archived to COMPLETED_TASKS.md -->

> Local AGENT/AUTO gates archived in COMPLETED_TASKS.md @ `247faf2`.
> **Sprint 0 first-push CI** archived in COMPLETED_TASKS.md (2026-08-23).

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
> **/ship v0.3.0** archived in COMPLETED_TASKS.md @ `7d10cfb`.

---

### Sprint 16 leftover (device smoke)

<!-- parallel_exception: HUMAN leftover archived -->

> **Sprint 16** AGENT/AUTO archived in COMPLETED_TASKS.md @ `c93d967`.
> **/ship v0.4.0** archived in COMPLETED_TASKS.md @ `c93d967`.
> **Sprint 16 donate smoke** archived in COMPLETED_TASKS.md (2026-08-23).

---

### Sprint 17 leftover

<!-- parallel_exception: AGENT extras archived to COMPLETED_TASKS.md -->

> **Sprint 17** AGENT archived in COMPLETED_TASKS.md @ `768ea59`.
> **/ship v0.5.0** archived in COMPLETED_TASKS.md @ `768ea59`.

---

### Sprint 18 leftover

<!-- parallel_exception: AGENT importer archived to COMPLETED_TASKS.md -->

> **Sprint 18** AGENT archived in COMPLETED_TASKS.md @ `6808c54`.

### Sprint 19 leftover

<!-- parallel_exception: AGENT leftover-pref map archived to COMPLETED_TASKS.md -->

> **Sprint 19** AGENT archived in COMPLETED_TASKS.md @ `6808c54`.
> **/ship v0.6.0** archived in COMPLETED_TASKS.md @ `6808c54`.
> **/ship v0.7.0** archived in COMPLETED_TASKS.md (2026-08-23).
> **/ship v0.8.0** archived in COMPLETED_TASKS.md (2026-08-23).

---

> **Sprint 20** AGENT/AUTO archived in COMPLETED_TASKS.md @ `9a54070`.
> **/ship v0.10.0** archived in COMPLETED_TASKS.md @ `9a54070`.
> **/ship v0.11.0** archived in COMPLETED_TASKS.md @ `909cd58`.
> **Sprint 20 leftover ADB** archived in COMPLETED_TASKS.md @ `83b45d9`.
> **Sprint 21** AGENT/AUTO archived in COMPLETED_TASKS.md @ `66584d0`.
> **/ship v0.12.0** archived in COMPLETED_TASKS.md @ `5816578`.
> **Sprint 22** AGENT/AUTO archived in COMPLETED_TASKS.md @ `38525c3`.
> **/ship v1.0.0** archived in COMPLETED_TASKS.md @ `38525c3`.
> **Sprint 22 leftover ADB** archived in COMPLETED_TASKS.md (2026-08-29).
> **/ship v1.1.1** archived in COMPLETED_TASKS.md @ `3af2512`.

### Sprint 21 leftover ADB

<!-- parallel_exception: HUMAN leftover; AGENT/AUTO archived -->

- 🔲 [ADB] WhatsApp Once then silence after answer; Phone still loops until answer (CPH2583 / CPH2655, v1.1.1 sideloaded; Incoming calls = Once in Apps to shout; release APK has no debug RING — needs a live WhatsApp + cellular ring)

---

> **Sprint 23** archived in COMPLETED_TASKS.md @ `63ad5a6`.

### Sprint 23 leftover ADB

<!-- parallel_exception: HUMAN leftover; AGENT/AUTO archived -->

- 🔲 [ADB] Drop several photos into Messages; first shout may say Messages, later ones do not repeat it within 30s (v1.1.1 sideloaded; cooldown is on Announcer; Google Messages app-name + notification selected on both phones)

---

### Sprint 24 — Silence competing sounds

<!-- parallel_exception: /allideas backlog -->

> **Sprint 24** archived in COMPLETED_TASKS.md (2026-08-30).

---

### Sprint 25 — Golden Path feedback pack

<!-- parallel_exception: /allideas backlog -->

> **Sprint 25** archived in COMPLETED_TASKS.md (2026-08-30).

---

### Sprint 26 — Sun and moon alarms

<!-- parallel_exception: /allideas backlog -->

> **Sprint 26** archived in COMPLETED_TASKS.md (2026-08-30).

---

### Sprint 27 — Hear quality

<!-- parallel_exception: /allideas backlog; decompose at /feature; skipped 9, 12, 14 -->

> **Sprint 27** archived in COMPLETED_TASKS.md (2026-08-30).

---

### Sprint 28 — Calls and telephony

<!-- parallel_exception: /allideas backlog; telephony schema Sequential before Parallel -->

> **Sprint 28** archived in COMPLETED_TASKS.md (2026-08-30).

---

### Sprint 29 — Time, calendar, and extras

<!-- parallel_exception: /allideas backlog; skipped 21 (next-alarm shout) -->

> **Sprint 29** archived in COMPLETED_TASKS.md (2026-08-30).

---

### Sprint 30 — Places and device states

<!-- parallel_exception: /allideas backlog; FOSS map + fences share place schema -->

> **Sprint 30** archived in COMPLETED_TASKS.md (2026-08-30).

---

### Sprint 31 — History and backup

<!-- parallel_exception: /allideas backlog; history PII rules stay on -->

> **Sprint 31** archived in COMPLETED_TASKS.md (2026-08-30).

---

### Sprint 32 — Accessibility and i18n

<!-- parallel_exception: /allideas backlog; overlay files stay under 300 lines -->

> **Sprint 32** archived in COMPLETED_TASKS.md (2026-08-30).

---

### Sprint 33 — Distribution and updates

<!-- parallel_exception: /allideas backlog -->

> **Sprint 33** archived in COMPLETED_TASKS.md (2026-08-30).

---

### Sprint 34 — FOSS integrations

<!-- parallel_exception: /allideas backlog; no GMS Wear stack -->

> **Sprint 34** archived in COMPLETED_TASKS.md (2026-08-30).

---

### Sprint 35 — UX polish

<!-- parallel_exception: /allideas backlog -->

> **Sprint 35** archived in COMPLETED_TASKS.md (2026-08-30).

---

> **Spoken clock + call cancel** archived in COMPLETED_TASKS.md.

---

### Sprint 36 — Template upgrade catch-up (1.5.1 → 1.12.0)

<!-- parallel_exception: sequential canon/mixed copy; sacred human-only -->

Named gaps from `/upgrade` Plan (compare base `v1.5.0...v1.12.0`).

<!-- ux-inventory:begin -->
<!-- ux-inventory:end -->

### Sequential work

- ✅ [AGENT][LOCAL] (#1) Canon: Agent Mode pack — lean/balanced/speed/mode/status commands, `agent-mode.mdc`, `set-agent-mode.sh`, `agent_mode.py`
- ✅ [AGENT][LOCAL] (#2) Canon: Cursor commands (1.6–1.11) — ux/brand/shape/resume/a11y/ui/upgrade/tour/etc.
- ✅ [AGENT][LOCAL] (#3) Canon: Cursor rules updates (`ux-ui`, `design-system`, `brief-replies`, batch/core/local-compute, …)
- ✅ [AGENT][LOCAL] (#4) Canon: Scripts bulk — `scripts/` + `scripts/lib/` from upstream 1.12.0
- ✅ [AGENT][LOCAL] (#5) Canon: Help + mode docs — `docs/help/**`, `docs/BATCH_COMMANDS.md`, `docs/CURSOR_MODES.md`
- ✅ [AGENT][LOCAL] (#6) Canon: Adapter re-sync after AGENTS merge — `bootstrap-lifecycle.sh --sync-adapters`
- ✅ [AGENT][LOCAL] (#7) Mixed: CI workflows merge — keep OpenShouter Android job shape; HUMAN review before push
- 🔲 [HUMAN] (#7) Review merged `.github/workflows/` Android CI shape before push
- ✅ [AGENT][LOCAL] (#8) Mixed: Linguist attribution — merge `templates/linguist/child.gitattributes` into `.gitattributes`; wire check into feature-gate
- ✅ [AGENT][LOCAL] (#9) Mixed: Config merge — `bootstrap.config.json`, `TEMPLATE_INDEX.json`, `.gitignore`, `.pre-commit-config.yaml` (keep child values)
- ✅ [AGENT][LOCAL] (#10) Mixed: Copy ADRs `docs/adr/0007`–`0010` if missing
- ✅ [AGENT][LOCAL] (#11) Mixed: Agent playbook docs merge (`PREFERENCES`, `START_HERE`, `BEST_PRACTICES`, `FEATURE_MODULES`, `UPGRADING`, shape/skills/MCP)
- ✅ [AGENT][LOCAL] (#12) Mixed: Skills / hooks / `agent-mode.json`
- ✅ [AGENT][LOCAL] (#13) Mixed: Branding pack `branding/**` (product brand stays OpenShouter)
- ✅ [AGENT][LOCAL] (#14) Mixed: Stamp `.template-version` to `1.12.0` after gates pass — scope: `.template-version`
- ✅ [HUMAN] (#15) Sacred: Merge session rules + Agent Mode pointers into `AGENTS.md` (no blind overwrite) — agent applied careful merge; HUMAN confirm before push
- ✅ [AGENT][LOCAL] (#16) Sacred: Confirm root `AGENT_MEMORY.md` retained (no `.agent/` migration)
- 🔲 [HUMAN] (#17) Sacred: Optional review of `docs/spec.md` / `docs/plan.md` / `docs/INITIALIZATION_PROMPT.md` / `.env` (no agent overwrite)
- ✅ [AGENT][LOCAL] (#18) Sacred: Skip `examples/` upstream deltas (OpenShouter app sacred)
- ✅ [AGENT][LOCAL] (#19–23) Mixed: Extend `feature-catalog.json` android detect globs for `org/openshouter/**` (already shipped; no `/feature` implement)

### Human & device (after automation)

- 🔲 [HUMAN] Approve CI workflow merge (#7) and AGENTS.md merge (#15) before `/push`

---

## Ongoing Maintenance (recurring)

Not a checklist. Monday cron / `/maintain` owns standing chores; do not park open AGENT/AUTO task rows here.

### Weekly

- Standing AUTO chore (not a board row): `check-security-triage.sh --wait-ci 300` (Dependabot + CI + Scorecard)
- Standing AGENT chore (not a board row): Apply Dependabot bumps; triage Scorecard SARIF findings
- Standing AUTO chore (not a board row): CI matrix + Repo Hygiene + Feature Gate green on `main`

### Monthly

- Standing AUTO chore (not a board row): `simulate-template-upgrade.sh`
- Standing AUTO chore (not a board row): `check-license-compliance.sh` + SBOM on latest release

### Pre-release (every version)

- Standing AUTO chore (not a board row): `pre-release-gate.sh`
- Standing HUMAN chore (not a board row): Approve release tag when product-ready

---

## Archived Sprints

| Sprint | Status | Archive |
| ------ | ------ | ------- |
| Sprint Audit leftover F-009 | Complete (HUMAN automation) | `COMPLETED_TASKS.md` |
| Sprint 0 first-push CI | Complete (AUTO) | `COMPLETED_TASKS.md` |
| Sprint 16 donate smoke | Complete (HUMAN automation) | `COMPLETED_TASKS.md` |
| Sprint Audit — 2026-08-23 | Complete (AGENT) | `COMPLETED_TASKS.md` |
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
| /ship v0.3.0 | Complete | `COMPLETED_TASKS.md` |
| Sprint 16 — Donations + GitHub product updates | Complete (AGENT/AUTO) | `COMPLETED_TASKS.md` |
| /ship v0.4.0 | Complete | `COMPLETED_TASKS.md` |
| Sprint 17 — Hear-quality extras (ideas 1–8) | Complete (AGENT) | `COMPLETED_TASKS.md` |
| /ship v0.5.0 | Complete | `COMPLETED_TASKS.md` |
| Sprint 18 — Import classic Shouter apps | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 19 — Shouter setting transfer parity | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 20 — Clickable history mute | Complete (AGENT/AUTO) | `COMPLETED_TASKS.md` |
| /ship v0.10.0 | Complete | `COMPLETED_TASKS.md` |
| /ship v0.11.0 | Complete | `COMPLETED_TASKS.md` |
| Sprint 20 leftover ADB (history mute on CPH2583) | Complete | `COMPLETED_TASKS.md` |
| Sprint 21 — Call repeat once vs until answered | Complete (AGENT/AUTO) | `COMPLETED_TASKS.md` |
| /ship v0.12.0 | Complete | `COMPLETED_TASKS.md` |
| Sprint 22 — Record shouts in announcement history | Complete (AGENT/AUTO) | `COMPLETED_TASKS.md` |
| /ship v1.0.0 | Complete | `COMPLETED_TASKS.md` |
| Sprint 23 — App name cooldown per shout channel | Complete (AGENT/AUTO) | `COMPLETED_TASKS.md` |
| Sprint 24 — Silence competing sounds | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 25 — Golden Path feedback pack | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 26 — Sun and moon alarms | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 27 — Hear quality | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 28 — Calls and telephony | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 29 — Time, calendar, and extras | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 30 — Places and device states | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 31 — History and backup | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 32 — Accessibility and i18n | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 33 — Distribution and updates | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 34 — FOSS integrations | Complete (AGENT) | `COMPLETED_TASKS.md` |
| Sprint 35 — UX polish | Complete (AGENT) | `COMPLETED_TASKS.md` |
| /ship v1.1.0 | Complete | `COMPLETED_TASKS.md` |
| /ship v0.6.0 | Complete | `COMPLETED_TASKS.md` |
| /ship v0.7.0 | Complete | `COMPLETED_TASKS.md` |
| /ship v0.8.0 | Complete | `COMPLETED_TASKS.md` |
| Sprints 9–14 ADB QA (CPH2655) | Complete | `COMPLETED_TASKS.md` |
| Sprints 0–13 HUMAN confirmations | Complete | `COMPLETED_TASKS.md` |
| Sprint 11 app speak list | Complete | `COMPLETED_TASKS.md` |
| Template maintainer sprints (v0.9.0–v0.17.0) | Complete (upstream) | `COMPLETED_TASKS.md` (provenance from agent-project-bootstrap) |
