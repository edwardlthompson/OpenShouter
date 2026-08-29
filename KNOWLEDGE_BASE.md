# Knowledge Base

> Repository of stack-specific edge cases, resolved complex bugs, anti-patterns, and reusable project solutions.
> **Do not populate with generic framework definitions.**

## How to use

1. Add entries only after resolving a non-obvious issue specific to this project.
2. Include: symptom, root cause, fix, and prevention.
3. Link to relevant ADRs or PRs when available.

## Entries

### KB-001 — UTF-16 file corruption on Windows

| Field | Detail |
|-------|--------|
| **Symptom** | `check-json` / `npm` / `json.load` fails; git ignore rules stop working; `.gitignore` shows as untracked patterns not applied |
| **Cause** | Cursor `StrReplace` or Windows editor saves text as UTF-16 LE (NUL bytes between ASCII chars) |
| **Fix** | Rewrite affected files with Python `Path.write_text(..., encoding='utf-8')`; re-run `scripts/check-file-encoding.sh` |
| **Prevention** | Bulk edits on Windows via Python/PowerShell UTF-8 write; include root `.gitignore` in encoding scan |
### KB-002 — Invalid `trivy-action@0.28.0` ref

| Field | Detail |
|-------|--------|
| **Symptom** | Security Scan workflow fails at setup: action version not found |
| **Cause** | Bare semver `@0.28.0` is not a valid GitHub Action ref tag |
| **Fix** | Pin to full SHA: `aquasecurity/trivy-action@a9c7b0f06e461e9d4b4d1711f154ee024b8d7ab8 # v0.36.0` |
| **Prevention** | Run `validate-workflow-actions.sh` pre-push; use `check-workflow-action-ref-format.sh` locally |
### KB-003 — `gh api --silent` false CI failures

| Field | Detail |
|-------|--------|
| **Symptom** | `validate-workflow-actions.sh` fails in CI with unknown `gh` flag error |
| **Cause** | `gh api` has no `--silent` flag; stderr not suppressed correctly |
| **Fix** | Redirect to `/dev/null` instead: `gh api ... >/dev/null 2>&1` |
| **Prevention** | Test validation scripts in CI job with `GH_TOKEN`; avoid undocumented `gh` flags |
### KB-004 — Lighthouse performance flake on shared runners

| Field | Detail |
|-------|--------|
| **Symptom** | CI fails with performance 0.88 vs required 0.90 on a single Lighthouse run |
| **Cause** | GitHub-hosted runner CPU variance; single-run assertion is noisy |
| **Fix** | Set `numberOfRuns: 3` in `.lighthouserc.json`; LHCI uses median; keep `minScore: 0.9` |
| **Prevention** | Do not lower performance budget for CI flake; use multi-run median in `modules/web/MODULE.md` |
### KB-005 — Playwright webServer duplicate build

| Field | Detail |
|-------|--------|
| **Symptom** | E2E hangs or serves stale assets; double `vite build` in CI |
| **Cause** | `webServer` runs build while CI already built; wrong host binding |
| **Fix** | Use `vite preview` on `127.0.0.1`; CI runs `npm run build` once before Playwright |
| **Prevention** | Golden Path `examples/web/playwright.config.ts` documents preview-only webServer |
### KB-006 — TypeScript strict null in render handlers

| Field | Detail |
|-------|--------|
| **Symptom** | `tsc` / ESLint error: Object is possibly null inside `render()` callback |
| **Cause** | `strictNullChecks` + `document.getElementById` return type includes null |
| **Fix** | Assign narrowed ref at module scope: `const root = document.getElementById('root')!` or guard once |
| **Prevention** | Module-level `const root = app` pattern in `examples/web/src/main.ts` |
### KB-007 — npm/pip overrides policy for transitive CVEs

