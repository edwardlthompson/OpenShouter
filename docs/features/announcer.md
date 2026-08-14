# OpenShouter announcer

GitHub Releases only (not F-Droid, not Play Store).

## Behavior

- Notification TTS with `%app` / `%title` / `%text` format strings, per-app filters, and regex ignore/replace
- Looping caller ID until answer, reject, or idle; contact name when `READ_CONTACTS` is granted
- Battery / charger phrases, shake-to-silence, face-down mute, screen on/off mute
- Quiet hours, screen-off-only, headset/A2DP-only gate
- In-process geofences via `LocationManager` (no Play Services)
- Master mute shared by dashboard, Quick Settings tile, widget, and foreground service

Parity vs Shouter Pro and Voice Notify: [`docs/features/parity-matrix.md`](parity-matrix.md) (Sprints 9–11 after plan approval).

## Distribution

Release APKs from GitHub Releases. In-app update check uses the GitHub Releases API when `release_repo` is set.
