# Feature: sun-moon-alarm

> Replace **Sun Alarm** (`com.vvse.sunalarm`, Volker Voecking) with a FOSS lockscreen alarm that actually appears over the lockscreen. One stored place (one-time fix or typed city). On-device sun/moon math. `AlarmManager.setAlarmClock` + **Snooze** / **Stop**. Home widget: fixed “now” hands, rotating day/night disk. No Play Services.

## Acceptance criteria

- 🔲 User-visible behavior: a Sun/Moon alarm menu lists events with optional before/after offsets. At fire time the system alarm clock path shows a full-screen activity **over the lockscreen** and shouts the event until **Snooze** or **Stop**.
- 🔲 Widget: home-screen analog disk (day white / night black) with **hands fixed pointing up** (“now”). The **background rotates** so the current instant is always at the top. Digital times for sunrise, sunset, solar noon, and solar midnight; a red dot on any of those (and any other events) that have an alarm.
- 🔲 Place: one-time `ACCESS_COARSE_LOCATION` via `LocationManager` (never `play-services-location`), **or** a city search field that resolves while typing. The **city** (locality) is the stored place, not a street address.
- 🔲 Offline/error: after a place is stored, event times are computed on-device (no weather API). Failed geocode leaves the last good city. Missing permission with no city stored keeps alarms unset and shows why.
- 🔲 Accessibility: **Snooze** and **Stop** are labelled buttons (not icon-only); TalkBack reads the event title. Widget `contentDescription` speaks next rise/set, not a picture-only face.
- 🔲 i18n: `astro_*` keys in `strings_astro.xml` (en + es + fr overlays).
- 🔲 PII: never log coordinates, city strings, or alarm copy.

## Place

1. **One-time permission:** request coarse location once, read one `LocationManager` fix, reverse-geocode to a city label, persist city + lat/lon + time zone, do not keep a location listener.
2. **Manual city:** `Geocoder.getFromLocationName` (framework API, not Play) debounce while typing; picker shows city + region + country; selecting a row stores that city’s centroid and zone.
3. City is the determining factor: two streets in the same city share one lat/lon.

## Alarm UX (must beat Sun Alarm on lockscreen)

- Schedule with **`AlarmManager.setAlarmClock`** so the OS treats this as a clock alarm (status-bar alarm indicator, Doze exemption, lockscreen). Also `USE_FULL_SCREEN_INTENT` + `showWhenLocked` / `turnScreenOn` + `SCHEDULE_EXACT_ALARM` rationale.
- **Stop:** halt TTS, sound, and vibration, close the full-screen UI, mark this occurrence done, and schedule the next event.
- **Snooze:** halt output, close the UI, and `setAlarmClock` again after the snooze interval (default 10 minutes, user-settable). Does not skip the event.
- Do **not** use `SYSTEM_ALERT_WINDOW` as the primary popup (that is why Sun Alarm often fails over the lockscreen).
- Do **not** add Play. Do **not** log the schedule time together with coordinates.
- History: write a `kind` + spoken row, no coordinates.
- `setAlarmClock` **does** take over bedtime / next-alarm UI. That is intended: this product is a clock alarm, not a quiet TIME_TICK shout.

## Solar events

Independent alarms (default off), each with optional offset minutes before/after:

- Sunrise, sunset
- Dawn, dusk (civil)
- Civil / nautical / astronomical twilight (each end)
- Solar noon, solar midnight
- Golden hour (morning and evening)
- Blue hour (morning and evening)
- March equinox, September equinox
- June solstice, December solstice

## Lunar events

Independent alarms (default off), each with optional offset:

- Moonrise, moonset, moon transit (highest)
- New moon, full moon
- Waxing crescent, first quarter, waxing gibbous
- Waning gibbous, last quarter, waning crescent

## Weather API?

**Do not add a weather API to make event times “more accurate.”** Sunrise, twilight, and moon instants are geometry (lat/lon, date, refraction model). A forecast does not move those clocks except by seconds of refraction, which NOAA-style math already models. Sun Alarm’s own Play Data safety says it collects and shares **no** data — it computes locally too.

A later **opt-in** “conditions at this event” (Open-Meteo or similar, no SDK, city only, never default-on) could say “cloudy at sunset.” That is a forecast feature, not a scheduling feature. It sends a place off-device. Keep it off the v1 lockscreen alarm.

## vs Sun Alarm (`com.vvse.sunalarm`)