| Field | Detail |
|-------|--------|
| **Symptom** | Dependabot or `npm audit` / `uv pip audit` reports CVE in a transitive dependency with no direct upgrade path |
| **Cause** | Parent package pins or bundles a vulnerable sub-dependency; fix not yet published upstream |
| **Fix** | **npm:** add `overrides` in `package.json` to force patched semver (see `examples/web` `@lhci/cli` overrides). **Python:** prefer `uv`/`pip` constraint or bump direct dep; document in DECISION_LOG if override is temporary |
| **Prevention** | Prefer overrides over `--force` installs; remove overrides when upstream ships fix; weekly triage per `docs/SECURITY_TRIAGE.md`; see KB-007 before dismissing Dependabot alerts |
### KB-009 — Release Please `pr` output is JSON, not a PR number

| Field | Detail |
|-------|--------|
| **Symptom** | `release-please.yml` sync step fails: `Error reading JToken from JsonReader` or empty `gh pr checkout` |
| **Cause** | `steps.release.outputs.pr` is empty when `release_created == 'true'` (post-merge push) or stale PR metadata |
| **Fix** | Skip sync when `release_created`; resolve PR number in shell from `PR_JSON` or `gh pr list --head release-please--branches--main` |
| **Prevention** | Never use bare `fromJSON(steps.release.outputs.pr)` in workflow `env:` without a non-empty guard |
### KB-008 — `android-release` APK hash compare policy

| Field | Detail |
|-------|--------|
| **Symptom** | `Android - assembleRelease` fails: APK hashes differ between two clean `assembleRelease` runs on CI |
| **Cause** | Usually a reproducibility regression (non-hermetic timestamp, path, or dependency drift). Rare runner flakes are possible but treated as failures to catch real regressions early |
| **Fix** | Rebuild locally with `SOURCE_DATE_EPOCH=1700000000 ./gradlew clean assembleRelease` twice; compare `sha256sum` of release APK. Align `build.gradle.kts`, `gradle.properties`, and dependency lockfiles with `modules/android/MODULE.md` |
| **Prevention** | Keep `SOURCE_DATE_EPOCH` pinned in CI; use `scripts/verify-reproducible-apk.sh --strict` before release tags. Do not downgrade the job to WARN — strict compare is intentional (M17 P2) |
### KB-010 — Agent shell opens `.sh` files and steals editor focus

| Field | Detail |
|-------|--------|
| **Symptom** | While typing, a `.sh` tab opens and keystrokes land in the wrong file during Cursor Agent work |
| **Cause** | Agent runs `bash scripts/*.sh`; Cursor reveals script paths. `beforeShellExecution` hooks used to run `.sh` wrappers on every shell command |
| **Fix** | Use `python3 scripts/agent-run.py <name> [args]` in agent commands; hooks migrated to `.cursor/hooks/*.py`; workspace `.vscode/settings.json` sets `workbench.editor.autoReveal: false` |
| **Prevention** | Agents follow `.cursor/commands/` and `scripts/agent-run.py`; pin active editor tab; optional `<!-- cursor-hooks: off -->` in `BUILD_PLAN.md` disables hooks entirely |
### KB-011 — Vitest jsdom `localStorage` broken on Node 25+

| Field | Detail |
|-------|--------|
| **Symptom** | `npm test` in `examples/web`: `TypeError: Cannot read properties of undefined (reading 'clear')` or `localStorage.getItem is not a function` |
| **Cause** | Node 25+ enables a global Web Storage stub without `--localstorage-file`; jsdom skips installing real Storage and the stub shadows it |
| **Fix** | Vitest `setupFiles: ["src/test/setup-localStorage.ts"]` installs in-memory Storage when `getItem` is missing |
| **Prevention** | Keep the setup file; do not rely on Node’s experimental `localStorage` in browser-unit tests |
### KB-012 — Gitleaks first-push parent SHA

| Field | Detail |
|-------|--------|
| **Symptom** | Security Scan fails on the first `main` push: `ambiguous argument '<root>^..<head>'` |
| **Cause** | Gitleaks diffs `before^..after`. The bootstrap commit is a root commit, so `before^` does not exist |
| **Fix** | Re-run Security Scan via `workflow_dispatch` (full-repo scan). Later pushes use a real parent |
| **Prevention** | Do not treat the first-push Gitleaks failure as a leak; confirm “no leaks found in partial scan” |
### KB-013 — Pruned-stack CI jobs still run

