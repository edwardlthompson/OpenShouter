# Feature: feedback

> About / Help review dialogs for bug and feature reports. Not a donate nag.

## Acceptance criteria

- ✅ User-visible behavior: About has Report a bug and Request a feature; review panel shows escaped preview, Copy, Open GitHub, Discard
- ✅ Offline/error behavior: Copy still works; Open GitHub is https-only
- ✅ Accessibility: labelled buttons; no Android Toast
- ✅ i18n: `feedback_*` in `strings_feedback.xml`

## Smoke scenario

1. Given crash-capture is off
2. When the user opens About and Report a bug, types a description
3. Then they can copy sanitized markdown; Open GitHub is enabled only when description or stack exists

## Container map

| Layer | Path |
|-------|------|
| View | `org/openshouter/ui/feedback/` |
| Logic | `org/openshouter/feedback/` |
| Tests | `src/test/java/org/openshouter/feedback/` |
| Wiring | `GoldenPathApp.kt` / `GoldenPathScreen.kt` ≤10 lines |

## Tests

- Automated: yes — `FeedbackPreviewTest.kt`, `FeedbackPrefsTest.kt`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
