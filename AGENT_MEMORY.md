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
| Template | agent-project-bootstrap | 0.17.0 scaffold; standards 0.21.0; product 0.10.0 | Product semver stays in `.template-version` |
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
- ✅ Dependency vulnerability scanning green on GitHub (`origin/main` CI + Security Scan + CodeQL)
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
| 2026-08-15 | `/ship` v0.2.0 | feat + CI/CodeQL KB-013 fixes; Release Please #3; APK + SBOM on GitHub Release | About tests must not require gitignored live assets |
| 2026-08-16 | `/ship` v0.2.1 | TTS WAV playback, silent/vibrate honor, exact-alarm setup, back nav, 12/24 time, unknown digits; RP #4 admin-merged | PR workflow runs stay `action_required` with empty jobs; use admin merge |
| 2026-08-16 | `/ship` v0.2.2 | Sectioned home/announcer/voice menu cards; RP #6 admin-merged | Same empty-job PR checks as KB-016 |
| 2026-08-16 | Sprints 9–14 ADB on CPH2655 | UI walk + Room + AlarmManager + logcat; TIME_SHOUT and REMINDER_FIRE armed | No su: live RING/IDLE and g-meter motion not injected |
| 2026-08-16 | `/build` HUMAN automation | 13 HUMAN rows + gitignored `release_repo`; archived to COMPLETED_TASKS | Sprint 0 ❌ AUTO first-push CI; CI AGENT KB-013/014 stay in HUMAN_BACKLOG |
| 2026-08-16 | `/ship` v0.2.3 | TTS ALARM fallback + per-utterance WAV; submenu cards + scroll restore; RP #8 admin-merged | Same empty-job PR checks as KB-016 |
| 2026-08-18 | `/ship` v0.2.4 | Silent/DND opt-in; no ALARM fallback; RP #10 admin-merged | Same empty-job PR checks as KB-016 |
| 2026-08-20 | `/build` Sprint 15 | Spanish pack, calendar, `%sim`, Bluetooth; HUMAN copy + ADB getprop | Live calendar/BT/dual-SIM shouts still need a human on-device walk |
| 2026-08-20 | `/ship` v0.3.0 | `feat` → minor; RP #14 admin-merged; SBOMs on the GitHub Release | Keep `CITATION.cff` in Release Please extra-files |
| 2026-08-20 | Sprint 16 donate/updates | Continuum-style Venmo + daily APK-filename GitHub check | Release assets must be `openshouter-X.Y.Z-foss.apk` |
| 2026-08-21 | `/ship` v0.4.0 | `feat` → minor; RP #16 admin-merged | HUMAN donate/update device smoke still open; name APKs `openshouter-X.Y.Z-foss.apk` |
| 2026-08-21 | Sprint 17 hear-quality | Gate misses in history; setup apps; test-notif bypass; REPEAT collapse; importance + Priority DND; calendar look-ahead; French | Live ADB walk still needed; silent places still mute unless left |
| 2026-08-21 | `/ship` v0.5.0 | `feat` → minor; RP #17 admin-merged; Sprint 16 leftover `parallel_exception` | HUMAN donate/update smoke still open; name APKs `openshouter-X.Y.Z-foss.apk` |
| 2026-08-22 | `/ship` v0.6.0 | `feat` → minor; RP #19 admin-merged; classic Shouter import + LEVEL | HUMAN donate/update smoke still open; name APKs `openshouter-X.Y.Z-foss.apk` |
| 2026-08-22 | Signed FOSS APK on `/ship` | Local debug-key `publish-foss-apk`; uploaded `openshouter-0.6.0-foss.apk` | Never upload CI-signed APKs; same debug key as prior sideloads |
| 2026-08-23 | `/audit` | Session-state gitignore; backup zip/rule caps; TTS format clamp; privacy + parity docs | HUMAN leftovers later automated the same day |
| 2026-08-23 | HUMAN leftover automation | Dismissed 16 code-scanning FPs; ProductUpdateTest donate smoke; first-push CI green | Leftover Scorecard: 66 pinned-deps + BinaryArtifacts/CodeReview/Maintained |
| 2026-08-23 | `/ship` v0.7.0 | `feat` → minor; RP #20 admin-merged; search-icon picker + backup caps | Weekly Scorecard pinned-deps remain; name APKs `openshouter-X.Y.Z-foss.apk` |
| 2026-08-23 | `/ship` v0.8.0 | `feat` → minor; RP #22 admin-merged; voice quality dropdowns + engine catalog | Instrumented settings test must open/dismiss the theme dropdown; name APKs `openshouter-X.Y.Z-foss.apk` |
| 2026-08-24 | Hourly shout vs bedtime | Dropped `setAlarmClock`; TIME_TICK + exact-while-idle; notification stream unchanged | Re-enable bedtime once after install if it already exited |
| 2026-08-24 | `/ship` v0.8.1 | `fix` → patch; RP #23 admin-merged; signed `openshouter-0.8.1-foss.apk` on release | Same empty-job PR checks as KB-016 |
| 2026-08-24 | `/ship` v0.8.2 | Android Auto auto-media + Voice settings note; RP #25 admin-merged | Run `publish-foss-apk` only after `git pull` post-merge |
| 2026-08-24 | Release signing | Gradle release keystore; no debug apksigner pass | Back up `openshouter-release.keystore`; uninstall debug builds before first release-signed install |
| 2026-08-24 | `/ship` v0.9.0 | Military/Zulu time TTS + release signing; RP #26 → 0.9.0 | Sync `versionName` in build.gradle.kts after RP merge |
| 2026-08-26 | `/ship` v0.9.1 | Call RINGING/WhatsApp shout fix; RP #27 admin-merged; tag created after merge-commit workflows queued | Bump `versionCode` before `publish-foss-apk` so sideloads replace 0.9.0 |
| 2026-08-28 | Sprint 20 history mute | Tap history row: OpenShouter app shout toggle + channel settings intent with highlight extras | ADB walk still needed on a device |
| 2026-08-28 | `/ship` v0.10.0 | History mute + required-check rollups; PR #30 and RP #32 merged without admin | Signed APK deferred to 0.11.0; ADB history-dialog smoke still open |
| 2026-08-28 | `/ship` v0.11.0 | Settings chrome #31 + RP #35; signed `openshouter-0.11.0-foss.apk` (versionCode 19) | ADB history-dialog smoke still open |
## Template Provenance

- **Source template:** `edwardlthompson/agent-project-bootstrap`
- **Bootstrap scaffold / standards:** `0.17.0` / `0.21.0` (not product semver)
- **Product version:** `0.11.0` (see `.template-version`)
- **Last update check:** See `.template-update.json`
- **Stack selection:** `.cursor/stack-selection.json` (`android`, `foss`, pruned)
