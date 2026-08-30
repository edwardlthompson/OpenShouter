# Human Backlog

> Items automation attempted during autonomous `/build` but could not complete. BUILD_PLAN rows stay open until a human finishes them.

| Deferred | Sprint | Owner | Task | Reason |
|----------|--------|-------|------|--------|
| 2026-08-29 | Sprint 21 leftover | ADB | WhatsApp Once then silence after answer; Phone still loops until answer | v1.1.1 on both phones; WhatsApp Incoming calls shows Once; foss release cannot inject RING — needs a live WhatsApp call and a cellular ring |
| 2026-08-29 | Sprint 23 leftover | ADB | Drop several photos into Messages; first shout may say Messages, later ones do not repeat it within 30s | Cooldown is on Announcer (default 30s); Google Messages is selected. Drop photos on-device |
| 2026-08-29 | Sprint 24 leftover | ADB | LineageOS None clears Default sound leak rows; install OpenShouter Silent and confirm a custom-channel ding is listed | v1.2.0 sideload; CPH2583 notification_sound is already None |
| 2026-08-29 | Sprint 24 leftover | HUMAN | Confirm ColorOS Silent-still-dings workaround | Both phones now run LineageOS; ColorOS Silent may not apply |
| 2026-08-30 | Sprint 21 leftover ADB | ADB | WhatsApp Once then silence after answer; Phone still loops until answer (CPH2583 / CPH2655, v1.1.1 sideloaded; Incoming calls = Once in Apps to shout; release APK has no debug RING — needs a live WhatsApp + cellular ring) | no_authorized_device after unit tests |
| 2026-08-30 | Sprint 23 leftover ADB | ADB | Drop several photos into Messages; first shout may say Messages, later ones do not repeat it within 30s (v1.1.1 sideloaded; cooldown is on Announcer; Google Messages app-name + notification selected on both phones) | No automation rule for ADB task in sprint Sprint 23 leftover ADB |
| 2026-08-30 | Sprint 24 — Silence competing sounds | ADB | On CPH2583 / CPH2655 (sideloaded 1.2.0 versionCode 29): LineageOS None clears Default sound leak rows; install OpenShouter Silent, confirm a custom-channel ding is listed and the channel page opens | no_authorized_device after unit tests |
| 2026-08-30 | Sprint 24 — Silence competing sounds | HUMAN | Confirm ColorOS Silent-still-dings workaround on both phones | No automation rule for HUMAN task in sprint Sprint 24 — Silence competing sounds |
| 2026-08-30 | Sprint 25 — Golden Path feedback pack (1–5) | ADB | Toggle on, force a test crash, confirm one sanitized review dialog and no auto-GitHub | No automation rule for ADB task in sprint Sprint 25 — Golden Path feedback pack (1–5) |
| 2026-08-30 | Sprint 25 — Golden Path feedback pack (1–5) | HUMAN | Confirm About Copy / Open GitHub / Discard on a real device | No automation rule for HUMAN task in sprint Sprint 25 — Golden Path feedback pack (1–5) |
| 2026-08-30 | Sprint 26 — Sun and moon alarms (idea 26 expanded) | ADB | “7:00 Mon–Fri” plus Sunset −15m Sat–Sun: lockscreen Snooze/Stop; widget “now” at the top on CPH2583 / CPH2655 | No authorized device attached for lockscreen testing |
| 2026-08-30 | Sprint 26 — Sun and moon alarms (idea 26 expanded) | HUMAN | Confirm one-time location vs typed city both produce the same city-level times | Requires live human verification on real hardware |
