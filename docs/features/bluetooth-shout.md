# Feature: Bluetooth connect and headset-battery shout

ACL connect/disconnect and optional battery-level extra. FOSS `BluetoothAdapter` / broadcasts only.

## Acceptance criteria

- Off by default (connect and battery toggles)
- API 31+ needs `BLUETOOTH_CONNECT`; older `BLUETOOTH`
- Skip blank device names and battery values outside 0–100
- Never log device names

## Smoke scenario

1. Enable Bluetooth connect shout; pair/connect a headset
2. Hear “{name} connected”
3. If the stack sends a battery extra, enable battery shout and hear the percent

## Container map

| Layer | Path |
|-------|------|
| Logic | `org/openshouter/bluetooth/BluetoothShout.kt` |
| Monitor | `org/openshouter/bluetooth/BluetoothMonitor.kt` |
| View | `org/openshouter/bluetooth/BluetoothShoutScreen.kt` |
| Tests | `org/openshouter/bluetooth/BluetoothShoutTest.kt` |
| Wiring | `AnnouncerService` start + panes ≤10 lines |

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty name | `BluetoothShout.deviceName` → skip |
| Network timeout | N/A |
| Race | Single TTS queue |
| Unhandled exceptions | `runCatching` on `device.name`; skip |
