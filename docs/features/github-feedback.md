# Feature: github-feedback

> Compose GitHub issue-form URLs, clipboard fallback, fail-soft placeholder repo.

## Acceptance criteria

- ✅ User-visible behavior: small fields prefill `issues/new?template=...`; large bodies use clipboard + short URL
- ✅ Offline/error behavior: `OWNER/REPO` never hits the network
- ✅ Accessibility: N/A (logic); Open GitHub is `https` only
- ✅ i18n: N/A in this container (copy lives in `feedback_*`)

## Smoke scenario

1. Given `release_repo` `acme/app` and fingerprint `a1b2c3d4e5f6`
2. When the composer builds a crash title
3. Then the title is `[crash] a1b2c3d4e5f6 TypeError`

## Container map

| Layer | Path |
|-------|------|
| Logic | `org/openshouter/githubfeedback/` |
| Tests | `src/test/java/org/openshouter/githubfeedback/` |
| Wiring | none (Feedback UI calls this) |

## Tests

- Automated: yes — `IssueFormUrlTest.kt`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