| Field | Detail |
|-------|--------|
| **Symptom** | Child Android-only repo: `Web - Lint, Test, Build` and `Node - Lint, Test` fail; CodeQL `javascript-typescript` exits 32 |
| **Cause** | Those CI/CodeQL jobs are not gated on `examples/web` / `examples/node` presence |
| **Fix** | Gate Web/Node CI jobs on `needs.stack-presence.outputs.*`. For CodeQL, emit a dynamic `matrix.include` from stack presence — do not use a job-level `if:` on `matrix.language` (Actions treats that as a workflow-file error and the run fails in 0s). |
| **Prevention** | Child-repo `/prune` should leave CI job `if:` guards in place; CodeQL languages must be built as JSON matrix output |
### KB-014 — Emulator `CallMonitor` needs READ_PHONE_STATE

| Field | Detail |
|-------|--------|
| **Symptom** | `connectedDebugAndroidTest` crashes: `SecurityException: listen` in `CallMonitor.start` |
| **Cause** | `AnnouncerService.onCreate` registers a telephony callback before the emulator grants `READ_PHONE_STATE` |
| **Fix** | Grant the permission in the instrumented test (or skip `CallMonitor.start` when the permission is missing) |
| **Prevention** | Never register `TelephonyCallback` without a permission check |
### KB-015 — Android 16 mutes engine-process TTS

| Field | Detail |
|-------|--------|
| **Symptom** | Voice test and shouts are silent when the phone is in silent/vibrate; `STREAM_NOTIFICATION` volume is 0 |
| **Cause** | Default TTS stream was NOTIFICATION. Android 16 AudioHardening also mutes `com.google.android.tts` background playback even on MEDIA |
| **Fix** | Synthesize to `cache/os-tts.wav` and play with `TtsFilePlayer` (MediaPlayer in our process). Default stream MEDIA. If the preferred stream is muted, fall back to MEDIA unless ringer is silent/vibrate and `allowSilentVibrate` is false |
| **Prevention** | Voice test uses `immediate = true` and MEDIA. `TtsController.speakNow` must not bypass `AnnouncementGate` / per-channel silent |
### KB-016 — Release Please PR checks stay `action_required`

| Field | Detail |
|-------|--------|
| **Symptom** | `chore(main): release X.Y.Z` PR is MERGEABLE but BLOCKED; CI/CodeQL/Security Scan show `action_required` with zero jobs |
| **Cause** | `pull_request` workflows on `release-please--branches--main` never start jobs (approval / first-run gate) |
| **Fix** | `merge-release-please-pr.sh` admin fallback: `gh pr merge --admin` after `--auto` fails |
| **Prevention** | Job-level rollups named `CI`, `Security Scan`, and `CodeQL` report the required check contexts. Do not wait for empty-job PR checks. Push-SHA CI/CodeQL/Security Scan on `main` are the real gate |
| **Seen again** | v0.5.0 Release Please #17 — `--auto` failed; admin merge after push-SHA CI/CodeQL/Security Scan green |
| **Seen again** | v0.6.0 Release Please #19 — `--auto` failed; admin merge after push-SHA CI/CodeQL/Security Scan green |
| **Seen again** | v0.7.0 Release Please #20 — `--auto` failed; admin merge after push-SHA CI/CodeQL/Security Scan green |
| **Seen again** | v0.8.0 Release Please #22 — `--auto` failed; admin merge after push-SHA CI/CodeQL/Security Scan green |
| **Seen again** | v0.9.1 Release Please #27 — `--auto` failed; admin merge. Merge-commit CI/Release Please `startup_failure`/`queued`; GitHub Release created from merge SHA |
### KB-021 — OP13 release APK silent under AudioHardening

