# Feature: history mute from a tap

Tap an announcement history row to mute that app in OpenShouter or open the posting app’s notification channel in system settings.

## Acceptance criteria

- ✅ History rows are clickable
- ✅ A dialog offers an OpenShouter shout toggle for that package (Apps to shout on/off)
- ✅ The same dialog offers a notification-channel toggle that opens system settings on that channel (highlight extras when the channel id is known)
- ✅ Missing channel id falls back to the app’s notification settings page
- ✅ TalkBack: row is a button; dialog switches use the visible labels
- ✅ i18n: `history_manage_*` / `history_toggle_*` / `history_channel_*` / `history_dialog_close` in `strings_history.xml` (en/es/fr)

## Smoke scenario

1. Given Notification access is on and a notification has been announced
2. When the user opens Announcement history and taps the row
3. Then a dialog shows **Shout with OpenShouter** and **This notification channel**
4. Turning the OpenShouter switch off removes the app from Apps to shout
5. Turning the channel switch opens that channel in the app’s notification settings (or the app notification page when the channel id was not stored)

## Container map

| Layer | Path |
|-------|------|
| Schema | `data/Store.kt` `HistoryEntity.channelId` / `channelName`, Room v5 |
| Logic | `domain/HistorySpeak.kt`, `domain/HistoryChannelTarget.kt`, `notification/NotificationChannelSettings.kt` |
| View | `ui/history/HistoryScreen.kt`, `HistoryMuteDialog.kt`, `HistoryPane.kt` |
| Tests | `HistorySpeakTest`, `HistoryChannelTargetTest`, `NotificationChannelSettingsTest` |
| Wiring | `OpenShouterPanes` History pane ≤10 lines |

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty package or channel | `HistoryChannelTarget` rejects blank package; blank channel opens app notification settings. Tests in `HistoryChannelTargetTest` / `NotificationChannelSettingsTest` |
| Network timeout | N/A — no network I/O |
| Race (dialog vs Room speak rules) | Toggle writes `AppSpeakStore.set`; dialog reads `appRules` Flow so the switch recomposes |
| Unhandled Settings intent | `NotificationChannelSettings.launch` uses `runCatching`; falls back to highlighted app notification settings |
| Channel state unknown | OpenShouter cannot change another app’s channel; the switch opens system settings. Help copy states that |
| PII in logcat | Do not log title, text, spoken, numbers, or coordinates. Dialog shows app label + channel name only |
| strings.xml 300-line cap | New keys live in `strings_history.xml` (I18nEsTest already unions `strings*.xml`) |

## Notes

- Message and call shout paths still have their own channel settings; the OpenShouter switch updates Apps to shout the same way the picker does.
- After each AGENT step: `python3 scripts/agent-run.py watch-agent-gates --once --autofix`

## Tests

- Automated: yes — Android unit tests under `examples/android/app/src/test/`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
