# Feature parity matrix

Inventory of **Shouter Pro** (`com.bhkapps.proshouter`) vs **Voice Notify** (`com.pilot51.voicenotify`) vs **OpenShouter** (`org.openshouter`).

**Sources (2026-08-13):**

- Shouter Pro walked page-by-page on device CPH2583 (UI dumps of home, silent hours, muting, history, app notification, call, message, time, battery, reminder, voice settings, call device-states dialog) plus preference XML from the installed APK (`res/xml/*_settings.xml`). Spoken history rows were not copied into this repo.
- Voice Notify is **not installed**; inventory is from GitHub `main` (`strings.xml`, `prefs/db/Settings.kt`, README).
- OpenShouter from `examples/android/` domain, DataStore, and Compose screens.

Interactive filterable table: Cursor canvas `feature-parity-matrix.canvas.tsx` (IDE-only; this file is the git source of truth).

Status: **Yes** shipped · **Partial** logic or subset UI · **No** missing · **Skip** will not copy (FOSS / out of scope).

## Summary

| Bucket | Count (OpenShouter) |
|--------|---------------------|
| Yes | Master enable, widget, QS tile, FGS, boot, mute gestures, notification TTS, call loop, silent geofences, theme, GitHub updates |
| Partial | Quiet hours (stored times, hardcoded UI label), regex (Room, no UI), history (Room, no UI), battery events (no per-state UI), format tokens (`%app/%title/%text` only), screen/headset gates (global only) |
| No | App picker, per-app overrides, VN filters, TTS stream/delay/length, time/reminder/message channels, missed call, shake sensitivity, backup zip |
| Skip | Placebook companion, GMS/Firebase analytics, Facebook/rate/share, in-app language clone, `READ_SMS` |

## Chosen add-in order

Do not clone either UI. Port **behavior** into OpenShouter’s Compose settings.

1. **Sprint 9 — Unlock and match Voice Notify notification quality.** Expose DataStore quiet hours; history + regex UIs; expand `AnnouncementGate`; TTS playback policy (stream, delay, max length, audio focus, test notification).
2. **Sprint 10 — Shouter shout channels.** Time interval, battery phrase UI, missed call, message-via-notifications (ADR-0003), voice reminders.
3. **Sprint 11 — Per-app control.** `QUERY_ALL_PACKAGES` picker, `AppOverride` merge, settings backup zip (no history payloads).

Keep OpenShouter extras: flip-to-mute, silent-inside geofences, GitHub Releases updates.

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty format, packages, regex | Existing `TtsFormat` / `RegexFilter.MAX_PATTERN`; reject blank speak; tests in `DomainLogicTest` |
| Network timeout | N/A — no new network I/O; backup uses user-picked local URI |
| Race (notification vs call TTS) | Single `TtsController` queue; call loop still `QUEUE_FLUSH`; gate before speak |
| Unhandled exceptions | Regex compile already `runCatching`; AlarmManager/TTS failures skip the event, no crash |
| SMS / `READ_SMS` | **Do not add.** Message shout = notification listener + messaging packages (ADR-0003) |
| Placebook / Play location | **Skip.** Keep ADR-0002 `LocationManager` silent fences |
| History PII in UI/logcat | History screen shows package + time by default; spoken text behind a toggle; never log payloads |
| `QUERY_ALL_PACKAGES` | Sprint 11 Sequential + manifest; GitHub Releases only (no Play policy review) |
| Exact alarms (time/reminders) | Default inexact `AlarmManager`; opt-in exact with `SCHEDULE_EXACT_ALARM` rationale |
| GMS / Firebase in Shouter APK | Forbidden to copy |

## Shouter screens (live)

**Master control:** Enable, Silent hours (24-hour grid + day chips, RESET/PRESET), Muting (shake, screen off, screen on), Announcement History, Add widget.

**Manage shouts:** App Notification (enable, select apps, device states, message builder, repeat, audio stream) · Call (enable, message builder, read unknown numbers, nickname/blacklist, device states, repeat, stream) · Message (enable, unknown numbers, nickname/blacklist, device states, message builder, read content, known-only body, repeat, stream) · Time (enable, schedule, announce accurately, message builder, device states, repeat, stream) · Battery (enable, situation picker, message builder, device states, repeat, stream) · Reminder (enable, list, device states, also notify, repeat, stream) · Location (Placebook required).

**Call device states dialog:** Headphone On, Headphone Off, Silent/Vibrate (screen flags also exist per-channel in APK keys).

**Voice settings:** Voice test, pitch, TTS language override, system TTS settings.

**Not copied:** Help/Facebook/rate/share, Credits, Language row, Placebook, GMS keys in the APK.

## Voice Notify (source)

