# Feature parity matrix

Inventory of **Shouter Pro** (`com.bhkapps.proshouter`) vs **Voice Notify** (`com.pilot51.voicenotify`) vs **OpenShouter** (`org.openshouter`).

**Sources (2026-08-13):**

- Shouter Pro walked page-by-page on device CPH2583 (UI dumps of home, silent hours, muting, history, app notification, call, message, time, battery, reminder, voice settings, call device-states dialog) plus preference XML from the installed APK (`res/xml/*_settings.xml`). Spoken history rows were not copied into this repo.
- Voice Notify is **not installed**; inventory is from GitHub `main` code (`prefs/db/Settings.kt`, `PreferenceHelper.kt`, `NotificationInfo.kt`, `IgnoreReason.kt`) — not the README.
- OpenShouter from `examples/android/` domain, DataStore, and Compose screens.

Interactive filterable table: Cursor canvas `feature-parity-matrix.canvas.tsx` (IDE-only; this file is the git source of truth). Refresh both after every `/build` wrap-up and `/push`.

**Wrap-up 2026-08-16:** HUMAN confirmations closed by `/build` (GitHub About, FOSS deps, default `%app: %title - %text`, location/permission/exact-alarm/OEM copy, `QUERY_ALL_PACKAGES` Play-policy N/A). Matrix stays Yes / Skip — no new Partial or No.

**Wrap-up 2026-08-18:** Speak in silent/vibrate is opt-in (`ds_speak_silent`). DND counts as silent. No ALARM auto-fallback.

**Wrap-up 2026-08-20:** Fastest same-resolution window mode + high-refresh scroll on home, app-speak, and menu scaffolds. Matrix rows unchanged (OpenShouter extra, not Shouter/VN).

**Wrap-up 2026-08-20 (Sprint 15):** Spanish `values-es` overlay, AOSP calendar shout, `%sim` caller-ID token, Bluetooth connect/battery extras. OpenShouter extras — in-scope Shouter/VN matrix rows stay Yes / Skip.

**Wrap-up 2026-08-21 (Sprint 16):** Quiet Venmo donate on About + Menu; once-per-version ethical reminder; daily GitHub check uses `openshouter-X.Y.Z-foss.apk` filenames. Apps to shout: selected-only filter + select/deselect all. Matrix rows unchanged (OpenShouter extras).

Status: **Yes** shipped · **Partial** logic or subset UI · **No** missing · **Skip** will not copy (FOSS / out of scope).

## Summary

| Bucket | Count (OpenShouter) |
|--------|---------------------|
| Yes | Master enable, widget, QS tile, FGS, boot, mute gestures + live g-meter, notification TTS, call loop, missed-call RING→IDLE, silent geofences, theme, GitHub updates, searchable app-speak picker, 24-hour quiet grid, history + ignore-reason, regex + require/ignore lists, empty/group/repeat, extra format tokens, repeat count + screen-off loop, test notification, pause-media, pitch, OEM autostart, TTS/device-state settings, engine language picker, per-channel headphone/stream/repeat grid, 15/30/60 time shout + exact opt-in + 12/24 clock style, nick/blacklist, call/message/time builders, battery situation phrases, reminder hour/day/week/month/year + also-notify, SAF backup, full AppOverride merge |
| Partial | — |
| No | — |
| Skip | Placebook companion, GMS/Firebase analytics, Facebook/rate/share, in-app language clone, `READ_SMS` |
## Chosen add-in order

Do not clone either UI. Port **behavior** into OpenShouter’s Compose settings.

1. **Sprint 9 — Unlock and match Voice Notify notification quality.** Expose DataStore quiet hours; history + regex UIs; expand `AnnouncementGate`; TTS playback policy (stream, delay, max length, audio focus, test notification).
2. **Sprint 10 — Shouter shout channels.** Time interval, battery phrase UI, missed call, message-via-notifications (ADR-0003), voice reminders.
3. **Sprint 11 — Per-app control.** `QUERY_ALL_PACKAGES` picker, `AppOverride` merge, settings backup zip (no history payloads).
4. **Sprint 12 — Close remaining VN notification/TTS gaps.** OEM autostart, shake threshold, 24-hour quiet grid, empty/group/repeat, test notification, repeat loops, pitch/locale, ignore reasons, extra tokens.
5. **Sprint 13 — Finish Shouter channels + backup.** Nick/blacklist, call/message builders, reminder alarms, battery phrases, SAF picker, full per-app merge, per-channel device states.
6. **Sprint 14 — Close remaining Partial rows.** Live shake g-meter, engine language picker, per-channel headphone/stream/repeat grid, reminder calendar intervals.

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

From `Settings.kt` + DataStore keys: audio focus · require/ignore strings · ignore empty/groups/repeats · speak screen on/off · headset on/off · silent · in-call · quiet start/end · TTS string · text replace · speak emojis · max length · stream · delay · repeat while screen off · per-app row merge · shake threshold · log ignored (notifications/apps) · app-list default enable · suspend · backup zip. Tokens in `NotificationInfo.kt`: `#A #T #S #C #M #I #H #Y #B #L`.

Not copied: Play analytics.

## OpenShouter today

Dashboard master + sectioned Hear / History / Phone cards · searchable app-speak list · extra format tokens · full AppOverride merge · regex + require/ignore + empty/group/repeat · looping caller ID + nick/blacklist + call format + `%sim` · missed-call RING→IDLE · unknown callers speak spaced digits · message extras/sender (no `READ_SMS`) · battery situation phrases · shake slider + live g-meter + flip + screen mute · 24-hour quiet grid · TTS in-process WAV + engine language picker + test notification · per-channel headphone/stream/repeat + silent/vibrate · 15/30/60 time shout + 12/24/system clock + `%time` builder · reminder hour/day/week/month/year + also-notify · SAF settings zip · OEM autostart · silent geofences · FGS · QS tile · widget · history + ignore-reason · Spanish overlay · calendar shout · Bluetooth connect/battery · quiet Venmo donate + filename-based GitHub updates.

