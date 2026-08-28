# Feature: record every shout in history

Time, battery, reminder, calendar, and Bluetooth shouts write an announcement-history row when they speak, same as notifications and calls.

## Acceptance criteria

- ✅ A time shout inserts one `TIME` history row with the spoken phrase and empty title/text
- ✅ Battery, reminder, calendar, and Bluetooth shouts insert matching `kind` rows
- ✅ Notification and message paths stay on `NotificationHistory` (no double row)
- ✅ Cellular calls stay on `CallHistory` (incoming + missed); VoIP stays on the listener path
- ✅ History list labels built-in shouts as Time / Battery / Reminder / Calendar / Bluetooth
- ✅ Tap on a built-in row does not open Apps-to-shout or another app’s channel settings
- ✅ TalkBack: row is a button; dialog title uses the source label
- ✅ i18n: `history_source_*` / `history_internal_help` in `strings_history.xml` (en/es/fr)

## Smoke scenario

1. Given Time shout is on (hourly or 15/30)
2. When OpenShouter announces the time
3. Then Announcement history shows a **Time** row at that clock time
4. Turning on **Show spoken text** shows the phrase that was shouted
5. Tapping the row explains that the shout is managed in OpenShouter settings

## Container map

| Layer | Path |
|-------|------|
| Logic | `domain/ShoutHistory.kt`, `data/ShoutHistoryStore.kt` |
| View | `ui/history/HistoryScreen.kt`, `HistorySourceLabel.kt`, `HistoryMuteDialog.kt` |
| Tests | `ShoutHistoryTest` |
| Wiring | `TimeShoutAnnouncer`, `PowerMonitor`, `ReminderReceiver`, `CalendarMonitor`, `BluetoothMonitor`; composition root unchanged |

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty utterance | `ShoutHistoryStore.row` returns null for blank spoken or non-recordable kinds. Test: `ShoutHistoryTest` |
| Network timeout | N/A — no network I/O |
| Race (slot vs history insert) | Insert runs on the same announce path after `shouldSpeakSlot`; one row per spoken slot |
| Unhandled Room failure | Same as existing history insert; shout still proceeds |
| Double-record notifications | `NOTIFICATION` / `MESSAGE` / `CALL` are excluded from `ShoutHistory.records` |
| Mute dialog on Time row | Internal kinds hide Apps-to-shout and channel settings. Artifact: `HistoryMuteDialog.showChannelToggle` |
| PII in logcat | Do not log spoken phrases, numbers, or names. History stores spoken on-device like notifications |
| `strings.xml` 300-line cap | New keys live in `strings_history.xml` |

## Notes

- Room already has `kind` (v6). No schema change.
- After each AGENT step: `python3 scripts/agent-run.py watch-agent-gates --once --autofix`
