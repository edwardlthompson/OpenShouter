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

1. 🔲 [AUTO] After first push to `main`: `check-github-ci.sh --wait 300` (CI + Security Scan + CodeQL)
2. 🔲 [HUMAN] Run `scripts/setup-github-repo.sh` / `.ps1` after the GitHub remote exists

### Human & device (after automation)

> Address after `/build` completes AGENT/AUTO work above. `/build` attempts each row via automation; failures land in `HUMAN_BACKLOG.md`.

1. 🔲 [HUMAN] Create GitHub repo, set remote, and enable Dependabot alerts + security updates + private vulnerability reporting (`docs/SECURITY_TRIAGE.md`)
2. 🔲 [HUMAN] Branch protection on `main`: required checks CI, Security Scan, CodeQL, Repo Hygiene, Feature Gate; linear history; no force-push
3. 🔲 [HUMAN] Paste `docs/GITHUB_ABOUT.md` into GitHub → Settings → General → About
4. 🔲 [HUMAN] Fill `.app-update.json` `release_repo` and `donations.json` links (or keep donations disabled)
5. 🔲 [HUMAN] Approve ADR-0001 and ADR-0002

---

### Sprint 1 — Android Gradle scaffold

<!-- parallel_exception: AGENT Gradle scaffold archived to COMPLETED_TASKS.md -->

> AGENT/ADB rows archived in COMPLETED_TASKS.md @ `247faf2`.

### Sequential (must complete in order)

1. 🔲 [HUMAN] Approve package name, SDK targets, and DI choice (Hilt) per ADR-0001

### Human & device (after automation)

1. 🔲 [HUMAN] Confirm FOSS deps (no Play Services / Firebase) in Gradle manifests

---

### Sprint 2 — TTS engine + notification listener

<!-- parallel_exception: AGENT TTS/listener work archived to COMPLETED_TASKS.md -->

> AGENT/ADB rows archived in COMPLETED_TASKS.md @ `247faf2`.

### Sequential (must complete in order)

1. 🔲 [HUMAN] Approve default format string `%app: %title - %text`

---

### Sprint 6 — FOSS geofencing (no Play Services)

<!-- parallel_exception: AGENT geofence work archived to COMPLETED_TASKS.md -->

> AGENT/ADB rows archived in COMPLETED_TASKS.md @ `247faf2`.

### Sequential (must complete in order)

1. 🔲 [HUMAN] Confirm background location UX copy and privacy disclosure

---

### Sprint 7 — Compose settings UI

<!-- parallel_exception: AGENT settings UI archived to COMPLETED_TASKS.md -->

> AGENT/ADB rows archived in COMPLETED_TASKS.md @ `247faf2`.

### Sequential (must complete in order)

1. 🔲 [HUMAN] Copy review for permission rationales

---

### Sprint 9 — Notification TTS quality + unused settings UI

<!-- agent_count_target: 4 -->

> Inventory: `docs/features/parity-matrix.md`. Unlock DataStore/Room that already exists, then match Voice Notify playback/filter axes. **Do not start until this plan is approved in chat.**

### Sequential (must complete in order)

1. 🔲 [AGENT] Lock `TtsPlaybackPolicy` (stream, delaySeconds, maxLength, audioFocus, speakEmojis, repeatMinutes) and `DeviceStatePolicy` (screen on/off, headset on/off, silent/vibrate, inCall) in `domain/`; persist on `AppSettings` + DataStore
2. 🔲 [AGENT] Apply both policies in `AnnouncementGate` + `TtsController` before every speak (including call/power)

### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| Quiet-hours start/end/day picker UI | AGENT | `examples/android/app/src/main/java/org/openshouter/ui/quiet/` |
| History viewer + clear (package + time; spoken text toggle) | AGENT | `examples/android/app/src/main/java/org/openshouter/ui/history/` |
| Regex / ignore-require / empty-group-repeat filter UI | AGENT | `examples/android/app/src/main/java/org/openshouter/ui/filters/` |
| TTS playback settings + voice test + system TTS shortcut | AGENT | `examples/android/app/src/main/java/org/openshouter/ui/tts/` |
### Human & device (after automation)