## Matrix

| Category | Feature | Shouter | VN | OS | Add in |
|----------|---------|---------|----|----|--------|
| Master | Master enable | Yes | Yes | Yes | Shipped |
| Master | Widget | Yes | Yes | Yes | Shipped |
| Master | QS tile | No | Yes | Yes | Shipped |
| Master | FGS / boot | Partial/Yes | Yes | Yes | Shipped |
| Master | OEM autostart dialog | No | Yes | Yes | Shipped OemScreen + vendor Settings intent |
| Quiet hours | Hour grid + day chips | Yes | No | Yes | Shipped 24-hour HourGrid + RESET/PRESET |
| Quiet hours | Start/end pickers | Partial | Yes | Yes | Shipped 15-minute start/end + dynamic label |
| Muting | Shake / screen on / screen off | Yes | Shake only | Yes | Sensitivity Sprint 12 |
| Muting | Flip face-down | No | No | Yes | Keep |
| Notifications | Listener TTS | Yes | Yes | Yes | Shipped |
| Notifications | Installed-app picker | Yes | Yes | Yes | Shipped (Sprint 11 list) |
| Notifications | Format tokens | Prefixes | `#A` `#T` `#S` `#C` `#M` `#I` `#H` `#Y` `#B` `#L` | `%app` `%title` `%text` `%ticker` `%subtext` `%bigtext` `%info` `%bigtitle` `%bigsummary` `%lines` `%time` | Shipped |
| Notifications | Regex / ignore-require | No | Yes | Yes | Shipped ignore/replace UI |
| Notifications | Ignore empty/group/repeat | No | Yes | Yes | Shipped NotificationPolicy + Filters toggles |
| Notifications | Per-app overrides | No | Yes | Yes | Shipped AppOverride merge + OverrideScreen |
| Notifications | Repeat count | Yes | No | Yes | Shipped repeatCount 0–3 |
| Notifications | Delay / max length / screen-off repeat | No | Yes | Yes | Shipped delay/max + screen-off loop |
| Notifications | Test notification | No | Yes | Yes | Shipped TestNotification.post |
| Device states | Global screen-off / headset-only | Yes | Yes | Yes | Shipped |
| Device states | Per-channel headphone/silent | Yes | No | Yes | Shipped ChannelStateScreen grid + stream/repeat |
| Device states | Silent/vibrate + in-call flags | Yes | Yes | Yes | Shipped DeviceStatePolicy toggles |
| TTS | Stream / audio focus / pitch / voice test | Stream | Stream+focus | Yes | Pitch + pause-media + `availableLanguages` chips |
| Call | Looping ID + contacts | Yes | No | Yes | Shipped |
| Call | Unknown toggle, nick/blacklist, missed | Yes | No | Yes | Shipped ContactRules + speakUnknown + missed RING→IDLE |
| Message | Dedicated SMS/MMS shout | Yes | Via apps | Yes | Extras/sender parse; no `READ_SMS` |
| Time | Interval + exact opt-in | Yes | No | Yes | 15/30/60 chips + `timeShoutExact` + 12/24/system hour style |
| Battery | Events + custom phrases | Yes | No | Yes | Shipped situation picker + `%level` phrases |
| Reminders | Recurring voice list | Yes | No | Yes | Hour/day/week/month/year chips + AlarmManager sync |
| Location | Placebook proximity | Yes | No | Skip | Keep silent fences |
| History | Viewer + ignore reasons | Yes | Yes | Yes | Shipped ignore-reason enum only |
| Backup | Settings zip | No | Yes | Yes | Shipped SAF CreateDocument/OpenDocument; history excluded |
| Distro | GMS analytics / Facebook | Yes | Play analytics | Skip | Forbidden |
| Distro | GitHub update check | No | No | Yes | Keep |
## Parallelization (implementation, after plan approval)

| Sprint | Sequential lock | Parallel scopes |
|--------|-----------------|-----------------|
| 9 | `TtsPlaybackPolicy`, `DeviceStatePolicy` in `domain/` | `ui/quiet/`, `ui/history/`, `ui/filters/`, `ui/tts/` |
| 10 | `TimeShout`, `ReminderEntity`, `MessageChannelPolicy` | `time/`, `reminder/`, `message/`, `missed/` |
| 11 | `AppOverride` entity + `QUERY_ALL_PACKAGES` | `ui/apps/`, `data/backup/`, override unit tests |
| 12 | `NotificationPolicy`, `IgnoreReason`, `ShakeThreshold`, `TtsVoice` | `oem/`, `gesture/`, `ui/quiet/`, `notification/`, `tts/`, `ui/tts/`, `ui/history/`, `ui/filters/` |
| 13 | `ContactRule`, `ChannelDeviceState`, full `AppOverride`, `BatterySituation` | `contacts/`, `call/`, `message/`, `time/`, `power/`, `reminder/`, `backup/`, `ui/overrides/` |
| 14 | `ReminderInterval`, `SpokenEvent.stream`, `ChannelStates.spoken` | `gesture/`, `ui/channel/`, `ui/tts/`, `reminder/`, `call/` |
`agent_count_target`: 4, 4, 3, 8, 8, 5. Wiring stays in `OpenShouterHome` (≤10 lines per feature). One feature row per agent.

Sprints 12–14 added 2026-08-14/15 to reach 100% in-scope parity. Skip rows stay out of BUILD_PLAN.
