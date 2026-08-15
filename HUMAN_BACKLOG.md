# Human Backlog

> Items automation attempted during autonomous `/build` but could not complete. BUILD_PLAN rows stay open until a human finishes them.

| Deferred | Sprint | Owner | Task | Reason |
|----------|--------|-------|------|--------|
| 2026-08-14 | CI | AGENT | Gate Web/Node CI jobs and CodeQL JS on stack presence (KB-013) | First-push CI red on pruned stacks |
| 2026-08-14 | CI | AGENT | Grant or skip telephony in `connectedDebugAndroidTest` (KB-014) | Emulator `CallMonitor` SecurityException |
| 2026-08-13 | Sprint 0 | HUMAN | Enable Dependabot alerts, security updates, private vulnerability reporting, branch protection | Repo exists; `setup-github-repo.sh` still needs a human pass in Settings |
| 2026-08-13 | Sprint 0 | HUMAN | Paste `docs/GITHUB_ABOUT.md` into GitHub About | Repo is live: https://github.com/edwardlthompson/OpenShouter |
| 2026-08-13 | Sprint 0 | HUMAN | Set `.app-update.json` `release_repo` to `edwardlthompson/OpenShouter` | Live file is gitignored |
| 2026-08-13 | Sprint 0 | HUMAN | Approve ADR-0001 (Hilt/MVVM/`org.openshouter`) and ADR-0002 (no Play Services location) | Product sign-off |
| 2026-08-13 | Sprint 1 | HUMAN | Confirm FOSS Gradle deps (no GMS/Firebase) | Review `examples/android/gradle/libs.versions.toml` |
| 2026-08-13 | Sprint 2 | HUMAN | Approve default TTS format `%app: %title - %text` | Implemented as default |
| 2026-08-13 | Sprint 6 | HUMAN | Confirm background-location rationale copy | Dashboard + `docs/PRIVACY.md` |
| 2026-08-13 | Sprint 7 | HUMAN | Copy review for permission rationales | Dashboard strings |
| 2026-08-14 | Sprint 0 — Bootstrap & identity | HUMAN | Create GitHub repo, set remote, and enable Dependabot alerts + security updates + private vulnerability reporting (docs/SECURITY_TRIAGE.md) | No automation rule |
| 2026-08-14 | Sprint 0 — Bootstrap & identity | HUMAN | Create GitHub repo, set remote, and enable Dependabot alerts + security updates + private vulnerability reporting (`docs/SECURITY_TRIAGE.md`) | No automation rule |
| 2026-08-14 | Sprint 0 — Bootstrap & identity | HUMAN | Paste `docs/GITHUB_ABOUT.md` into GitHub → Settings → General → About | No automation rule for HUMAN task in sprint Sprint 0 — Bootstrap & identity |
| 2026-08-14 | Sprint 1 — Android Gradle scaffold | HUMAN | Confirm FOSS deps (no Play Services / Firebase) in Gradle manifests | No automation rule for HUMAN task in sprint Sprint 1 — Android Gradle scaffold |
| 2026-08-14 | Sprint 9 — Notification TTS quality + unused settings UI | ADB | Quiet-hours custom window suppresses; history lists without leaking payloads to logcat | No automation rule for ADB task in sprint Sprint 9 — Notification TTS quality + unused settings UI |
| 2026-08-14 | Sprint 9 — Notification TTS quality + unused settings UI | ADB | Test notification speaks with delay/max-length; shake threshold change interrupts | No automation rule for ADB task in sprint Sprint 9 — Notification TTS quality + unused settings UI |
| 2026-08-14 | Sprint 10 — Shouter shout channels | ADB | Time shout at interval; missed call after RING→IDLE; SMS app notification uses Message rules | No automation rule for ADB task in sprint Sprint 10 — Shouter shout channels |
| 2026-08-14 | Sprint 10 — Shouter shout channels | HUMAN | Approve `SCHEDULE_EXACT_ALARM` opt-in copy if “Announce accurately” ships | No automation rule for HUMAN task in sprint Sprint 10 — Shouter shout channels |
| 2026-08-14 | Sprint 10 — Shouter shout channels | ADB | Battery situation toggles + custom phrases without logging percents next to identity | No automation rule for ADB task in sprint Sprint 10 — Shouter shout channels |
| 2026-08-14 | Sprint 11 — Per-app overrides + backup | ADB | Searchable app list; App name only vs Notification vs both; unlisted apps silent | No automation rule for ADB task in sprint Sprint 11 — Per-app overrides + backup |
| 2026-08-14 | Sprint 11 — Per-app overrides + backup | ADB | Backup zip restores toggles; history payloads absent from zip | No automation rule for ADB task in sprint Sprint 11 — Per-app overrides + backup |
| 2026-08-14 | Sprint 11 — Per-app overrides + backup | HUMAN | Confirm QUERY_ALL_PACKAGES Play-policy N/A (GitHub Releases only) | No automation rule for HUMAN task in sprint Sprint 11 — Per-app overrides + backup |
| 2026-08-15 | Sprint 12 — Close notification + TTS quality gaps | ADB | Quiet-hours 24-hour grid + RESET/PRESET; shake threshold interrupts; test notification posts without PII in logcat | No automation rule for ADB task in sprint Sprint 12 — Close notification + TTS quality gaps |
| 2026-08-15 | Sprint 12 — Close notification + TTS quality gaps | ADB | Empty/group/repeat skips; ignore-reason column shows enum only | No automation rule for ADB task in sprint Sprint 12 — Close notification + TTS quality gaps |
| 2026-08-15 | Sprint 12 — Close notification + TTS quality gaps | HUMAN | Confirm OEM autostart copy for vendor Settings intents (no extra SDKs) | No automation rule for HUMAN task in sprint Sprint 12 — Close notification + TTS quality gaps |
| 2026-08-15 | Sprint 13 — Finish shout channels + backup + overrides | ADB | Nick/blacklist skips without logging numbers; unknown-number toggles on call and message | No automation rule for ADB task in sprint Sprint 13 — Finish shout channels + backup + overrides |
| 2026-08-15 | Sprint 13 — Finish shout channels + backup + overrides | ADB | Reminder alarm speaks + optional notification; SAF zip restores toggles and excludes history | No automation rule for ADB task in sprint Sprint 13 — Finish shout channels + backup + overrides |
| 2026-08-15 | Sprint 13 — Finish shout channels + backup + overrides | ADB | Battery situation phrases fire without logging percent next to identity | No automation rule for ADB task in sprint Sprint 13 — Finish shout channels + backup + overrides |
| 2026-08-15 | Sprint 13 — Finish shout channels + backup + overrides | HUMAN | Confirm `SCHEDULE_EXACT_ALARM` copy still covers reminder exact opt-in | No automation rule for HUMAN task in sprint Sprint 13 — Finish shout channels + backup + overrides |
| 2026-08-15 | Sprint 14 — Close remaining Partial rows | ADB | Live shake g-meter moves with the device; language chips match `engine.availableLanguages` | No automation rule for ADB task in sprint Sprint 14 |
| 2026-08-15 | Sprint 14 — Close remaining Partial rows | ADB | Per-channel headphone/silent/stream/repeat apply to a call shout | No automation rule for ADB task in sprint Sprint 14 |
| 2026-08-15 | Sprint 14 — Close remaining Partial rows | ADB | Reminder day/week/month/year reschedules after fire | No automation rule for ADB task in sprint Sprint 14 |
