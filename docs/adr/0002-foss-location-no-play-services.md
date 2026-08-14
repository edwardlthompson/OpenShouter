# ADR-0002: FOSS Location and Geofencing (No Play Services)

- **Status:** Proposed (awaiting `[HUMAN]` approval)
- **Date:** 2026-08-13
- **Deciders:** OpenShouter maintainers

## Context

The original product brief asked for Google Play Services Location / Geofencing APIs so users can auto-toggle announcement modes at Home/Work. `agent-project-bootstrap` Module A and `.cursor/rules/foss-compliance.mdc` forbid proprietary SDKs (`play-services-location`, Firebase, Play Core). OpenShouter ships GitHub Releases only and still must not ship GMS binaries.

## Decision

Implement geofencing **without** Play Services:

1. Request `ACCESS_FINE_LOCATION`, then `ACCESS_BACKGROUND_LOCATION` with a dedicated rationale screen.
2. Use Android `LocationManager` (and/or AndroidX `PlayServices`-free fused providers if any remain AOSP-only — default to `LocationManager`).
3. Evaluate circular fences in-process (haversine distance vs radius) on location updates and boot/package-restart.
4. Persist places (label, lat, lng, radius, enter/exit actions) in Room.
5. If a map picker is needed later, use a FOSS map (OSMDroid / MapLibre) — never Google Maps SDK.

The original Shouter-style *behavior* (Home/Work silent or announce modes) is in scope. The GMS *implementation* is not.

## Consequences

- Fence enter/exit may be less power-efficient than GMS hardware geofencer; mitigate with balanced location intervals and user-configurable radius.
- Background location is a Play-policy and user-trust sensitive permission; copy must match `docs/PRIVACY.md`.
- Gradle FOSS grep must fail the build if `com.google.android.gms` appears in manifests.

## Alternatives Considered

| Option | Rejected because |
|--------|------------------|
| `play-services-location` GeofencingClient | Proprietary; Module A / FOSS violation |
| microG as a runtime dependency | Extra user setup; not hermetic FOSS in our APK |
| Skip geofencing entirely | Drops a stated Shouter parity feature |
