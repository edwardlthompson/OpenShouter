# Agent Memory

> Centralized index of tech stack, threat models, persistent context, and retrospectives.
> Update only at session startups, milestone boundaries, or major architectural pivots.

## Tech Stack

| Layer | Technology | Version | Notes |
|-------|-----------|---------|-------|
| Platform | Android (Kotlin, Jetpack Compose, Material 3) | minSdk 26, targetSdk 35 | Child repo; Golden Path stub in `examples/android/` |
| Architecture | Clean Architecture + MVVM | ADR-0001 | Hilt DI, Room, DataStore, Coroutines/Flow |
| License | MIT | - | Pure FOSS; F-Droid-friendly |
| Distribution | GitHub Releases + F-Droid (planned) | - | No Play Core / in-app update SDKs |
| Template | agent-project-bootstrap | 0.17.0 | See `.template-version` |

## Active Modules

- ❌ Web / PWA (`modules/web/MODULE.md`)
- ❌ Python (`modules/python/MODULE.md`)
- ✅ Android / F-Droid (`modules/android/MODULE.md`)
- ❌ Node API (`modules/node/MODULE.md`)
- ❌ Lightroom Classic (`modules/lightroom/MODULE.md`)
- ❌ Rust (`modules/rust/MODULE.md`)
- ❌ Go (`modules/go/MODULE.md`)

## Threat Model Checklist

- ✅ `docs/THREAT_MODEL.md` drafted (STRIDE + MASVS; OpenShouter data flows)
- ✅ No proprietary closed-source SDKs in production path (ADR-0002: no Play Services location)
- ✅ Opt-in only telemetry (none planned); see `docs/PRIVACY.md`
- ✅ Secrets excluded from VCS (Gitleaks pre-commit)
- 🔲 Dependency vulnerability scanning green on GitHub (`[HUMAN]` first push)
- ✅ Input validation at all data boundaries (regex rules, TTS format strings)
- ✅ `SECURITY.md` present; private reporting is `[HUMAN]` GitHub setting

## Persistent Context

### Project Purpose

OpenShouter is a FOSS recreation/enhancement of classic Shouter (`com.bhkapps.shouter`) plus Voice Notify (`pilot51/voicenotify`) notification TTS: spoken notifications, looping caller ID, battery/power alerts, mute gestures, quiet hours, headset-only mode, and FOSS geofences.

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
- SMS/MMS reading (out of scope unless a later ADR adds it)

### Success metrics

- Feature parity with Shouter's hardware/telephony/automation set listed in BUILD_PLAN Sprints 2–8
- Notification TTS quality comparable to Voice Notify format strings + filters
- F-Droid inclusion blockers = 0 (no GMS)
- TalkBack-usable settings; master mute via QS tile in one tap

## Session Retrospectives

| Date | Milestone | What worked | What to improve |
|------|-----------|-------------|-----------------|
| 2026-08-13 | Sprint 0 bootstrap | Template clone + android prune + product identity | `gh` CLI missing locally; GitHub CI gate waits on HUMAN remote |

## Template Provenance

- **Source template:** `edwardlthompson/agent-project-bootstrap`
- **Template version:** `0.17.0` (see `.template-version`)
- **Last update check:** See `.template-update.json`
- **Stack selection:** `.cursor/stack-selection.json` (`android`, `foss`, pruned)