Phones were **not** attached to this agent (`adb devices` empty). Comparison is from Play (`com.vvse.sunalarm`), [vvse.com/sunalarm](https://www.vvse.com/products/en/sunalarm.html), AppBrain/reviews, and the iOS sibling listing (same developer family). Re-walk on CPH2583 / CPH2655 when ADB is up.

### They have (Android listing)

| Feature | Integrate? |
|---------|------------|
| Sunrise, sunset, noon | **Yes** — already in this spec |
| Civil / nautical / astronomical twilight | **Yes** |
| Golden hour, blue hour | **Yes** |
| Offset: at event, or N minutes/hours before/after | **Yes — add** (their main UX) |
| GPS or manual location | **Yes** — one-time coarse or typed city |
| Per-alarm sound / vibrate | **Yes — add** (reviews: vibrate-only often broken there) |
| Free cap of 2 alarms + Pro IAP + ads | **No** — unlimited, no Play Billing |
| Overlay / notification-only popup | **No** — that is the bug we replace |

### Reviews say they want / we should take

| Feature | Integrate? |
|---------|------------|
| Reliable lockscreen + `setAlarmClock` | **Yes — required** |
| Snooze / Stop that actually silences | **Yes — required** |
| Moonrise, moonset, moon transit, phases | **Yes** — already in this spec |
| Solar midnight | **Yes** — already in this spec |
| Custom sun altitude (e.g. sun 5° above horizon) | **Later** — photographer P2 |
| Multiple named alarms per event (coop −15m and photo −0) | **Yes — add** named rows, not one toggle per event only |
| Next-event times listed on the menu | **Yes — add** |
| Unrestricted battery / OEM autostart hint | **Yes** — reuse Sprint 35 OEM row + Welcome |
| Widget of next sunrise/sunset | **Yes — rotating day/night disk** (below) |

### Skip

- Play ads / IAP / “weather app” store category
- Always-on GPS
- Weather/radar for scheduling
- `READ_SMS`, GMS maps (FOSS map picker is Sprint 30)

## Rotating day/night widget

Not the existing master on/off widget. New `astro` provider (Glance or `RemoteViews` + a painted bitmap — App Widgets cannot spin a Compose tree).

- **Hands stay still** and point **up** = now. Unlike a normal analog clock, **the disk rotates** under those hands as time passes so “now” is always at the top (12 o’clock).
- **Day = white sector, night = black sector.** Sector sizes follow today’s sunrise→sunset and sunset→sunrise (not a fixed 50/50 except near equinox). Polar day/night: one sector can fill the disk.
- **Sun and moon** glyphs sit on the disk at rise/set (and current altitude if we have it) so they ride with the rotating background.
- **Digital labels** (clock time in the user’s 12/24 setting) at:
  - night → day (sunrise)
  - day → night (sunset)
  - solar noon
  - solar midnight
- **Red dot** beside any of those four that have an enabled alarm. **Also draw a labelled tick + digital time + red dot for every other enabled alarm** (golden hour, moonrise, offset rows, …).
- Update at least on `TIME_TICK` / widget period (once a minute is enough; do not log coords).
- Pure-logic helper: `diskAngle(now, eventInstant)` so “now” maps to 0° (up). Unit-test a known city/date.

## Smoke scenario

1. Given no location stored, the user types a city and picks the row (or grants one-time coarse location).
2. When they add “Sunset −15m” and “Full moon”.
3. Then `setAlarmClock` shows those as next-alarm; at fire the lockscreen shows **Snooze** / **Stop** and OpenShouter shouts until one of those. The widget disk has now at the top, sunset labelled, and red dots on sunset and full moon.

## Container map

| Layer | Path |
|-------|------|
| Logic | `org/openshouter/astro/` (sun file, moon file, schedule file; 150-line cap each) |
| Place | `org/openshouter/astro/AstroPlace.kt` (`LocationManager` + `Geocoder`) |
| View | `org/openshouter/ui/astro/` |
| Widget | `org/openshouter/astro/AstroWidgetProvider` + `res/xml/astro_widget_info.xml` |
| Alarm | `org/openshouter/astro/AstroAlarmActivity` + `setAlarmClock` receiver |
| Tests | `org/openshouter/astro/` unit tests with fixed lat/lon fixtures (no live GPS) |
| Wiring | `AnnouncerService` + Dashboard row ≤10 lines |

## Tests

- Automated: yes — instants for a known city/date; offset math; Snooze reschedules; Stop cancels this fire; disk angle keeps now at 0°; geocode uses locality.
- Device: `[ADB]` lockscreen Snooze/Stop + widget rotation on CPH2583 / CPH2655 vs Sun Alarm’s miss.

## Fallback validation

- Why tests are not feasible: N/A (math + schedule unit tests exist). Lockscreen chrome is `[ADB]`.
- Command: `python3 scripts/agent-run.py feature-gate --stack android`

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty city | Alarms stay unset; empty Geocoder → “no city match”; test blank query |
| Network timeout | Geocode best-effort; times compute offline. No weather API on the schedule path |
| Race | One `setAlarmClock` at a time (OS next-alarm); Snooze re-arms this event; Stop queues the next |
| Unhandled exceptions | `runCatching` on Geocoder and `setAlarmClock`; skip that fire |
| PII | Persist locally; never log lat/lon or city |
| Bedtime | Accepted: `setAlarmClock` owns next-alarm / bedtime, same as Clock |
| Play Services | Forbidden — `LocationManager` + framework `Geocoder` only |
| Overlay leftover | Do not request `SYSTEM_ALERT_WINDOW` for the alarm UI |

## Out of scope (v1)

- Weather/forecast API, live crash-proxy, Play location, street-level address, always-on GPS, custom sun-altitude editor, spinning Compose hands (disk is a bitmap under fixed hands)
