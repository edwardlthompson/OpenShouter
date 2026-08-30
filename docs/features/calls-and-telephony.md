# Feature: Calls and telephony (Sprint 28)

Call enhancements for cellular and VoIP telephony in OpenShouter:
1. Dedup cellular + VoIP double shout into one utterance
2. Bluetooth HFP caller ID on the headset path
3. Second-call / call-waiting announce
4. Speak after hangup (duration)
5. Conference / merge hint when more than one participant

## Acceptance criteria

- ✅ **Dedup**: If a cellular incoming call is already active/announced on `CallMonitor` or VoIP arrives with the same number/contact within 5 seconds, duplicate utterances are suppressed.
- ✅ **Bluetooth HFP**: Detect Bluetooth SCO / HFP audio routing and format caller ID for the headset path without echoing over the main speaker.
- ✅ **Second call / call waiting**: When a second incoming call rings while offhook / in a call, announce call waiting ("Call waiting from %name").
- ✅ **Speak after hangup**: When configured, announce call end / duration ("Call ended, duration %duration").
- ✅ **Conference hint**: When multiple participants are detected on an incoming/active call, announce conference / multi-party hint ("Conference call with %count participants" or "Conference call from %name").
- ✅ Local unit tests for telephony logic, dedup, call waiting, hangup duration, and conference formats.

## Critique

| Issue | Resolution |
|---|---|
| Concurrent VoIP & cellular broadcasts | `CallDedup` keeps short timestamp cache to suppress double-shout |
| Missing permissions for Bluetooth SCO/HFP | Graceful fallback to standard TTS audio routing |
| Zero duration on missed / instant hangup | Only announce duration if call was in OFFHOOK / active state |
| Rapid repeated call-waiting state events | Rate-limited call-waiting announcements |