1. 🔲 [ADB] Quiet-hours custom window suppresses; history lists without leaking payloads to logcat
2. 🔲 [ADB] Test notification speaks with delay/max-length; shake threshold change interrupts
3. 🔲 [HUMAN] Approve ADR-0003 (message via notifications, no `READ_SMS`)

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty format or regex | Reject blank; `RegexFilter.MAX_PATTERN`; unit tests |
| Network timeout | N/A — local settings only |
| Race TTS vs call loop | Single `TtsController`; call still flush-queue |
| Unhandled TTS/regex errors | `runCatching` skip event |
| History PII | UI default: package + timestamp; no logcat of title/text |
### Parallelization

- Sequential lock: `TtsPlaybackPolicy`, `DeviceStatePolicy`, DataStore keys
- `agent_count_target`: 4
- Dry-run: four non-overlapping `ui/{quiet,history,filters,tts}/` AGENT rows

---

### Sprint 10 — Shouter shout channels

<!-- agent_count_target: 4 -->

> Time, battery phrase UI, missed call, message-via-notifications, reminders. Requires Sprint 9 policies.

### Sequential (must complete in order)

1. 🔲 [AGENT] Lock `TimeShoutSchedule`, `ReminderEntity`, `MessageChannelPolicy`, `MissedCallPolicy` (no SMS permissions)
2. 🔲 [AGENT] Shared `AlarmScheduler` adapter (inexact default; exact opt-in)

### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| Interval time announcer + format | AGENT | `examples/android/app/src/main/java/org/openshouter/time/` |
| Voice reminders + optional notification | AGENT | `examples/android/app/src/main/java/org/openshouter/reminder/` |
| Message channel (notification extras + contacts) | AGENT | `examples/android/app/src/main/java/org/openshouter/message/` |
| Missed-call shout + unknown-number / contact rules | AGENT | `examples/android/app/src/main/java/org/openshouter/missed/` |
### Human & device (after automation)

1. 🔲 [ADB] Time shout at interval; missed call after RING→IDLE; SMS app notification uses Message rules
2. 🔲 [HUMAN] Approve `SCHEDULE_EXACT_ALARM` opt-in copy if “Announce accurately” ships
3. 🔲 [ADB] Battery situation toggles + custom phrases without logging percents next to identity

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty reminder text | Require non-blank; skip empty TTS |
| Alarm Doze / timeout | Inexact alarms by default; exact is opt-in + [HUMAN] copy |
| Race reminder vs notification TTS | Same `TtsController` |
| Unhandled AlarmManager | Catch and skip; no crash |
| SMS permission creep | ADR-0003 — listener only |
### Parallelization

- Sequential lock: schedule/reminder/message/missed types + `AlarmScheduler`
- `agent_count_target`: 4
- Dry-run: `time/`, `reminder/`, `message/`, `missed/`

---

### Sprint 11 — Per-app overrides + backup

<!-- agent_count_target: 2 -->

> **App speak list** archived in COMPLETED_TASKS.md @ `247faf2`.

### Sequential (must complete in order)

1. 🔲 [AGENT] Lock backup file allowlist (DataStore + `app_speak_rules` only; exclude history payloads)

### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| Per-app override editor | AGENT | `examples/android/app/src/main/java/org/openshouter/ui/overrides/` |
| Settings backup/restore zip (exclude history text) | AGENT | `examples/android/app/src/main/java/org/openshouter/backup/` |
### Human & device (after automation)

1. 🔲 [ADB] Searchable app list; App name only vs Notification vs both; unlisted apps silent
2. 🔲 [ADB] Backup zip restores toggles; history payloads absent from zip
3. 🔲 [HUMAN] Confirm QUERY_ALL_PACKAGES Play-policy N/A (GitHub Releases only)

### Critique

| Issue | Resolution |
|-------|------------|
| Null override fields | `merge(global, override)` treats null as inherit; tests |
| Network timeout | N/A — SAF URI local file |
| Race picker vs package install | Reload list on resume |
| Unhandled zip I/O | Typed error snackbar; leave DB closed/reopened like VN pattern |
| History in backup | Sequential lock: DataStore + `app_speak_rules` only |
### Parallelization

- Sequential lock: backup file allowlist
- `agent_count_target`: 2
- Dry-run: `ui/overrides/`, `backup/`

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
| Sprint 11 app speak list | Complete | `COMPLETED_TASKS.md` |
| Template maintainer sprints (v0.9.0–v0.17.0) | Complete (upstream) | `COMPLETED_TASKS.md` (provenance from agent-project-bootstrap) |
