# Privacy Policy (Draft)

OpenShouter is local-first. Notification text, contacts, call numbers, and location stay on the device unless the user explicitly exports them.

## Data We Collect

| Data | Purpose | Lawful Basis | Retention |
|------|---------|--------------|-----------|
| App settings (DataStore) | Feature functionality | Legitimate interest | Until user clears app data |
| Notification history (Room) | On-device log / debugging TTS | Legitimate interest | User-configurable; deletable in-app |
| Contacts (read-only lookup) | Speak contact name for caller ID | User consent (permission) | Not stored; resolved at ring time |
| Phone numbers (incoming) | Caller ID announcement | User consent (`READ_PHONE_STATE`) | Not logged beyond optional local history if enabled |
| Location (fine / background) | Geofence Home/Work mode toggles | User consent | Coordinates for saved places only; no telemetry |
| GitHub Releases check | Optional in-app update notice | Legitimate interest | `last_checked`, `installed_artifact_format` locally |

## App update checks

- Release endpoint: GitHub Releases API or configured manifest URL
- Stored locally: `last_checked`, `installed_artifact_format`, `check_interval`
- No PII transmitted

## Data We Do Not Collect

- No accounts, analytics, or advertising IDs
- No upload of notification contents, call logs, or GPS traces
- No Firebase / Google Analytics / crash reporters
- No sale of personal data

## User Rights (GDPR / CCPA)

- **Access:** Notification history and saved places are viewable in-app
- **Deletion:** Clear history / uninstall / clear storage
- **Opt-out:** Master mute, quiet hours, per-app blacklist; update checks default weekly and can be set to `off`
- **Portability:** Settings export is a later feature; until then, Room/DataStore are on-device only

## Data Minimization

- Notification Listener is used only to speak and optionally log locally
- Contacts are queried, not copied into our database
- Location updates run only when at least one geofence is enabled

## DPIA Checklist (`[HUMAN]`)

If processing EU personal data:

- 🔲 Document processing purpose and legal basis
- 🔲 Assess necessity and proportionality
- 🔲 Identify risks and mitigations
- 🔲 Record in `DECISION_LOG.md` or ADR

## Contact

Privacy inquiries: maintainers in `.github/CODEOWNERS` or `SECURITY.md`.
