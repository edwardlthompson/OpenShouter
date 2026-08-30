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

<!-- parallel_exception: single feature container; one agent sequential slice -->

Feature spec: `docs/features/silence-competing-sounds.md`

### Parallelization

| Agent | Scope | Status |
|-------|-------|--------|
| Logic + tests | `silence/`, `data/SoundLeakStore.kt` | Sequential (same wiring) |
| View + i18n | `ui/silence/`, `strings_silence.xml`, Welcome/Dashboard | Sequential |
| Wiring | Room v7, listener, AnnouncerService, manifest | Sequential |

`agent_count_target`: 1 (overlapping listener + Room + Welcome)

- ✅ [AGENT] Feature spec + silent WAV pack + MediaStore installer + leak policy tests
- ✅ [AGENT] Welcome silence wizard + Dashboard Silence pane + leak list
- ✅ [AGENT] Optional WRITE_SETTINGS default notification/ringtone
- ✅ [AGENT] API 31+ notification-usage audio-session hint (`OWN_AUDIO`)
- 🔲 [ADB] On CPH2583 / CPH2655 (sideloaded 1.2.0 versionCode 29): LineageOS None clears Default sound leak rows; install OpenShouter Silent, confirm a custom-channel ding is listed and the channel page opens
- 🔲 [HUMAN] Confirm ColorOS Silent-still-dings workaround on both phones

---

### Sprint 25 — Golden Path feedback pack (1–5)

<!-- parallel_exception: coupled sanitizer + crash queue + dialogs; one sequential slice -->

Feature specs: `docs/features/crash-capture.md`, `docs/features/feedback.md`, `docs/features/github-feedback.md`, `docs/features/privacy-report.md`, `docs/features/settings.md`

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | `SanitizeReport.text(null)` → `""`; Open GitHub disabled when preview blank (`FeedbackPreview.canSubmit`) |
| Network timeout | N/A — sanitizer/crash queue are local; GitHub open is an https Intent only |
| Race | Single pending-crash file; `CrashCapture.install` is one-shot; toggle-off deletes the file |
| Unhandled exceptions | `runCatching` on persist/handler/Intent; write failure drops the record |
| PII in logs | Never log crash text, stacks, or report bodies |
| Package | Logic lives in `org.openshouter.*`, not a Golden Path stub overwrite |

### Parallelization

| Agent | Scope | Status |
|-------|-------|--------|
| Privacy + crash + URL logic | `privacyreport/`, `crashcapture/`, `githubfeedback/` | Sequential (shared sanitize) |
| Feedback UI + settings toggle | `feedback/`, `ui/feedback/`, Settings/About | Sequential |
| Wiring | `OpenShouterApp`, `GoldenPathApp` / `GoldenPathScreen` | Sequential |

`agent_count_target`: 1

- ✅ [AGENT] privacy-report sanitizer, fingerprint, markdown + unit tests
- ✅ [AGENT] crash-capture opt-in queue + Application install
- ✅ [AGENT] feedback dialogs + About Report a bug / Request a feature
- ✅ [AGENT] github-feedback issue-form URLs + clipboard fallback
- ✅ [AGENT] Settings leftover: Save crash details toggle (default off)
- 🔲 [ADB] Toggle on, force a test crash, confirm one sanitized review dialog and no auto-GitHub
- 🔲 [HUMAN] Confirm About Copy / Open GitHub / Discard on a real device

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
| /ship v1.1.0 | Complete | `COMPLETED_TASKS.md` |
| /ship v0.6.0 | Complete | `COMPLETED_TASKS.md` |
| /ship v0.7.0 | Complete | `COMPLETED_TASKS.md` |
| /ship v0.8.0 | Complete | `COMPLETED_TASKS.md` |
| Sprints 9–14 ADB QA (CPH2655) | Complete | `COMPLETED_TASKS.md` |
| Sprints 0–13 HUMAN confirmations | Complete | `COMPLETED_TASKS.md` |
| Sprint 11 app speak list | Complete | `COMPLETED_TASKS.md` |
| Template maintainer sprints (v0.9.0–v0.17.0) | Complete (upstream) | `COMPLETED_TASKS.md` (provenance from agent-project-bootstrap) |
