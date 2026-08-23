# Feature: app speak list

Searchable installed-app list. Per app: shout the **app name**, read the **notification**, both, or neither (silent).

## Acceptance criteria

- ✅ User can search by app label or package name
- ✅ **Selected apps only** hides inactive apps so the active set is visible at a glance
- ✅ **Select all** / **Deselect all** applies both boxes to the visible list
- ✅ Two independent checkboxes: App name, Notification
- ✅ Unchecked apps are not spoken (opt-in)
- ✅ TalkBack: each checkbox has a content description that includes the app label
- ✅ i18n: `apps_*` keys in `strings.xml`
- ✅ TTS format (`%app` `%title` `%text`) lives on Voice, not this list, so the picker stays a search + list

## Smoke scenario

1. Given Notification access is on and announcements are enabled
2. When the user checks **App name** only for Messages
3. Then a Messages notification is spoken as the app label, not the body
4. When **Notification** is also checked, the TTS format string is used (including `%app` if that box is on)

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/openshouter/domain/AppSpeakPolicy.kt`, `AppSpeakList.kt` |
| Persistence | `examples/android/app/src/main/java/org/openshouter/data/AppSpeak.kt` |
| Catalog | `examples/android/app/src/main/java/org/openshouter/apps/InstalledAppCatalog.kt` |
| View | `examples/android/app/src/main/java/org/openshouter/ui/apps/AppSpeakScreen.kt` |
| Tests | `AppSpeakPolicyTest.kt`, `AppSpeakListTest.kt` |
| Wiring | `OpenShouterHome.kt` Pane.Rules |
## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty package | `AppSpeakStore.set` ignores blank package names |
| Network timeout | N/A |
| Race list vs toggle | Room REPLACE upsert; Flow refreshes checkboxes |
| Select-all empty list | Button hidden when the visible list is empty |
| Select-all scope | Applies to the current search + selected-only view, not every installed app unless that view is unfiltered |
| Unhandled PM errors | Catalog uses `runCatching` for labels |
| QUERY_ALL_PACKAGES | Manifest + GitHub Releases; `[HUMAN]` Play-policy N/A |
## Notes

- Chrome is title + search icon + selected-only switch. The search field opens only after the icon tap; Back or close hides it and clears the query. TTS format is on Voice (`TtsSettingsScreen`).
- Apps with both boxes off have no Room row.
- Existing DataStore whitelist packages are imported once (both boxes on).
- After each AGENT step: `python3 scripts/agent-run.py watch-agent-gates --once --autofix`