| Field | Detail |
|-------|--------|
| **Symptom** | Notification-stream shouts work on OP12 debug APK (`CPH2583`) but are silent on OP13 release APK (`CPH2655`). Listener, FGS, ringer, and notification volume are fine |
| **Cause** | Android 16 AudioHardening treats `specialUse` FGS playback as background. It ignores transient focus and mutes `org.openshouter` on the non-debuggable release UID. Debug APKs are exempt |
| **Fix** | Declare `FOREGROUND_SERVICE_MEDIA_PLAYBACK` and `specialUse\|mediaPlayback`. Hold a `MediaSession` while the in-process WAV plays. Prefer highest-quality on-device TTS voices |
| **Prevention** | After a release sideload, confirm `dumpsys audio` no longer logs `background playback would be muted for org.openshouter` |
### KB-017 — Next shout silent after a muted stream

| Field | Detail |
|-------|--------|
| **Symptom** | First silent/vibrate shout is quiet (correct). The next shout stays silent even when MEDIA should play |
| **Cause** | A shared `os-tts.wav` plus ignored synth errors left playback stuck. v0.2.3 also fell back to ALARM, which punches through silent/DND |
| **Fix** | Synthesize each utterance to `os-tts-<id>.wav`. Do not auto-escalate to ALARM. Silent/DND is opt-in (`ds_speak_silent`) |
| **Prevention** | `TtsStreamPlayableTest` stays on the muted stream when MEDIA is muted. `RingerSilent` treats DND as silent |
### KB-018 — Product semver is not the bootstrap template version

| Field | Detail |
|-------|--------|
| **Symptom** | Copying upstream `.template-version` `0.21.0` into OpenShouter fails `check-template-version-sync` or would retag the product |
| **Cause** | Child `.template-version` / Release Please / `CITATION.cff` track **product** semver (0.3.x), not agent-project-bootstrap |
| **Fix** | Keep `.template-version` on the last shipped product. Record template provenance in `AGENT_MEMORY.md` and `AGENTS.md` |
| **Prevention** | Never run `sync-template-version.sh` to adopt an upstream template number. `pre-release-gate` CI wait needs a **pushed** SHA
### KB-023 — `chore` and `docs` must not mint a Release Please patch

| Field | Detail |
|-------|--------|
| **Symptom** | After `chore(android): sync versionName` or `docs: record vX.Y.Z ship`, Release Please opens `X.Y.Z+1` with only that commit |
| **Cause** | `changelog-sections` listed `chore` and `docs`, so those commits are releasable |
| **Fix** | Drop `chore` and `docs` from `changelog-sections`. Put `versionName` in extra-files with `// x-release-please-version` |
| **Prevention** | `tests/test_release_please_config.py`. Do not merge a release PR whose only change is a versionName sync or ship-notes `docs` commit |
### KB-019 — `feat` is a minor bump; keep CITATION.cff on the tag

| Field | Detail |
|-------|--------|
| **Symptom** | `/ship` commit said v0.2.6; Release Please tagged **v0.3.0**. Merge CI failed `CITATION.cff version (0.2.5) != .template-version (0.3.0)` |
| **Cause** | `feat` maps to Added / minor. Release Please extra-files did not include `CITATION.cff` |
| **Fix** | Accept 0.3.0. Add `CITATION.cff` to extra-files with `# x-release-please-version`. `sync-template-version.sh` rewrites the CFF version line |
| **Prevention** | Do not put a guessed patch version in a `feat` subject. After each RP merge, confirm CITATION matches `.template-version` |
### KB-022 — Settings theme dropdown consumes the first Back

| Field | Detail |
|-------|--------|
| **Symptom** | `GoldenPathUiTest.opensSettingsPanelWithThemeAndUpdateControls` fails after chips become a dropdown: Dark theme is missing, or Theme remains after one Back |
| **Cause** | `MenuDropdown` hides options until expanded. `ExposedDropdownMenu` consumes the first Back to dismiss the menu |
| **Fix** | Open the current value (`System theme`), pick `Dark theme`, then Back until `Check for updates` is gone |
| **Prevention** | Settings smoke tests must expand exclusive-choice dropdowns; do not assert a chip label that is only inside the menu |
### KB-020 — HUMAN leftover sprint blocks need `parallel_exception`