QS tile + widget suspend · TTS message tokens (`#A` app, `#T` ticker, `#S` subtext, `#C` title, `#M` message, plus big-text/lines) · text replace · ignore/require text · ignore empty/group/repeats · TTS stream · screen/headset/silent/in-call flags · quiet start/end · shake threshold · max length · delay · repeat while screen off · **per-app overrides** · notification log + ignore reasons · test notification · backup/restore zip · pause/dim media · speak emojis · OEM autostart help.

Not copied: Play analytics.

## OpenShouter today

Dashboard master + permission shortcuts · package textarea blacklist/whitelist · `%app %title %text` · Room regex (no UI) · looping caller ID · battery/plug phrases (no settings UI) · shake + flip + screen mute · quiet hours toggle labeled 10pm–7am (start/end/days already in DataStore) · screen-off-only · headset/A2DP-only · silent geofences · FGS · QS tile · widget · history DAO (no UI).

## Matrix

| Category | Feature | Shouter | VN | OS | Add in |
|----------|---------|---------|----|----|--------|
| Master | Master enable | Yes | Yes | Yes | Shipped |
| Master | Widget | Yes | Yes | Yes | Shipped |
| Master | QS tile | No | Yes | Yes | Shipped |
| Master | FGS / boot | Partial/Yes | Yes | Yes | Shipped |
| Master | OEM autostart dialog | No | Yes | No | Sprint 9 |
| Quiet hours | Hour grid + day chips | Yes | No | No | Sprint 9 window+days first |
| Quiet hours | Start/end pickers | Partial | Yes | Partial | Sprint 9 UI |
| Muting | Shake / screen on / screen off | Yes | Shake only | Yes | Sensitivity Sprint 9 |
| Muting | Flip face-down | No | No | Yes | Keep |
| Notifications | Listener TTS | Yes | Yes | Yes | Shipped |
| Notifications | Installed-app picker | Yes | Yes | No | Sprint 11 |
| Notifications | Format tokens | Prefixes | `#A` `#C` `#M`… | `%app` `%title` `%text` | Sprint 9 |
| Notifications | Regex / ignore-require | No | Yes | Partial | Sprint 9 UI |
| Notifications | Ignore empty/group/repeat | No | Yes | No | Sprint 9 |
| Notifications | Per-app overrides | No | Yes | No | Sprint 11 |
| Notifications | Repeat count | Yes | No | No | Sprint 9 |
| Notifications | Delay / max length / screen-off repeat | No | Yes | No | Sprint 9 |
| Notifications | Test notification | No | Yes | No | Sprint 9 |
| Device states | Global screen-off / headset-only | Yes | Yes | Yes | Shipped |
| Device states | Per-channel headphone/silent | Yes | No | No | Sprint 10 |
| Device states | Silent/vibrate + in-call flags | Yes | Yes | No/Partial | Sprint 9 |
| TTS | Stream / audio focus / pitch / voice test | Stream | Stream+focus | No | Sprint 9 |
| Call | Looping ID + contacts | Yes | No | Yes | Shipped |
| Call | Unknown toggle, nick/blacklist, missed | Yes | No | No | Sprint 10 |
| Message | Dedicated SMS/MMS shout | Yes | Via apps | No | Sprint 10, ADR-0003 |
| Time | Interval + exact opt-in | Yes | No | No | Sprint 10 |
| Battery | Events + custom phrases | Yes | No | Partial | Sprint 10 UI |
| Reminders | Recurring voice list | Yes | No | No | Sprint 10 |
| Location | Placebook proximity | Yes | No | Skip | Keep silent fences |
| History | Viewer + ignore reasons | Yes | Yes | Partial | Sprint 9 |
| Backup | Settings zip | No | Yes | No | Sprint 11 |
| Distro | GMS analytics / Facebook | Yes | Play analytics | Skip | Forbidden |
| Distro | GitHub update check | No | No | Yes | Keep |

## Parallelization (implementation, after plan approval)

| Sprint | Sequential lock | Parallel scopes |
|--------|-----------------|-----------------|
| 9 | `TtsPlaybackPolicy`, `DeviceStatePolicy` in `domain/` | `ui/quiet/`, `ui/history/`, `ui/filters/`, `ui/tts/` |
| 10 | `TimeShout`, `ReminderEntity`, `MessageChannelPolicy` | `time/`, `reminder/`, `message/`, `missed/` |
| 11 | `AppOverride` entity + `QUERY_ALL_PACKAGES` | `ui/apps/`, `data/backup/`, override unit tests |

`agent_count_target`: 4, 4, 3. Wiring stays in `OpenShouterHome` (≤10 lines per feature). One feature row per agent.

Do not implement until the human approves this plan in chat.
