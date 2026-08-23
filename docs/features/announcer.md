# OpenShouter announcer

GitHub Releases only (not F-Droid, not Play Store).

## Behavior

- Notification TTS with `%app` / `%title` / `%text` format strings, per-app filters, and regex ignore/replace
- Looping caller ID until answer, reject, or idle; contact name when `READ_CONTACTS` is granted
- Battery / charger phrases, shake-to-silence, face-down mute, screen on/off mute
- Quiet hours, screen-off-only, headset/A2DP-only gate
- In-process geofences via `LocationManager` (no Play Services)
- Master mute shared by dashboard, Quick Settings tile, widget, and foreground service
- Foreground service is `specialUse|mediaPlayback` so Android 16 AudioHardening does not mute the release APK
- Voice picker uses dropdowns: Quality → Language → Accent → Voice, then TTS source. Quality is a hard filter: Very high hides High and Normal models. Default is very high. Language list merges installed engines with RHVoice and SherpaTTS catalogs; a missing pack opens that app (or F-Droid if the engine is not installed). Exclusive setting chips elsewhere (stream, interval, theme, filters) are the same dropdown control. Quiet Hours keeps the 24-hour grid and day multi-select.

Parity vs Shouter Pro and Voice Notify: [`docs/features/parity-matrix.md`](parity-matrix.md) (Sprints 9–11 after plan approval).

## Distribution

Release APKs from GitHub Releases. Daily in-app update check compares `openshouter-X.Y.Z-foss.apk` filenames (not git tags). Quiet **Donate via Venmo** lives on About and the home Menu; a one-time ethical reminder appears only after a version change. See [`donations-updates.md`](donations-updates.md).
