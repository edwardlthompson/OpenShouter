# Third-Party Licenses

> Generated and maintained per release. See pre-release gate in `docs/INITIALIZATION_PROMPT.md` Section 7a.

## Project License

This project is licensed under the MIT License. See [`LICENSE`](LICENSE).

## Dependencies

Android (Gradle) is the only active stack:

```bash
cd examples/android
./gradlew :app:dependencies --configuration releaseRuntimeClasspath
```

`[AUTO]` CI runs `scripts/check-license-compliance.sh` on each push.

## Attribution

When bundling dependencies in APK releases, include this file or a generated `NOTICE` in the distribution artifact.

## Incompatible Licenses

`[HUMAN]` must approve any dependency with copyleft licenses (GPL, AGPL) that may affect distribution. Document exceptions in `DECISION_LOG.md`.

Hilt/Dagger, AndroidX, and Room are Apache-2.0 and expected.
