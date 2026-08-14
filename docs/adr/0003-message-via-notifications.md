# ADR-0003: Message shout via notification listener (no READ_SMS)

- **Status:** Proposed (awaiting `[HUMAN]` approval)
- **Date:** 2026-08-13
- **Deciders:** OpenShouter maintainers

## Context

Classic Shouter has a **Message** shout (read sender, optional body, unknown-number toggle, known-contacts-only body). Store copy notes Google policy blocked direct SMS/call APIs; Shouter already reads call/SMS from **notifications**. OpenShouter `AGENT_MEMORY.md` previously listed SMS/MMS reading as a non-goal. A dedicated Message channel is still wanted for parity without a new SMS permission.

## Decision

1. Implement Message shout as a **notification-listener channel** for messaging packages (user-selected, default SMS/MMS/RCS packages when present).
2. Do **not** add `READ_SMS`, `RECEIVE_SMS`, or `READ_CELL_BROADCASTS`.
3. Parse sender/body from notification extras only; apply Contacts lookup when `READ_CONTACTS` is granted.
4. Never log sender, number, or body.

## Consequences

- RCS/OEM messengers work only if they post notifications OpenShouter can see.
- Users who disable notification access lose Message shout (same as App Notification).
- Play-restricted SMS permissions stay out of the APK.

## Alternatives considered

| Option | Rejected because |
|--------|------------------|
| `READ_SMS` + Telephony SMS inbox | Policy/privacy; contradicts FOSS GitHub-Releases stance and prior non-goal |
| Skip Message entirely | Drops a primary Shouter screen the user asked to match |
| Treat all notifications as messages | Collides with App Notification; no sender/known-only rules |
