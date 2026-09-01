# Feature: sun-moon-alarm (moved)

> **Status:** Removed from OpenShouter. The sun/moon/clock alarm product now lives in the standalone app **[AstroAlarm](https://github.com/edwardlthompson/AstroAlarm)**.

OpenShouter keeps a home-menu recommendation that opens the AstroAlarm GitHub repository. In-app alarm scheduling, lockscreen activity, widgets, and `astro_*` string packs were deleted from `examples/android/`.

## Why

Alarm scope grew beyond OpenShouter’s TTS announcer focus. Splitting keeps OpenShouter on notification/telephony/hardware shouts while AstroAlarm owns astronomical and clock alarms.

## Acceptance criteria

- ✅ In-app Astro alarm UI, scheduler, widget, and lockscreen activity are removed from OpenShouter.
- ✅ Home menu shows **Try AstroAlarm** near Settings/Donate with a one-sentence pitch and opens `https://github.com/edwardlthompson/AstroAlarm`.
- ✅ No `org.openshouter.astro` or `ui.astro` packages remain in `examples/android/`.
- ✅ PII: no alarm coordinates or event copy logged (feature code gone).

## Critique

| Issue | Resolution |
|---|---|
| Users lose in-app alarms | Promo row + AstroAlarm repo; documented in CHANGELOG |
| Stale feature-spec sections | This file kept as moved status with required contract headings |
| Null/empty promo URL | `AstroAlarmLinks.GITHUB_URL` constant; no network fetch in-app |

## Container map

- Promo UI: `examples/android/app/src/main/java/org/openshouter/ui/dashboard/DashboardScreen.kt`
- URL constant: `examples/android/app/src/main/java/org/openshouter/updates/AstroAlarmLinks.kt`
- Strings: `astroalarm_promo_*` in `res/values*/strings.xml`
- Standalone product: https://github.com/edwardlthompson/AstroAlarm

## Smoke scenario

1. Open OpenShouter home menu → Phone and backup (bottom of the list).
2. Tap **Try AstroAlarm** (above Donate) → browser opens the AstroAlarm GitHub page.

## Tests

- Automated: no — feature code removed from this repo; AstroAlarm owns its tests.
- Why tests are not feasible: no remaining alarm logic under `examples/android/` to unit-test; promo is a static URI open.

## Fallback validation

- `python3 scripts/agent-run.py feature-gate --stack android`
