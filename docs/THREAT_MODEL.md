# Threat Model

> OpenShouter (Android FOSS announcer). Methodology: STRIDE + OWASP MASVS.

## Scope

| Item | Value |
|------|-------|
| Project | OpenShouter |
| Stack | Android / Kotlin / Compose (minSdk 26, targetSdk 35) |
| Distribution | GitHub Releases only (sideload APK) |
## Trust Boundaries

```text
[User] --> [OpenShouter UI]
              |
              +--> [NotificationListenerService] --> [Notification shade / other apps]
              +--> [TelephonyCallback] --> [Telecom / ContactsContract]
              +--> [LocationManager] --> [OS location providers]
              +--> [TTS engine] --> [Audio HAL / Bluetooth A2DP]
              +--> [Room / DataStore] --> [App-private storage]
              +--> [GitHub Releases API] --> [Optional update metadata only]

```

Other apps' notification payloads and the contact database are **untrusted input**. TTS output is user-audible and must not leak into logs.

## STRIDE Summary

| Threat | Example | Mitigation | Owner |
|--------|---------|------------|-------|
| Spoofing | Fake update APK | Pin `release_repo`; match `installed_artifact_format`; sha256 when published | AGENT |
| Tampering | Modified format string / regex ReDoS | Bound regex cost; validate format tokens against allow-list | AGENT |
| Repudiation | User denies a spoken notification | Local history optional; no cloud audit | AGENT |
| Information disclosure | Notification text in logcat | Never log title/body/phone; redact in crash paths | AGENT |
| Denial of service | TTS loop + wake lock abuse | Call loop tied to RINGING only; gesture interrupt; quiet hours | AGENT |
| Elevation of privilege | Using Notification Listener beyond speak/log | No accessibility-node scraping; no export without user action | AGENT |
## Top Abuse Cases

1. **Notification content leak** via verbose logging or screenshots in bug reports — mitigate: default log level excludes payload; HISTORY is local and user-clearable
2. **Regex ReDoS** on incoming notification text — mitigate: timeout/size limits in domain layer tests
3. **Background location stalking** if a malicious fork adds upload — mitigate: FOSS license + no network except optional GitHub update check; review PRs that add INTERNET use
4. **Persistent TTS / wake lock** draining battery — mitigate: foreground service with user-visible notification; master mute tile
5. **Supply-chain compromise** of Gradle plugins — mitigate: Dependabot, Trivy, CodeQL, lockfiles, FOSS grep for GMS

## Security Tasks

- Sprint 1: FOSS Gradle grep in CI (already in template android jobs)
- Sprint 2: regex budget tests; no PII in `Log.*`
- Sprint 6: background location rationale + privacy copy
- Ongoing: `docs/SECURITY_TRIAGE.md` weekly

## Review Cadence

- `[HUMAN]` Review at each milestone boundary
- `[AGENT]` Update when architecture or data flows change (append ADR reference)
