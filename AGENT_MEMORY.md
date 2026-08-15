# Agent Memory

> Centralized index of tech stack, threat models, persistent context, and retrospectives.
> Update only at session startups, milestone boundaries, or major architectural pivots.

## Tech Stack

| Layer | Technology | Version | Notes |
|-------|-----------|---------|-------|
| Platform | Android (Kotlin, Jetpack Compose, Material 3) | minSdk 26, targetSdk 35 | Child repo; Golden Path stub in `examples/android/` |
| Architecture | Clean Architecture + MVVM | ADR-0001 | Hilt DI, Room, DataStore, Coroutines/Flow |
| License | MIT | - | Pure FOSS; no Play Services |
| Distribution | GitHub Releases only | - | No Play Store, no F-Droid listing, no Play Core |
| Template | agent-project-bootstrap | 0.17.0 scaffold; product 0.1.0 | See `.template-version` |
## Active Modules

- ❌ Web / PWA (`modules/web/MODULE.md`)
- ❌ Python (`modules/python/MODULE.md`)
- ✅ Android (`modules/android/MODULE.md`) — GitHub Releases only
- ❌ Node API (`modules/node/MODULE.md`)
- ❌ Lightroom Classic (`modules/lightroom/MODULE.md`)
- ❌ Rust (`modules/rust/MODULE.md`)
- ❌ Go (`modules/go/MODULE.md`)

## Threat Model Checklist

- ✅ `docs/THREAT_MODEL.md` drafted (STRIDE + MASVS; OpenShouter data flows)
- ✅ No proprietary closed-source SDKs in production path (ADR-0002: no Play Services location)
- ✅ Opt-in only telemetry (none planned); see `docs/PRIVACY.md`
- ✅ Secrets excluded from VCS (Gitleaks pre-commit)
- 🔲 Dependency vulnerability scanning green on GitHub (first push in progress)
- ✅ Input validation at all data boundaries (regex rules, TTS format strings)
- ✅ `SECURITY.md` present; private reporting is `[HUMAN]` GitHub setting

## Persistent Context

### Project Purpose

OpenShouter is a FOSS recreation/enhancement of classic Shouter (`com.bhkapps.shouter`) plus Voice Notify (`pilot51/voicenotify`) notification TTS. Pitch: hear chosen alerts without looking — for people who cannot see or read a screen, and for anyone who silences the phone to check it less. Spoken notifications, looping caller ID, battery/power alerts, mute gestures, quiet hours, headset-only mode, and FOSS geofences.

### Key Constraints

- Max 300 lines per static data file (UI + i18n), 150 lines per pure logic file
- Trunk-based development with Conventional Commits
- Strict type safety and test coverage budgets
- **No Google Play Services, Firebase, or closed telemetry** (`modules/android/MODULE.md`)
- Notification content, contacts, and locations stay on-device (Room / DataStore)
- Geofencing must use `LocationManager` + in-process fences (ADR-0002)

### Non-goals (Sprint 0)

- Play Store listing / Play Billing / Play In-App Updates
- Cloud sync, accounts, or remote logging of notification text
- Exact proprietary UI clone of legacy Shouter
- `READ_SMS` / SMS inbox APIs (Message shout uses the notification listener; ADR-0003)
- Placebook companion, Play/GMS analytics, Facebook/rate/share rows from Shouter Pro

### Success metrics

- Feature parity tracked in `docs/features/parity-matrix.md` (Sprints 9–11 after plan approval)
- Notification TTS quality comparable to Voice Notify format strings + filters
- Zero GMS / Firebase / Play Core dependencies
- TalkBack-usable settings; master mute via QS tile in one tap

## Session Retrospectives

| Date | Milestone | What worked | What to improve |
|------|-----------|-------------|-----------------|
| 2026-08-13 | Sprint 0 bootstrap | Template clone + android prune + product identity | GitHub CI gate waits on HUMAN remote |
| 2026-08-13 | Sprints 1–8 AGENT | Domain + services + Compose UI; unit tests + assembleDebug | HUMAN: repo/push/ADR |
| 2026-08-13 | ADB QA on CPH2583 | Rooted device: listener, history, call debug RING/IDLE, quiet hours, headset-only, geofence, TalkBack, FGS | Compose Espresso tests fail on API 36 InputManager; physical shake not injected |
| 2026-08-13 | `/audit` | Local gates green; F-001–F-004 fixed (backup, debug export, history prune, regex cap) | F-008 GitHub remote still HUMAN |
| 2026-08-13 | Parity inventory | Shouter Pro page dumps on CPH2583 + VN GitHub settings schema | Implement Sprints 9–11 only after human approves plan |
| 2026-08-13 | Branding + README pitch | Replaced Golden Path SVGs; accessibility + digital-quiet voice | PNG store/social exports still [HUMAN]/[ADB] |
| 2026-08-13 | `/cleanup` | Archived Sprints 3–5, 8 + Sprint 11 app-speak AGENT rows | HUMAN/ADB backlog stays 🔲; Sprint 11 overrides + backup remain |
| 2026-08-13 | Welcome + hourly shout | Setup permission buttons including battery/exact; setAlarmClock keep-alive | ADB: Unrestricted battery on CPH2583; hour-boundary speak |
| 2026-08-14 | `/build` Sprints 9–11 | TtsPlaybackPolicy, quiet hours, history/filters/TTS, shout channels, backup allowlist, overrides | Reminder alarms not scheduled; backup is cache zip; CI red until `/push` |
| 2026-08-14 | Parity closeout plan | Sprints 12–13 added to BUILD_PLAN for 100% in-scope No/Partial | Skip rows stay out (Placebook, GMS, Facebook, in-app language, READ_SMS) |
| 2026-08-15 | Sprint 14 Partial close-out | Live g-meter, language chips, channel grid, reminder intervals | ADB still needed for meter/chips/alarms on device |
## Template Provenance

- **Source template:** `edwardlthompson/agent-project-bootstrap`
- **Template version:** `0.17.0` (see `.template-version`)
- **Last update check:** See `.template-update.json`
- **Stack selection:** `.cursor/stack-selection.json` (`android`, `foss`, pruned)
