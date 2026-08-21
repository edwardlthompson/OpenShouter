# Feature: calendar shout

Speak the next calendar title in a user-picked look-ahead window (5 / 15 / 30 minutes) via AOSP `CalendarContract.Instances`. No Play Services.

## Acceptance criteria

- Off by default (`calendarShoutEnabled`)
- Requires `READ_CALENDAR`; setup row explains why
- Skip blank titles; speak each `eventId+begin` once
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

Unit tests cover skip/blank/once. Device grant is `[ADB]` on CPH2655.

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty title | `CalendarShout.phrase` returns blank → skip speak |
| Network timeout | N/A — on-device provider |
| Race | Single `TtsController` queue; lastSpoken pair |
| Unhandled exceptions | `runCatching` on query; skip event |
