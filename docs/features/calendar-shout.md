# Feature: calendar shout

Speak the next calendar title in a user-picked look-ahead window (5 / 15 / 30 minutes) via AOSP `CalendarContract.Instances`. No Play Services.

## Acceptance criteria

- Off by default (`calendarShoutEnabled`)
- Requires `READ_CALENDAR`; setup row and Calendar pane explain Android calendar permission plus device-synced calendars (no Google Calendar OAuth)
- Skip blank titles, all-day, hidden, and declined rows; speak each `eventId+begin` once
- `CalendarMonitor.start()` registers `TIME_TICK` even before permission; scan no-ops until granted
- Never log event titles

## Smoke scenario

1. Grant calendar; enable Calendar shout
2. Create an event starting within the chosen look-ahead window
3. After the next minute tick, TTS speaks the upcoming title

## Container map

| Layer | Path |
|-------|------|
| Logic | `org/openshouter/calendar/CalendarShout.kt` |
| Monitor | `org/openshouter/calendar/CalendarMonitor.kt` |
| View | `org/openshouter/calendar/CalendarShoutScreen.kt` |
| Tests | `org/openshouter/calendar/CalendarShoutTest.kt` |
| Wiring | `AnnouncerService` start + `OpenShouterPanes` ≤10 lines |
## Fallback validation

Unit tests cover skip/blank/once plus pickNext past/all-day/hidden. Device grant is `[ADB]` on CPH2655.

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty title | `CalendarShout.phrase` returns blank → skip speak |
| Network timeout | N/A — on-device provider |
| Race | Single `TtsController` queue; lastSpoken pair |
| Unhandled exceptions | `runCatching` on query; skip event |
| First Instances row is all-day or in-progress | `pickNext` walks rows; SQL `BEGIN>=now`; skip all-day/hidden/declined |
| Permission granted after service start | `start()` no longer requires `READ_CALENDAR`; scan checks each tick |

## Tests

- Automated: yes — Android unit tests under `examples/android/app/src/test/`

- Command: `python3 scripts/agent-run.py feature-gate --stack android`
