# Feature: Spanish translation pack

First locale overlay. Compose screens keep using `stringResource`; no hardcoded user-facing literals.

## Acceptance criteria

- ✅ `res/values-es/strings.xml` has the same `name` keys as `res/values/strings.xml`
- ✅ Device language `es` shows Spanish labels, including setup and shout screens
- ✅ Missing keys fail `I18nEsTest` (Gradle unit test)

## Smoke scenario

1. Set emulator/device language to Spanish
2. Open welcome and Announcer
3. Section titles and toggles are Spanish; format tokens (`%app`, `%sim`) stay as tokens

## Container map

| Layer | Path |
|-------|------|
| Copy | `examples/android/app/src/main/res/values-es/strings.xml` |
| Spec | `docs/features/i18n-es.md` |
| Tests | `examples/android/app/src/test/java/org/openshouter/i18n/I18nEsTest.kt` |
| Wiring | none (resource overlay)

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | Key-parity test rejects missing names |
| Network timeout | N/A |
| Race | N/A — static resources |
| Unhandled exceptions | XML parse failures fail the unit test |
