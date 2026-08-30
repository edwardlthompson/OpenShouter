# Feature: Sprint 17 hear-quality extras

Close silent-failure gaps after v0.4.0: history shows why a shout was dropped, setup asks for Apps to shout, test posts bypass the allowlist, repeats collapse, importance and Priority-DND filters, calendar look-ahead chips, French overlay.

## Acceptance criteria

- ✅ SpeakGate misses write `IgnoreReason` (no spoken payload) to history
- ✅ Welcome includes an Apps to shout row; Continue still works for calls-only users
- ✅ OpenShouter test-channel posts speak without an app-speak rule
- ✅ Later REPEAT rows in a 60s burst are not stored when collapse is on
- ✅ Filters can require Low / Default / High importance
- ✅ Priority DND can still shout HIGH or `CATEGORY_CALL` when silent-speak is off
- ✅ Calendar look-ahead is 5 / 15 / 30 minutes
- ✅ `values-fr/strings.xml` key-parity with English (`I18nEsTest`)

## Smoke scenario

1. Enable a silent place or quiet hours, post a matching notification
2. History shows Skipped: device or quiet gate (no body unless Show spoken is on)
3. Voice → Post test notification speaks on the selected stream without adding OpenShouter to Apps to shout

## Container map

| Layer | Path |
|-------|------|
| Logic | `org/openshouter/domain/AnnouncementGate.kt`, `NotificationPolicy.kt`, `NotificationRank.kt` |
| View | Filters, Setup, Calendar, History labels |
| Tests | `NotificationPolicyTest`, `NotificationRankTest`, `CalendarShoutTest`, `I18nEsTest` |
| Wiring | `NotificationPosted` + DataStore `SettingsSprint17` |

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | Blank utterances still skip; test channel requires `openshouter-test` |
| Network timeout | N/A — no network I/O |
| Race | `RepeatClock` is listener-local; first REPEAT is stored, later burst rows collapse |
| Unhandled exceptions | Gate/datastore failures skip the event; history insert is `runCatching`-free Room (existing) |

## Tests

- Automated: yes — Android unit tests under `examples/android/app/src/test/`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