| Field | Detail |
|-------|--------|
| **Symptom** | CI `Validate Bootstrap Artifacts` and `Template Upgrade Simulation` fail: `Sprint 16 leftover (device smoke): missing ### Parallel table` |
| **Cause** | `check-build-plan-parallel.sh` treats leftover headings as sprint blocks; a single `[HUMAN]` row is not a Parallel table |
| **Fix** | Add `<!-- parallel_exception: HUMAN-only leftover; AGENT/AUTO archived -->` under the leftover heading |
| **Prevention** | After `/cleanup`, leftover HUMAN/ADB sections keep a `parallel_exception` comment or a Parallel table |
### KB-023 — Incoming calls silenced by GATE_CALL and ongoing NLS drop

| Field | Detail |
|-------|--------|
| **Symptom** | Cellular and WhatsApp calls on OP13 (CPH2655) never shout, not even the app name. Listener, FGS, READ_PHONE_STATE, and READ_CALL_LOG are fine |
| **Cause** | SpeakGate treated RINGING as in-call (GATE_CALL). The listener dropped all ongoing posts, so WhatsApp/CallStyle never reached TTS. WhatsApp was also hijacked onto the message channel (off by default). PHONE_STATE was registered without an export flag, aborting TelephonyCallback on API 34+ if that register threw |
| **Fix** | In-call suppression is OFFHOOK only. CALL channel ignores screen-off-only and silent/vibrate. Ongoing VoIP/CATEGORY_CALL posts go through CallChannel with the app label. Register PHONE_STATE with ContextCompat.RECEIVER_EXPORTED; register TelephonyCallback even if the receiver fails |
| **Prevention** | CallSuppressionTest and CallNotificationTest cover RINGING vs OFFHOOK and WhatsApp vs dialer routing. Do not skip sbn.isOngoing for incoming-call posts. Same-key ongoing WhatsApp posts after answer must not restart looping or a second ONCE shout — `CallAnnounceSession` ignores the key until ENDED/removed; VoIP default is ONCE |
### KB-024 — Android Auto / DHU ignores notification and media TTS

| Field | Detail |
|-------|--------|
| **Symptom** | Voice plays on the phone speaker (or not at all) while Desktop Head Unit / car Android Auto is connected. 0.8.2 remapped notification to media; still silent on the head unit |
| **Cause** | DHU projection does not set UI_MODE_TYPE_CAR or expose BUS/USB_ACCESSORY devices (mCarModeEnabled=false). AA AudioHardening also blocks USAGE_MEDIA unless the app is the selected media source. USAGE_NOTIFICATION never enters the car TTS channel |
| **Fix** | Detect projection via content://androidx.car.app.connection (CarConnectionState 1/2). On that path play WAV with USAGE_ASSISTANCE_NAVIGATION_GUIDANCE plus transient ducking focus. Skip MediaSession on the car path so AA uses the TTS stream, not media |
| **Prevention** | With DHU up, content query shows CarConnectionState=2 and dumpsys audio shows OpenShouter USAGE_ASSISTANCE_NAVIGATION_GUIDANCE. Gearhead logs CAR.AUDIO.TTS / enabling stream: TTS. Do not treat A2DP-only as sufficient AA detection |
### KB-025 — Windows `simulate-template-upgrade` drops path slashes

| Field | Detail |
|-------|--------|
| **Symptom** | Local `/ship` regress fails `check-bootstrap-engine.sh` with `/bin/bash: C:Users...scriptscheck-required-status-jobs.sh: No such file` |
| **Cause** | Git Bash on Windows concatenates a Windows path without separators when the upgrade sim invokes a `.sh` from Python |
| **Fix** | Treat as local-only. Linux CI job **Template Upgrade Simulation** is the gate |
| **Prevention** | Do not block `/ship` on this Windows flake when `pre-release-gate` and the main CI upgrade job are green |
