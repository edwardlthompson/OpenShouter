# Human Backlog

> Items automation attempted during autonomous `/build` but could not complete. BUILD_PLAN rows stay open until a human finishes them.

| Deferred | Sprint | Owner | Task | Reason |
|----------|--------|-------|------|--------|
| 2026-08-29 | Sprint 21 leftover | ADB | WhatsApp Once then silence after answer; Phone still loops until answer | v1.1.1 on both phones; WhatsApp Incoming calls shows Once; foss release cannot inject RING — needs a live WhatsApp call and a cellular ring |
| 2026-08-29 | Sprint 23 leftover | ADB | Drop several photos into Messages; first shout may say Messages, later ones do not repeat it within 30s | Cooldown is on Announcer (default 30s); Google Messages is selected. Drop photos on-device |
| 2026-08-30 | Sprint 21 leftover ADB | ADB | WhatsApp Once then silence after answer; Phone still loops until answer (CPH2583 / CPH2655, v1.1.1 sideloaded; Incoming calls = Once in Apps to shout; release APK has no debug RING — needs a live WhatsApp + cellular ring) | requires live incoming VoIP and cellular call |
| 2026-08-30 | Sprint 23 leftover ADB | ADB | Drop several photos into Messages; first shout may say Messages, later ones do not repeat it within 30s (v1.1.1 sideloaded; cooldown is on Announcer; Google Messages app-name + notification selected on both phones) | requires incoming photo drops in Messages |
