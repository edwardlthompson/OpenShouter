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
2. 🔲 [HUMAN] Run `scripts/setup-github-repo.sh` / `.ps1` after the GitHub remote exists

### Human & device (after automation)

> Address after `/build` completes AGENT/AUTO work above. `/build` attempts each row via automation; failures land in `HUMAN_BACKLOG.md`.

1. 🔲 [HUMAN] Create GitHub repo, set remote, and enable Dependabot alerts + security updates + private vulnerability reporting (`docs/SECURITY_TRIAGE.md`)
2. 🔲 [HUMAN] Paste `docs/GITHUB_ABOUT.md` into GitHub → Settings → General → About

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

<!-- parallel_exception: AGENT TTS quality + settings UI archived to COMPLETED_TASKS.md -->

> **Sprint 9** AGENT/HUMAN archived in COMPLETED_TASKS.md @ `2ea76a1`.

### Human & device (after automation)

1. 🔲 [ADB] Quiet-hours custom window suppresses; history lists without leaking payloads to logcat
2. 🔲 [ADB] Test notification speaks with delay/max-length; shake threshold change interrupts

---

### Sprint 10 — Shouter shout channels

<!-- parallel_exception: AGENT shout-channel work archived to COMPLETED_TASKS.md -->

> **Sprint 10** AGENT archived in COMPLETED_TASKS.md (2026-08-14).

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

<!-- parallel_exception: AGENT override + backup work archived to COMPLETED_TASKS.md -->

> **Sprint 11** AGENT archived in COMPLETED_TASKS.md (2026-08-14). App speak list @ `247faf2`.

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

### Sprint 12 — Close notification + TTS quality gaps

<!-- parallel_exception: AGENT notification/TTS quality work archived to COMPLETED_TASKS.md -->

> **Sprint 12** AGENT archived in COMPLETED_TASKS.md (2026-08-14).

### Human & device (after automation)

1. 🔲 [ADB] Quiet-hours 24-hour grid + RESET/PRESET; shake threshold interrupts; test notification posts without PII in logcat
2. 🔲 [ADB] Empty/group/repeat skips; ignore-reason column shows enum only
3. 🔲 [HUMAN] Confirm OEM autostart copy for vendor Settings intents (no extra SDKs)

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty notification or ignore reason | `NotificationPolicy` skips blank; `IgnoreReason.NONE` default; tests in `domain/` |
| Network timeout | N/A — OEM intent and TTS are local |
| Race repeat-loop vs new notification | Single `TtsController` queue; screen-on cancels loop |
| Unhandled TTS/AlarmManager | Catch and skip; no crash |
| History PII | Ignore-reason enum only; never log title/text/spoken |
### Parallelization

- Sequential lock: `NotificationPolicy`, `IgnoreReason`, `ShakeThreshold`, `TtsVoice`, extra tokens, `RepeatCount`
- `agent_count_target`: 8
- Dry-run: `oem/`, `gesture/`, `ui/quiet/`, `notification/`, `tts/`, `ui/tts/`, `ui/history/`, `ui/filters/`

---

### Sprint 13 — Finish shout channels + backup + overrides

<!-- parallel_exception: AGENT shout-channel + backup work archived to COMPLETED_TASKS.md -->

> **Sprint 13** AGENT archived in COMPLETED_TASKS.md (2026-08-14).

### Human & device (after automation)

1. 🔲 [ADB] Nick/blacklist skips without logging numbers; unknown-number toggles on call and message
2. 🔲 [ADB] Reminder alarm speaks + optional notification; SAF zip restores toggles and excludes history
3. 🔲 [ADB] Battery situation phrases fire without logging percent next to identity
4. 🔲 [HUMAN] Confirm `SCHEDULE_EXACT_ALARM` copy still covers reminder exact opt-in

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty nick, format, or reminder text | Reject blank; inherit global override when null; skip empty TTS |
| Network timeout | N/A — SAF URI is local |
| Race reminder vs notification TTS | Same `TtsController`; reminder uses `QUEUE_ADD` |
| Unhandled AlarmManager / zip I/O | Catch and skip; typed snackbar; leave DB closed/reopened |
| SMS permission creep | ADR-0003 — listener extras only; no `READ_SMS` |
| Contact / battery PII | Never log numbers, names, or percent next to identity |
### Parallelization

- Sequential lock: `ContactRule`, `ChannelDeviceState`, full `AppOverride`, `BatterySituation`, `ReminderReceiver`
- `agent_count_target`: 8
- Dry-run: `contacts/`, `call/`, `message/`, `time/`, `power/`, `reminder/`, `backup/`, `ui/overrides/`

---

### Sprint 14 — Close remaining Partial rows

<!-- parallel_exception: AGENT parity close-out archived to COMPLETED_TASKS.md -->

> **Sprint 14** AGENT archived in COMPLETED_TASKS.md (2026-08-15).
> **/ship v0.2.1** archived in COMPLETED_TASKS.md @ `da4d76a`.

### Human & device (after automation)

1. 🔲 [ADB] Live shake g-meter moves with the device; language chips match `engine.availableLanguages`
2. 🔲 [ADB] Per-channel headphone/silent/stream/repeat apply to a call shout
3. 🔲 [ADB] Reminder day/week/month/year reschedules after fire

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty language list | Chips when `availableLanguages` is ready; BCP-47 field fallback if empty |
| Sensor leak | `DisposableEffect` unregisters the accelerometer listener |
| Stale channel map on toggle | `onSave(settings.channelStates + (channel to state))`; parent recomposes from DataStore |
| Reminder month/year | 30 / 365 day minutes; `ReminderInterval.nextAt` |
| File caps | Channel grid in `ui/channel/`; prefs stay under 150 |
### Parallelization

- Sequential lock: `ReminderInterval`, `SpokenEvent.stream`, `ChannelStates.spoken`, `TtsVoice.MAX_TAG`, `ShakeThreshold.gForce`
- `agent_count_target`: 5
- Dry-run: `gesture/`, `ui/channel/`, `ui/tts/`, `reminder/`, `call/`

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
| Sprint 11 app speak list | Complete | `COMPLETED_TASKS.md` |
| Template maintainer sprints (v0.9.0–v0.17.0) | Complete (upstream) | `COMPLETED_TASKS.md` (provenance from agent-project-bootstrap) |
