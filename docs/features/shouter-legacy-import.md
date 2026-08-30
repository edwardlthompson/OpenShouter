# Feature: Import settings from classic Shouter

Classic Shouter Pro (`com.bhkapps.proshouter`) stores product data in **two private stores**. There is no third settings file on disk.

| Path | Access | What we import |
|------|--------|----------------|
| OEM / no-root: `content://bhkapps.proshouter/{apps,silenthours,reminders,contacts}` | Exported ContentProvider (no permission) | Enabled apps (`shout=1`), silent-hour cells, reminders, nick/block |
| Root: `/data/data/com.bhkapps.proshouter/databases/shdb` | `su` cat | Same tables as the provider |
| Root: `/data/data/…/shared_prefs/com.bhkapps.proshouter_preferences.xml` | `su` cat (not exported) | Master/mute/time/call/message/battery, per-channel stream/repeat/headset/screen-off, speak-app-name, reminder master |
| `shoutlogs` / `pkdbserprx_*` | never read | Spoken history and contact JSON stay out |

Free Shouter uses `bhkapps.shouter` / `com.bhkapps.shouter` when present. Import prefers a complete root dump, then fills missing tables from the OEM provider. Root uses Magisk `su -mm` (global mount namespace) plus `/debug_ramdisk/su` so isolated-ns devices can still read `/data/data/com.bhkapps.*`.

## Acceptance criteria

- ✅ Welcome/setup one-tap import uses root when Superuser is granted, else the OEM provider
- ✅ Same dump applies from a picked `shdb`, prefs XML, or a zip of both
- ✅ Prefs map mute, time/call/message formats, battery including percent status, channel streams/repeats/silent-only, `Enotifrdapname`, `enremasht`, `entmforid`
- ✅ `shoutlogs` / `pkdbserprx_*` payloads are never parsed
- ✅ Setup shows leftover manual steps

## Smoke scenario

1. Leave Shouter Pro installed on a rooted device
2. Sideload debug APK; grant Magisk Superuser to `org.openshouter`
3. Welcome → Import from Shouter (or debug `IMPORT_SHOUTER`)
4. Apps to shout includes enabled packages; time shout / screen-off match prefs
5. Enable Notification access and turn Shouter off

## Manual leftover

- Notification listener + disable or uninstall Shouter (two announcers will clash)
- Placebook (`enlocasht`) — Skip, ADR-0002 (no Play location)
- TTS language index `0` means system default (already mapped as empty tag)
- Repeat `8` is Shouter “till call ends”; OpenShouter already loops caller ID
- Magisk Superuser prompt on first import if policy is not pre-granted

## Container map

| Layer | Path |
|-------|------|
| Logic | `ShouterLegacy.kt`, `ShouterLegacyRoot.kt`, `ShouterLegacyChannels.kt`, `BackupImport.kt` |
| View | `SetupLegacy.kt`, `BackupScreen.kt` |
| Tests | `ShouterLegacyTest.kt` |
| Wiring | `OpenShouterPanes` Setup + Backup |

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | Blank packages/reminders skipped; missing root+provider → 0 items + `backup_legacy_none` |
| Network timeout | N/A — ContentResolver, `su`, or user-picked file |
| Race | Single coroutine upsert; reminder insert then `ReminderAlarms.sync` |
| Unhandled exceptions | Provider/SQLite/`su`/zip failures are `runCatching` → empty dump or 0 |

## Tests

- Automated: yes — Android unit tests under `examples/android/app/src/test/`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
