# Feature: welcome permissions + on-the-hour shout

First-run welcome lists one **Activate** button per runtime/special permission, including unrestricted battery and exact alarms. Hourly time shout uses `AlarmManager.setAlarmClock` so it fires on the clock boundary after Doze.

## Acceptance criteria

- Welcome shows until Continue; dashboard can reopen it
- Buttons: notification access, POST_NOTIFICATIONS (API 33+), phone, contacts, call log, fine location, background location, ignore-battery, app battery details, exact alarms
- Time shout toggle: announce on the hour when master announcements are on
- Keep-alive: FGS + boot/replace + exact alarm; TTS queues if the engine is still warming

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
