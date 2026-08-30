# Feature: silence competing sounds

Ship a valid silent notification/ringtone file, teach first-run setup how to use it, list notification channels that still have a sound, optionally set that file as the system default, and hint when another app plays its own notification-usage audio.

## Acceptance criteria

- ✅ Welcome links to **Silence competing sounds** (wizard lives on that pane, not in Permissions)
- ✅ Home → Hear and Announcer → Silence open the same pane
- ✅ ColorOS-family devices (OnePlus / Oppo / Realme) show an extra hint that **Silent** often still dings
- ✅ Dashboard opens a **Silence competing sounds** pane with the wizard plus a leak list
- ✅ Listener records a leak (package + channel only) when a posted notification has a real channel sound, notification sound, or default-sound flag
- ✅ Setting a channel to None, Silent, OpenShouter Silent, or low importance removes that leak on the next post or when the Silence pane resumes
- ✅ Channels that only use the system default are ignored when that default is None or OpenShouter Silent; ColorOS empty Silent is not trusted; LineageOS/AOSP on OnePlus hardware still trusts None
- ✅ Leak row opens that channel in system settings (same intent as history mute)
- ✅ Optional `WRITE_SETTINGS` sets the MediaStore silent file as the default notification or ringtone
- ✅ When the announcer is running, notification-usage playback from another UID (API 31+) is stored as `OWN_AUDIO`
- ✅ Empty / null / OpenShouter Silent URIs are not leaks; our package and group summaries are skipped
- ✅ TalkBack: rows are buttons with the visible labels
- ✅ i18n: `silence_*` / `nav_silence` in `strings_silence.xml` (en/es/fr)

## Smoke scenario

1. Fresh install → Welcome → install OpenShouter Silent → open sound settings → pick **OpenShouter Silent** or **None**
2. Grant notification access; receive a Messages notification that still has a channel sound
3. Dashboard → Silence competing sounds lists Messages and that channel
4. Tap the row → system channel settings → set sound to None or OpenShouter Silent
5. Optional: allow modify system settings → Use as default notification sound

## Container map

| Layer | Path |
|-------|------|
| Logic | `silence/SilentWav.kt`, `SoundLeakPolicy.kt`, `OemSilenceHints.kt`, `AudioSessionHint.kt`, `SilentPack.kt`, `SilentDefaults.kt`, `SoundSettingsIntents.kt`, `AudioSessionMonitor.kt` |
| Data | `data/SoundLeakStore.kt`, Room v7 `sound_leaks` |
| View | `ui/silence/SilenceScreen.kt`, `SilenceWizard.kt`, Welcome + Dashboard wiring |
| Tests | `SoundLeakPolicyTest`, `SilentWavTest`, `OemSilenceHintsTest`, `AudioSessionHintTest`, `SoundSettingsIntentsTest` |
| Wiring | `OpenShouterEntryPoint` + `DatabaseModule` + listener/service start ≤10 lines each |

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty / broken silent URI | Valid PCM WAV bytes in `SilentWav`; `SoundLeakPolicy` treats blank, `null`, and our display name as silent. Tests in `SilentWavTest` / `SoundLeakPolicyTest` |
| Network timeout | N/A — no network I/O |
| Race (MediaStore insert vs picker) | `SilentPack.existingUri` before insert; wizard refreshes on resume |
| Unhandled Settings / MediaStore | `runCatching` on insert, write-settings, and intents; fall back to `ACTION_SOUND_SETTINGS` |
| Cannot mute another app’s `MediaPlayer` | `OWN_AUDIO` is a hint only; copy says the app may play its own sound |
| PII in leak table / logcat | Store package + channel id/name + evidence only. Never title, text, numbers, or coordinates |
| `WRITE_SETTINGS` surprise | Explicit grant button; never set defaults until the user taps Use as default |
| API 26–28 MediaStore | `WRITE_EXTERNAL_STORAGE` maxSdk 28; request on install tap |
| API 26–30 audio UID | `OWN_AUDIO` only on API 31+ (`getClientUid`). Test documents the skip |
| `strings.xml` 300-line cap | Keys live in `strings_silence.xml` |
| LineageOS None on OnePlus hardware | `OemSilenceHints` trusts empty None when `ro.lineage.*` / AOSP flavor is set; Silence resume deletes leftover `DEFAULT_SOUND` rows. `OemSilenceHintsTest` |

## Notes

- OpenShouter cannot change another app’s channel sound; the leak list only deep-links.
- After each AGENT step: `python3 scripts/agent-run.py watch-agent-gates --once --autofix`

## Tests

- Automated: yes — Android unit tests under `examples/android/app/src/test/`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
