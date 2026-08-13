# Build Plan

> OpenShouter task board with owner labels. **Completed sprints:** `COMPLETED_TASKS.md`.
> Scaffolded from [agent-project-bootstrap](https://github.com/edwardlthompson/agent-project-bootstrap) v0.17.0.

## Owner Label Legend

| Label   | Owner           | When to use                                                |
| ------- | --------------- | ---------------------------------------------------------- |
| `AGENT` | Cursor Agent    | Code, docs, scaffolding, tests, CI config                  |
| `HUMAN` | Human developer | Approvals, credentials, GitHub settings, product decisions |
| `ADB`   | Human (Android) | Android SDK, emulator/device testing, F-Droid submission   |
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

## Sprint 0 — Bootstrap & identity

<!-- agent_count_target: 2 -->

### Sequential (must complete in order)

1. ✅ [AGENT] Copy agent-project-bootstrap v0.17.0 and run `init-project.ps1 -NonInteractive -Stack android -Prune -PruneOptional -DistributionTier foss`
2. ✅ [AGENT] Fill `branding/product.json` (`mode: product`), ADRs, `AGENT_MEMORY.md`, privacy/threat stubs, and this playbook
3. ✅ [AUTO] `validate-bootstrap.sh --quick` and `check-build-plan-parallel.sh` passed locally
3b. ❌ [AUTO] `feature-gate.sh --stack android` — JAVA_HOME not set (unblocks with JDK 17 in Sprint 1)
4. 🔲 [AUTO] After first push to `main`: `check-github-ci.sh --wait 300` (CI + Security Scan + CodeQL)
5. 🔲 [AGENT] Run `scripts/setup-github-repo.sh` / `.ps1` when `gh` is authenticated (or leave for HUMAN)

### Parallel (safe after Sequential step 2)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| F-Droid metadata copy | AGENT | `examples/android/metadata/` |
| Fastlane listing copy | AGENT | `examples/android/fastlane/` |

### Human & device (after automation)

> Address after `/build` completes AGENT/AUTO work above. `/build` attempts each row via automation; failures land in `HUMAN_BACKLOG.md`.

1. 🔲 [HUMAN] Create GitHub repo, set remote, and enable Dependabot alerts + security updates + private vulnerability reporting (`docs/SECURITY_TRIAGE.md`)
2. 🔲 [HUMAN] Branch protection on `main`: required checks CI, Security Scan, CodeQL, Repo Hygiene, Feature Gate; linear history; no force-push
3. 🔲 [HUMAN] Paste `docs/GITHUB_ABOUT.md` into GitHub → Settings → General → About
4. 🔲 [HUMAN] Fill `.app-update.json` `release_repo` and `donations.json` links (or keep donations disabled)
5. 🔲 [HUMAN] Approve ADR-0001 and ADR-0002

---

## Sprint 1 — Android Gradle scaffold

<!-- agent_count_target: 2 -->

> User implementation plan step 2. Rebrand Golden Path stub to OpenShouter packages. **No feature services yet.**

### Sequential (must complete in order)

1. 🔲 [AGENT] Draft/lock applicationId `org.openshouter`, minSdk 26, compile/targetSdk 35, namespace, and Hilt/Room/Compose catalog versions (schema lock)
2. 🔲 [HUMAN] Approve package name, SDK targets, and DI choice (Hilt) per ADR-0001
3. 🔲 [AGENT] Apply Gradle/manifest/permissions skeleton after approval

### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| App module Gradle + AndroidManifest permissions | AGENT | `examples/android/app/build.gradle.kts` |
| Root Gradle + libs catalog + wrapper pins | AGENT | `examples/android/gradle/` |
| Unit-test placeholders for new packages | AGENT | `examples/android/app/src/test/java/org/openshouter/` |

### Human & device (after automation)

1. 🔲 [ADB] `./gradlew assembleDebug test` on JDK 17
2. 🔲 [HUMAN] Confirm F-Droid-friendly deps (no Play Services / Firebase) in Gradle manifests

---

## Sprint 2 — TTS engine + notification listener

<!-- agent_count_target: 2 -->

### Sequential (must complete in order)

1. 🔲 [AGENT] Lock domain models: `SpokenEvent`, `TtsFormatString`, `AppFilterRule`, `RegexRule`, Room entities (schema lock)
2. 🔲 [AGENT] Implement `TextToSpeechManager` + `NotificationListenerService` against locked types

### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| TTS manager + format-string engine + unit tests | AGENT | `examples/android/app/src/main/java/org/openshouter/tts/` |
| Notification listener, filters, Room history | AGENT | `examples/android/app/src/main/java/org/openshouter/notification/` |
| TTS/notification unit tests | AGENT | `examples/android/app/src/test/java/org/openshouter/tts/` |

### Human & device (after automation)

1. 🔲 [ADB] Enable Notification Listener; verify spoken notification + history row
2. 🔲 [HUMAN] Approve default format string `%app: %title - %text`

---

## Sprint 3 — Looping caller ID announcer

<!-- agent_count_target: 2 -->

### Sequential (must complete in order)

1. 🔲 [AGENT] Lock `IncomingCallEvent` + start/stop loop contract (stop on OFFHOOK, IDLE, reject)
2. 🔲 [AGENT] Implement `CallAnnouncerService` using `TelephonyCallback` (API 31+) with `PhoneStateListener` fallback

### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| Telephony callback / phone state receiver | AGENT | `examples/android/app/src/main/java/org/openshouter/call/` |
| Contacts lookup (`ContactsContract`) | AGENT | `examples/android/app/src/main/java/org/openshouter/contacts/` |

### Human & device (after automation)

1. 🔲 [ADB] Incoming call from known contact loops TTS until answer/reject
2. 🔲 [ADB] Unknown number announces digits; loop stops on miss

---

## Sprint 4 — Battery, power, and mute gestures

<!-- agent_count_target: 2 -->

### Sequential (must complete in order)

1. 🔲 [AGENT] Lock `PowerEvent` + `MuteGesture` settings keys and interrupt API on `TextToSpeechManager`
2. 🔲 [AGENT] Wire receivers/sensors to the interrupt API

### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| Battery/power broadcast receiver + thresholds | AGENT | `examples/android/app/src/main/java/org/openshouter/power/` |
| Shake, flip-down, screen on/off mute sensors | AGENT | `examples/android/app/src/main/java/org/openshouter/gesture/` |

### Human & device (after automation)

1. 🔲 [ADB] Low battery, charger connect/disconnect, 100% / 15% thresholds speak as configured
2. 🔲 [ADB] Shake and face-down stop in-progress TTS

---

## Sprint 5 — Quiet hours and audio routing

<!-- agent_count_target: 2 -->

### Sequential (must complete in order)

1. 🔲 [AGENT] Lock `AnnouncementGate` (quiet hours, screen-off-only, headset/A2DP-only) as a pure function
2. 🔲 [AGENT] Apply gate before every TTS speak

### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| Quiet-hours schedule + day-of-week logic/tests | AGENT | `examples/android/app/src/main/java/org/openshouter/gate/` |
| Audio route detector (wired + Bluetooth A2DP) | AGENT | `examples/android/app/src/main/java/org/openshouter/audio/` |

### Human & device (after automation)

1. 🔲 [ADB] Quiet hours suppress announcements; headset-only mode silent on speaker

---

## Sprint 6 — FOSS geofencing (no Play Services)

<!-- agent_count_target: 2 -->

### Sequential (must complete in order)

1. 🔲 [AGENT] Lock `GeofenceRule` (lat/lng/radius, enter/exit, mode toggle) per ADR-0002
2. 🔲 [AGENT] Implement in-process geofence evaluator on `LocationManager` (not `play-services-location`)

### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| Location manager + fence evaluator + tests | AGENT | `examples/android/app/src/main/java/org/openshouter/geo/` |
| Saved places (Home/Work) persistence | AGENT | `examples/android/app/src/main/java/org/openshouter/places/` |

### Human & device (after automation)

1. 🔲 [ADB] Enter/exit Home fence toggles announcement mode
2. 🔲 [HUMAN] Confirm background location UX copy and privacy disclosure

---

## Sprint 7 — Compose settings UI

<!-- agent_count_target: 2 -->

### Sequential (must complete in order)

1. 🔲 [AGENT] Lock navigation graph: Dashboard, App Rules, Audio/TTS, Gestures, Quiet Hours, Places, About
2. 🔲 [AGENT] Wire screens to existing ViewModels (composition root stays small)

### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| Dashboard + permission status | AGENT | `examples/android/app/src/main/java/org/openshouter/ui/dashboard/` |
| App list rules + TTS format editor | AGENT | `examples/android/app/src/main/java/org/openshouter/ui/rules/` |
| Gesture / quiet-hours / audio settings | AGENT | `examples/android/app/src/main/java/org/openshouter/ui/settings/` |

### Human & device (after automation)

1. 🔲 [ADB] TalkBack pass on primary settings screens
2. 🔲 [HUMAN] Copy review for permission rationales

---

## Sprint 8 — Quick Settings tile, widget, foreground service

<!-- agent_count_target: 2 -->

### Sequential (must complete in order)

1. 🔲 [AGENT] Lock master `AnnouncerEnabled` DataStore key shared by tile, widget, and foreground service
2. 🔲 [AGENT] Implement persistent foreground notification + tile + widget against that key

### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
|------|-------|----------------|
| Foreground service + persistent notification | AGENT | `examples/android/app/src/main/java/org/openshouter/service/` |
| QS tile + home-screen widget | AGENT | `examples/android/app/src/main/java/org/openshouter/quicksettings/` |

### Human & device (after automation)

1. 🔲 [ADB] Tile and widget toggle master on/off; service survives app swipe-away on API 34+
2. 🔲 [ADB] Runtime permission flow: POST_NOTIFICATIONS, phone, contacts, location (fine then background)
3. 🔲 [HUMAN] F-Droid metadata + reproducible APK when product-ready

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
| Template maintainer sprints (v0.9.0–v0.17.0) | Complete (upstream) | `COMPLETED_TASKS.md` (provenance from agent-project-bootstrap) |
