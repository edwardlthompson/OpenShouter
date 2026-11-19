# News Inbox

Auto-collected inbox of news, upstream updates, security fixes, and ideas relevant to this repo, appended daily by News Bot.
A Cursor planning agent should review these items and fold the useful ones into BUILD_PLAN; entries are append-only, newest at the bottom.


## 2026-11-13

**News and updates**

- Android 17 QPR2 stable: lock-screen widget editing, status-bar notification filters, share-sheet changes, brightness override. [source](https://www.androidauthority.com/android-17-qpr2-stable-3733645/)
- Kotlin 2.5.0-RC released (fixes a reified Enum<T> inference regression); Gradle 9.9.1 / 8.14.7 patch releases. [source](https://github.com/JetBrains/kotlin/releases/tag/v2.5.0-RC)

**Tasks for the planning agent**

- [x] Check alert capture with Android 17 QPR2 notification filters — New status-bar filters could affect which notifications are spoken. Label: `[ADB]` · Priority: P2 · https://www.androidauthority.com/android-17-qpr2-stable-3733645/ (synced to BUILD_PLAN)
- [ ] Smoke-test Kotlin 2.5.0-RC and bump Gradle wrapper — Catches compiler regressions before stable. Label: `[AGENT][LOCAL]` · Priority: P3 · https://github.com/JetBrains/kotlin/releases/tag/v2.5.0-RC

## 2026-11-19

**News and updates**

- Jetpack Compose November '26 release is stable: Compose BOM 2026.11.01 (Compose 1.13) with Material3 1.5.0. The BOM needs Kotlin 2.2+ and compileSdk 37.1. Material3 1.5.0 breaks Slider/RangeSlider source (stateless overloads deprecated, `onValueChange` now required, RangeSliderState activeRangeStart/End renamed to startValue/endValue); SecureTextField now defaults to system obfuscation, and clickables play platform click sounds by default. [source](https://developer.android.com/blog/posts/jetpack-compose-november-2026-release) · [Material3 notes](https://developer.android.com/jetpack/androidx/releases/compose-material3)
- Material3 1.5.0 adds a `material3-ripple` artifact that draws keyboard/D-pad focus as inset focus rings, and Compose 1.13 clickables now play the system click sound by default (respecting system settings). [source](https://developer.android.com/jetpack/androidx/releases/compose-material3)

**Tasks for the planning agent**

- [x] Bump Compose BOM 2026.06.01 to 2026.11.01 (Compose 1.13 / Material3 1.5.0) and fix Slider/RangeSlider call sites — Material3 1.5.0 makes `onValueChange` required on Slider/RangeSlider (1 file here call Slider/RangeSlider) and the BOM requires Kotlin 2.2+ and compileSdk 37.1, so a Dependabot/Argus BOM bump will fail unless AGP accepts 37.1 and the call sites are fixed first. Label: `[AGENT][LOCAL]` · Priority: P2 · https://developer.android.com/blog/posts/jetpack-compose-november-2026-release (synced to BUILD_PLAN)
- [ ] After the BOM 2026.11.01 bump, check keyboard/D-pad focus rings on the main flows and remove manual click sounds that would now double-fire — Inset focus rings make focus visible for keyboard and switch-access users, and stray double UI sounds are noticeable in an accessibility-focused app. Label: `[AGENT][LOCAL]` · Priority: P3 · https://developer.android.com/jetpack/androidx/releases/compose-material3
