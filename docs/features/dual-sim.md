# Feature: dual-SIM line in caller ID

`%sim` token in the call format. Resolve the line from `ACTION_PHONE_STATE_CHANGED` extras + `SubscriptionManager`. Do not log numbers.

## Acceptance criteria

- `TtsFormat.call` substitutes `%sim`
- One active subscription → speak that display name
- Two+ SIMs → match slot/subscription extra; unknown → blank (do not guess)
- Numbers never appear in logs or `toString()` of helpers

## Smoke scenario

1. Dual-SIM device: incoming call on SIM 2
2. Format `Incoming call from %name on %sim`
3. TTS includes the SIM display name; logcat has no digits from the number

## Container map

| Layer | Path |
|-------|------|
| Logic | `org/openshouter/telephony/SimLine.kt` |
| Format | `org/openshouter/domain/TtsFormat.kt` (`%sim`) |
| Wire | `CallMonitor` / `CallChannel` ≤ few lines |
| Tests | `org/openshouter/telephony/SimLineTest.kt` |

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty SIM label | Leave `%sim` empty; whitespace collapsed by `TtsFormat.render` |
| Network timeout | N/A |
| Race | Last resolved label kept for TelephonyCallback path |
| Unhandled exceptions | `runCatching` on `SubscriptionManager` |

## Tests

- Automated: yes — Android unit tests under `examples/android/app/src/test/`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
