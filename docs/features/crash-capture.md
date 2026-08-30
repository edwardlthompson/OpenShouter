# Feature: crash-capture

> Opt-in local crash queue. Never auto-sends. Sanitize before persist.

## Acceptance criteria

- ✅ User-visible behavior: after a captured crash, one review dialog; never auto-open GitHub
- ✅ Offline/error behavior: write failure drops the record; handler errors do not re-enter
- ✅ Accessibility: same dialog contract as Feedback
- ✅ i18n: uses `feedback_*`

## Smoke scenario

1. Given the save-crashes setting is off
2. When an unhandled error occurs
3. Then nothing is persisted
4. When the setting is on, at most one sanitized record is stored; turning the setting off deletes it

## Container map

| Layer | Path |
|-------|------|
| Logic | `org/openshouter/crashcapture/` |
| Tests | `src/test/java/org/openshouter/crashcapture/` |
| Wiring | `OpenShouterApp.onCreate` ≤10 lines |

## Tests

- Automated: yes — `PendingCrashTest.kt`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
