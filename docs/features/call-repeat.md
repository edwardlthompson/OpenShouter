# Feature: call repeat once vs until answered

WhatsApp (and other VoIP) incoming calls speak **once** by default. Cellular Phone calls still **repeat until answered**. Apps to shout stays independent of incoming-call policy.

## Acceptance criteria

- ✅ Cellular (`CallMonitor`) loops until answered, rejected, or idle; DataStore is ignored on this path
- ✅ VoIP packages default to `CallRepeatMode.ONCE` when unset; optional Until answered; `OFF` skips **calls** only
- ✅ WhatsApp **messages** stay on Apps to shout / the message channel
- ✅ Same notification key is not announced twice; in-call, ended, or removed interrupts TTS and clears the session
- ✅ VoIP `ONCE` builds `SpokenEvent` with `repeatCount = 0` (CALL channel extras cannot turn once into four plays)
- ✅ `MODE_IN_COMMUNICATION` interrupts only while `CallLoopGate` has an active **VoIP** package
- ✅ Cellular RINGING writes **one** history row (`com.android.phone`, empty title/text)
- ✅ Apps to shout and history dialog expose Incoming calls for messaging/VoIP packages
- ✅ TalkBack: Incoming calls dropdown uses the visible label
- ✅ i18n: `apps_call_repeat_*` / `history_call_*` in `strings.xml` / `strings_history.xml` (en/es/fr)

## Smoke scenario

1. _Given_ notification access is on and WhatsApp Incoming calls is Once (default)
2. _When_ a WhatsApp call rings, then the user answers
3. _Then_ OpenShouter speaks once and does not keep looping; a later new incoming call can shout again
4. _Given_ a cellular incoming call
5. _When_ the phone rings until answer or idle
6. _Then_ TTS loops until `OFFHOOK`/`IDLE`, and history shows one CALL row with no number

## Container map

| Layer | Path |
|-------|------|
| Spec | `docs/features/call-repeat.md` |
| Logic | `domain/CallRepeatMode.kt`, `call/VoipCallPhase.kt`, `call/CallAnnounceSession.kt`, `call/CallLoopGate.kt`, `call/CallPosted.kt` |
| View | `ui/apps/AppSpeakScreen.kt`, `ui/call/CallRepeatDropdown.kt`, `ui/history/HistoryMuteDialog.kt` |
| Tests | `VoipCallPhaseTest`, `CallAnnounceSessionTest`, `CallRepeatModeTest`, `CallNotificationTest`, `CallAndPowerTest`, backup + `HistoryEntityTest` |
| Wiring | NLS `onNotificationRemoved` + `NotificationPosted`; `CallMonitor` history; Room v6; DataStore `call_repeat` |

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty CallStyle extras | `VoipCallPhaseLogic` treats missing type as unknown; first ongoing VoIP post is `INCOMING`. Test: `VoipCallPhaseTest` |
| Null/empty `sbn.key` | `CallAnnounceSession` falls back to package name; still cleared on removed/ended. Test: `CallAnnounceSessionTest` |
| Blank DataStore package / bad enum | Parser skips blank keys; unknown mode → default ONCE. Test: `CallRepeatModeTest` |
| Network timeout | N/A — no network I/O |
| Race: WhatsApp updates the same ongoing notification | Same `package+key` is ignored after the first announce until ENDED/removed. Artifact: `CallAnnounceSession` |
| Race: second WhatsApp call after the first | Removed/ENDED/in-call clears the session so a new key can announce. Test: `CallAnnounceSessionTest` |
| Race: ONCE vs CALL `repeatCount` extra plays | VoIP ONCE builds `SpokenEvent` with `repeatCount = 0`. Test: `CallRepeatModeTest` |
| Race: `MODE_IN_COMMUNICATION` vs cellular ring | Interrupt only when `CallLoopGate` active package is VoIP. Artifact: `TtsController` onDone |
| Unhandled exceptions | CallStyle extras via `runCatching`; `onNotificationRemoved` must not throw |
| PII in logs or history | No title/text/numbers/names in logcat. Cellular history title/text empty |
| Apps to shout off disables WhatsApp calls | Independent `CallRepeatMode`; messages use Apps to shout |
| Room v6 crash on upgrade | `MIGRATION_5_6` + register in `DatabaseModule`; kind default `'NOTIFICATION'` |
| Backup drop of new prefs | Encode/decode `callRepeatModes` in SettingsBackup/BackupImport |
| CallMonitor duplicate history | One insert per RINGING session, not on the 400ms retry |
| Time/battery/reminder not in Apps to shout | Already have Announcer channel toggles; not required to fix WhatsApp persist-loop |
| Listener `onNotificationRemoved` while disconnected | No-op if gate session empty; no TTS call |

## Notes

- Time / battery / reminder / calendar / Bluetooth stay on existing Announcer channel toggles.
- After each AGENT step: `python3 scripts/agent-run.py watch-agent-gates --once --autofix`
