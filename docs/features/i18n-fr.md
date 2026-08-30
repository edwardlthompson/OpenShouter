# Feature: French translation pack

Second locale overlay after Spanish. Compose screens keep using `stringResource`.

## Acceptance criteria

- ✅ `res/values-fr/strings.xml` has the same `name` keys as `res/values/strings.xml`
- ✅ Device language `fr` shows French labels
- ✅ Missing keys fail `I18nEsTest.frenchKeysMatchEnglish`

## Smoke scenario

1. Set emulator/device language to French
2. Open welcome and Announcer
3. Section titles and toggles are French; format tokens stay as tokens

## Container map

| Layer | Path |
|-------|------|
| Copy | `examples/android/app/src/main/res/values-fr/strings.xml` |
| Spec | `docs/features/i18n-fr.md` |
| Tests | `examples/android/app/src/test/java/org/openshouter/i18n/I18nEsTest.kt` |
| Wiring | none (resource overlay) |

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | Key-parity test rejects missing names |
| Network timeout | N/A |
| Race | N/A — static resources |
| Unhandled exceptions | XML parse failures fail the unit test |

## Tests

- Automated: yes — Android unit tests under `examples/android/app/src/test/`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
