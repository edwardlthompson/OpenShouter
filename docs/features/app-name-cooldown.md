# Feature: per-channel app name cooldown

A dropdown on each shout channel sets how long OpenShouter waits before saying the same app name again. Thirty photos dropped into Messages should not produce “Messages” thirty times in thirty seconds.

## Acceptance criteria

- ✅ Voice → per-channel states has an **App name cooldown** dropdown (Off, 10s, 30s, 1 min, 2 min, 5 min)
- ✅ Default is 30 seconds on every channel, including previously saved rows that omit `ac`
- ✅ Off (0) always speaks the app name
- ✅ Notification path omits `%app` and a title that equals the app label during the window
- ✅ Message path omits a sender that equals the app label during the window and speaks the body
- ✅ Name-only Apps-to-shout is silent during the window
- ✅ Contact names are not treated as the app label
- ✅ TalkBack: dropdown uses the visible label
- ✅ i18n: `channel_app_name_cooldown*` in `strings_channel.xml` (en/es/fr)

## Smoke scenario

1. Given Messages is shouted and App name cooldown is 30 seconds (default)
2. When the user shares 30 photos into Messages within a few seconds
3. Then the first shout may say Messages; the rest speak the photo text without repeating Messages
4. When the user sets the Message channel cooldown to Off
5. Then every shout includes the app name again

## Container map

| Layer | Path |
|-------|------|
| Logic | `domain/AppNameCooldown.kt`, `domain/ChannelDeviceState.kt` |
| View | `ui/channel/AppNameCooldownDropdown.kt`, `ChannelStateScreen.kt` |
| Tests | `AppNameCooldownTest`, `NotificationUtteranceTest`, `MessageChannelTest`, `Sprint13LockTest` |
| Wiring | Existing `onChannelStates` in Voice / ChannelStateScreen (0 new composition-root lines) |

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty package or label | `RepeatClock.markAppName` ignores blank package; `isAppLabel` rejects blank sides. Test: `AppNameCooldownTest` |
| Network timeout | N/A — no network I/O |
| Race (two photos in the same millisecond) | Same `RepeatClock` instance on the listener; first mark wins. Later posts omit the name |
| Unhandled parse of old channel rows | Missing `ac` → 30 seconds. Test: `Sprint13LockTest` |
| Contact named similarly to the app | Exact ignore-case match on the resolved app label only. Test: `MessageChannelTest` |
| PII in logcat | Do not log title, text, sender, or spoken phrase |
| `strings.xml` 300-line cap | Keys live in `strings_channel.xml` |
| Filter ignore after name included | Mark app-name time only after regex lets the utterance through |

## Notes

- Call / time / battery rows show the same dropdown; only notification and message speak paths apply it.
- After each AGENT step: `python3 scripts/agent-run.py watch-agent-gates --once --autofix`
