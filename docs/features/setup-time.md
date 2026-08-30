# Feature: welcome permissions + on-the-hour shout

First-run welcome lists one **Activate** button per runtime/special permission, including unrestricted battery and exact alarms. Hourly time shout is a regular announcement: `TIME_TICK` while the announcer service is running, plus `AlarmManager.setExactAndAllowWhileIdle` when “Announce on the exact minute” is on. It does not use `setAlarmClock`, so bedtime/sleep mode stays on.

## Acceptance criteria

- Welcome shows until Continue; dashboard can reopen it
- Buttons: notification access, POST_NOTIFICATIONS (API 33+), phone, contacts, call log, fine location, background location, ignore-battery, app battery details, exact alarms
- After permissions, a Silence competing sounds section installs OpenShouter Silent and opens system sound settings (`docs/features/silence-competing-sounds.md`)
- Time shout toggle: announce on the hour when master announcements are on
- Keep-alive: FGS + boot/replace + `TIME_TICK` + exact-while-idle (not alarm-clock); TTS queues if the engine is still warming

## Smoke scenario

1. Fresh install → welcome → grant unrestricted battery and exact alarms
2. Enable **Announce the time on the hour**
3. Device sleeps; at the next hour the phrase speaks without opening the app

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty phrase | Skip speak when blank |
| Network timeout | N/A |
| Race TTS not ready | `TtsController` pending queue |
| Unhandled AlarmManager | `runCatching`; inexact fallback if exact denied |
| Quiet hours vs clock | Same `SpeakGate` as other shouts |
| Bedtime exits | Never `setAlarmClock`; TIME uses the selected notification stream |
| Tick vs alarm race | `TimeShoutAnnouncer.lastSlot` drops the duplicate |

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/openshouter/` |
| View | matching Compose screen under `org/openshouter/` |
| Tests | `examples/android/app/src/test/java/org/openshouter/` |
| Wiring | composition root ≤10 lines |

## Tests

- Automated: yes — Android unit tests under `examples/android/app/src/test/`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
