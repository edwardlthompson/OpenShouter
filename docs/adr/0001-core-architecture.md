# ADR-0001: Core Application Architecture

- **Status:** Proposed (awaiting `[HUMAN]` approval)
- **Date:** 2026-08-13
- **Deciders:** OpenShouter maintainers

## Context

OpenShouter is a long-lived Android accessibility/utility app with many event sources (notifications, telephony, sensors, battery, location) feeding one TTS pipeline. We need a structure that keeps domain rules testable without Robolectric for every branch, while Compose UI stays thin.

## Decision

**Selected pattern:** Clean Architecture with MVVM at the UI boundary.

| Layer | Responsibility | Android types allowed |
|-------|----------------|------------------------|
| Domain | Pure use cases and models (`SpokenEvent`, `AnnouncementGate`, format strings) | None (JVM unit tests) |
| Data | Room, DataStore, system listeners as adapters | Room, DataStore, platform callbacks behind interfaces |
| Presentation | Jetpack Compose + ViewModels | Compose, Hilt ViewModel, Navigation |

- **DI:** Hilt (FOSS, Apache-2.0). Koin rejected to stay aligned with AndroidX/Hilt docs and compile-time graphs.
- **Async:** Kotlin Coroutines + Flow.
- **Persistence:** Room for notification history, app rules, regex rules, geofence places. DataStore for master on/off, TTS prefs, quiet-hours flags.
- **Application ID:** `org.openshouter`
- **SDK:** min 26 (Android 8.0), compile/target 35 (Android 15).
- **UI:** Jetpack Compose + Material 3; strings in `res/values/strings.xml` only.

## Consequences

- Golden Path package `dev.foss.goldenpath` is a temporary stub until Sprint 1 relocates to `org.openshouter`.
- Shared schema/types (domain models, Room entities) are Sequential-only in BUILD_PLAN.
- CI coverage gates apply per `examples/android/` conventions.
- Changing this ADR later requires a new ADR and BUILD_PLAN `[HUMAN]` approval.

## Alternatives Considered

| Pattern | Rejected because |
|---------|------------------|
| Hexagonal only | Extra port/adapter ceremony for a single Android delivery surface |
| MVC Activities | Poor fit for Compose + multi-service event bus |
| Koin | Runtime graph; Hilt is the AndroidX default and still FOSS |
| Google Play Services as platform layer | Violates Module A FOSS isolation (see ADR-0002) |
