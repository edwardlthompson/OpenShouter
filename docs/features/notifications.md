# Notification reader

Speak notifications with `%app` `%title` `%text` format strings, per-app allow/deny lists, regex ignore/replace, and Room history.

Gap vs Voice Notify (app picker, per-app overrides, delay/stream/log UI): [`parity-matrix.md`](parity-matrix.md).

## Acceptance

- Listener bound; filtered spam is silent; history row stored locally
- History rows open a mute dialog (OpenShouter app toggle + that notification channel in system settings)
