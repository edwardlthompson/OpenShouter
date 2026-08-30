# Feature: sun-moon-alarm

> Sun and moon event alarms. One stored place (one-time fix or typed city). Looping TTS until dismissed. Full-screen lockscreen popup like a clock alarm. No Play Services.

## Acceptance criteria

- 🔲 User-visible behavior: a Sun/Moon alarm menu lists independent toggles for solar and lunar events. When an enabled event occurs, the phone shows a full-screen alarm over the lockscreen and shouts the event name until the user dismisses it.
- 🔲 Place: one-time `ACCESS_COARSE_LOCATION` via `LocationManager` (never `play-services-location`), **or** a city search field that resolves while typing. The **city** (locality) is the stored place, not a street address.
- 🔲 Offline/error: after a place is stored, all event times are computed on-device (no network). Failed geocode leaves the last good city. Missing permission with no city stored keeps every toggle off and shows why.
- 🔲 Accessibility: dismiss and snooze are labelled buttons; TalkBack reads the event title; no Toast-only errors.
- 🔲 i18n: `astro_*` keys in `strings_astro.xml` (en + es + fr overlays).
- 🔲 PII: never log coordinates, city strings, or alarm copy.

## Place

1. **One-time permission:** request coarse location once, read one `LocationManager` fix, reverse-geocode to a city label, persist city + lat/lon in DataStore, do not keep a location listener.
2. **Manual city:** `Geocoder.getFromLocationName` (framework API, not Play) debounce while typing; picker shows city + region + country; selecting a row stores that city’s centroid.
3. City is the determining factor: two streets in the same city share one lat/lon.

## Solar toggles

Independent on/off (default off):

- Sunrise, sunset
- Dawn, dusk (civil)
- Twilight — civil, nautical, and astronomical (each end as its own toggle, or paired rise/set per kind)
- Solar noon, solar midnight
- Golden hour (morning and evening)
- Blue hour (morning and evening)
- March equinox, September equinox
- June solstice, December solstice

## Lunar toggles

Independent on/off (default off):

- Moonrise, moonset
- New moon, full moon
- Waxing crescent, first quarter, waxing gibbous
- Waning gibbous, last quarter, waning crescent

## Alarm UX

- Schedule the next enabled instant with `setExactAndAllowWhileIdle` (opt-in `SCHEDULE_EXACT_ALARM` rationale). Prefer a high-priority full-screen notification + `USE_FULL_SCREEN_INTENT` + `showWhenLocked` / `turnScreenOn` so the popup can appear over the lockscreen like a clock alarm.
- Do **not** add Play. Do **not** log the schedule time together with coordinates.
- TTS loops the event phrase on the alarm stream until **Dismiss**. Optional short snooze (one cycle) if we already have a snooze pattern; dismiss is required.
- History: write a `kind` + spoken row, no coordinates.

## Smoke scenario

1. Given no location stored, the user types “Austin” and picks the city row (or grants one-time coarse location).
2. When they enable Sunset and Full moon.
3. Then the next sunset and the next full-moon instant each open the lockscreen alarm and shout until dismissed. Turning a toggle off cancels that series.

## Container map

| Layer | Path |
|-------|------|
| Logic | `org/openshouter/astro/` (sun file, moon file, schedule file; 150-line cap each) |
| Place | `org/openshouter/astro/AstroPlace.kt` (`LocationManager` + `Geocoder`) |
| View | `org/openshouter/ui/astro/` |
| Alarm | `org/openshouter/astro/AstroAlarmActivity` + receiver |
| Tests | `org/openshouter/astro/` unit tests with fixed lat/lon fixtures (no live GPS) |
| Wiring | `AnnouncerService` + Dashboard row ≤10 lines |

## Tests

- Automated: yes — solar/lunar instants for a known city/date; toggle off cancels; geocode uses locality; sanitizer never sees raw coords in logs (assert log API not called with numbers).
- Device: `[ADB]` lockscreen popup + dismiss on CPH2583 / CPH2655.

## Fallback validation

- Why tests are not feasible: N/A (math + schedule unit tests exist). Lockscreen chrome is `[ADB]`.
- Command: `python3 scripts/agent-run.py feature-gate --stack android`

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty city | Toggles stay off; empty Geocoder list shows “no city match”; test blank query |
| Network timeout | Geocode is best-effort; after persist, math is offline. No weather API |
| Race | One `AstroSchedule` queue; last-wins per event id; dismiss clears the looping shout |
| Unhandled exceptions | `runCatching` on Geocoder and alarm set; skip that fire |
| PII | Persist city label + lat/lon locally; never log them |
| Bedtime / `setAlarmClock` | Use full-screen intent + exact-while-idle, not `setAlarmClock`, unless a later HUMAN row asks to take over bedtime |
| Play Services | Forbidden — `LocationManager` + framework `Geocoder` only |

## Out of scope

- Live crash-proxy, Play location, weather, street-level address, always-on GPS
