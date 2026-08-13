# Runbook

> Operational guide for OpenShouter (Android FOSS announcer).

## Health Checks

This is an on-device app, not a server. Use:

| Check | Purpose | Expected |
|-------|---------|----------|
| Foreground notification | Process liveness | Visible while announcer is enabled |
| `adb shell dumpsys notification` | Listener bound | OpenShouter listener listed |
| `./gradlew assembleDebug test` | Build + unit tests | EXIT 0 |
| GitHub Actions **CI** | Trunk health | Green on `main` |

## Structured Logging

- Tag: `OpenShouter`
- **Never** log notification title/text, phone numbers, contact names, or coordinates
- Log levels: `ERROR` for user-visible failures, `WARN` for missing permission, `INFO` for service lifecycle

## Deploy

1. `[AUTO]` CI green on `main`
2. `[HUMAN]` Approve release (pre-release gate in `BUILD_PLAN.md`)
3. `[AUTO]` Tag and publish APK via GitHub Release (sha256)
4. `[ADB]` Sideload or F-Droid metadata update
5. Update checks use `.app-update.json` `release_repo` (format-locked `apk`)

## Rollback

1. Install previous GitHub Release APK (same `apk` format)
2. Confirm Notification Listener still enabled (OS may reset after uninstall)
3. Log incident in `DECISION_LOG.md` if user-impacting

## Common Failures

| Symptom | Check | Fix |
|---------|-------|-----|
| CI failing on lint | Local `pre-commit run --all-files` | Fix and push |
| No speech | Notification access, DND, quiet hours, headset-only, master mute | Dashboard permission/status |
| Call loop continues after answer | TelephonyCallback OFFHOOK | Stop TTS in call state handler |
| Geofence never fires | Background location + location enabled | Places screen + system settings |
| UTF-16 files on Windows | `scripts/check-file-encoding.sh` | Rewrite UTF-8 no BOM |
| `gh` missing | GitHub CLI not installed | `[HUMAN]` install gh; run `setup-github-repo.ps1` |

## Escalation

1. Check `KNOWLEDGE_BASE.md` and `docs/FOR_AGENTS.md` Failure Playbook
2. 3-strike: stop and ask `[HUMAN]` with evidence (command output, logcat *without* PII)
3. Security issues: `SECURITY.md` (private reporting), not public issues
