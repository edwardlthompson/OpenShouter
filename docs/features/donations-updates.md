# Feature: donations and product updates

Reuse Continuum Calendar’s quiet Venmo donate path and installer-filename GitHub check.

## Acceptance criteria

- ✅ About and the home Menu always show **Donate via Venmo** (public URL, not a secret)
- ✅ Donate is never on the update/install dialog
- ✅ No launch nag and no daily donate timer
- ✅ First run records the installed version with no donate popup
- ✅ After a later version change, one optional note: title **Development is still going**; either button records “seen this version”
- ✅ Once per 24 hours, fetch `https://api.github.com/repos/edwardlthompson/OpenShouter/releases/latest` (`User-Agent`, 10s timeout)
- ✅ Compare **product installer** versions from `openshouter-X.Y.Z-foss.apk`, not the git/template tag
- ✅ Newer matching asset and not dismissed → **Install** | **Later**; Later silences that version
- ✅ Failed fetch, timeout, empty assets, or same version: stay silent
- ✅ Donate prefs and last-check timestamps stay device-local

## Smoke scenario

1. Cold install: app opens with no donate or update dialog
2. Bump `versionName` and relaunch: one donate note; **Not now** or **Donate via Venmo** both stop it until the next bump
3. `/ship` publishes a signed `openshouter-X.Y.Z-foss.apk` on GitHub: **Install** opens the asset URL (else the release page); **Later** does not ask again for that version

## Container map

| Layer | Path |
|-------|------|
| Logic | `org/openshouter/updates/ProductUpdate.kt` |
| Prefs | `org/openshouter/updates/UpdatePrefs.kt` |
| Network | `org/openshouter/updates/GithubRelease.kt` |
| View | `org/openshouter/ui/updates/` plus About + Dashboard |
| Tests | `org/openshouter/updates/ProductUpdateTest.kt` |
| Wiring | `GoldenPathApp` `ProductUpdateHost()` |

## Fallback validation

Unit tests cover interval, filename parse, newer-than, dismiss, and donate-nudge. Live GitHub/Venmo taps are `[HUMAN]`.

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | Blank version / empty assets → no prompt; tests in `ProductUpdateTest` |
| Network timeout | 10s; exceptions → null |
| Race | Donate nudge short-circuits the daily check on that launch |
| Unhandled exceptions | Fetch/parse catch; dialogs only after a successful decide |

## Notes

- Venmo: `https://venmo.com/code?user_id=1857304970395648420`
- Do not peer-sync `openshouter_updates` prefs through SAF backup

## Tests

- Automated: yes — Android unit tests under `examples/android/app/src/test/`

- Command: `python3 scripts/agent-run.py feature-gate --stack android`
