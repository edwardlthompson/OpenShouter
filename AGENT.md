# AGENT.md — original product brief (Sacred)

OpenShouter product brief. Bootstrap stamps `AGENTS.md` only and will not overwrite this file.

<!-- agent-brief:one-liner -->
FOSS Android TTS announcer for notifications and Shouter-style telephony/hardware.
<!-- /agent-brief:one-liner -->

<!-- agent-brief:keywords -->
notification, announcer, FOSS, Shouter, TTS, geofence, caller
<!-- /agent-brief:keywords -->

## Rules

- FOSS MIT; no Google Play Services, Firebase, Play Core, or closed telemetry.
- Geofences via LocationManager + in-process radius checks only.
- Never log notification text, phone numbers, contact names, or coordinates.
- Package `org.openshouter`; strings in `res/values/strings.xml` only.
