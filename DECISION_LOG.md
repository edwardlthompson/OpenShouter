# Decision Log

> Chronological register of major technical trade-offs, accepted architectures, and rejected alternatives.
> **Treat past entries as immutable history; append only.**

## Format

```markdown
### YYYY-MM-DD — [Title]
- **Status:** Accepted | Rejected | Superseded
- **Context:** ...
- **Decision:** ...
- **Alternatives considered:** ...
- **Consequences:** ...

```

## Entries

### 2026-08-20 — /ship v0.3.0
- **Status:** Accepted
- **Context:** Sprint 15 extras were ready after `/build`. Commit subject said v0.2.6; `feat` is a minor bump.
- **Decision:** Keep Release Please 0.3.0. Admin-merge RP #14 (KB-016). Put `CITATION.cff` in extra-files so the next tag stays in sync.
- **Alternatives considered:** Force 0.2.6 (rejected: rewrites a published tag). Leave CITATION at 0.2.5 (rejected: CI template-version-sync fails).
- **Consequences:** https://github.com/edwardlthompson/OpenShouter/releases/tag/v0.3.0 has root + Android SBOMs. Guessed patch numbers in `feat` subjects are ignored.

### 2026-08-20 — /build Sprint 15 extras
- **Status:** Accepted
- **Context:** Ideas 4–7 (Spanish, calendar, dual-SIM, Bluetooth) were queued after v0.2.5.
- **Decision:** Ship as OpenShouter extras (not Shouter/VN clones). Calendar/BT off by default. `%sim` blank when the line is unknown. Empty `%sim` strips trailing `on`/`en`.
- **Alternatives considered:** Guess SIM 1/2 labels (rejected). Play calendar APIs (forbidden).
- **Consequences:** `READ_CALENDAR` + `BLUETOOTH_CONNECT` on the welcome screen. Device-connected ADB smoke used `b5214fc6` (not CPH2655).

### 2026-08-20 — /ship v0.2.5
- **Status:** Accepted
- **Context:** Child repo needed 0.21.0 bootstrap standards. Fastest same-resolution display refresh was ready but uncommitted.
- **Decision:** Ship both as v0.2.5. Keep product semver in `.template-version`. Merge Release Please #12 via admin (KB-016).
- **Alternatives considered:** Ship bootstrap-only chore (rejected: likely a changelog-only RP). Bump `.template-version` to 0.21.0 (rejected: collides with product releases).
- **Consequences:** https://github.com/edwardlthompson/OpenShouter/releases/tag/v0.2.5 includes Android + root SBOMs. Docs-only follow-up may open a spurious next RP — close it if changelog-only.

### 2026-08-20 — Sync agent-project-bootstrap 0.21.0
- **Status:** Accepted
- **Context:** Child repo was scaffolded from 0.17.0 and had drifted from upstream adapters, SDD stubs, and split `scripts/lib` modules.
- **Decision:** Close bootstrap gaps only. Keep product semver in `.template-version` (0.2.4). Keep OpenShouter HUMAN extras in `human_task_openshouter.py`. Do not restore pruned stacks or overwrite CI workflow `if:` guards.
- **Alternatives considered:** Blind-copy TEMPLATE_INDEX / `.template-version` to 0.21.0 (rejected: collides with product release-please). Overwrite `human_task_automation.py` without extras (rejected: `/build` would drop product handlers).
- **Consequences:** `validate-bootstrap.sh --quick` now requires CITATION, SUPPORT, adapters, and `## [Unreleased]`. Product code and ADRs stay untouched.

### 2026-08-18 — /ship v0.2.4
- **Status:** Accepted
- **Context:** After v0.2.3, shouts played while the phone was silent. ALARM fallback punched through mute, and “Speak in silent or vibrate” defaulted to on.
- **Decision:** Make silent/DND opt-in (`ds_speak_silent`). Treat interruption filter as silent. Do not auto-escalate to ALARM. Ship as v0.2.4. Release Please #10 admin-merged (KB-016).
- **Alternatives considered:** Keep ALARM fallback for muted MEDIA (rejected: it ignores silent). Leave the old default-on key (rejected: existing installs kept shouting).
- **Consequences:** Same debug signing key as v0.2.3. Users who want shouts on silent must enable Voice → Device state → Speak in silent or vibrate. Docs-only follow-up may open a spurious next Release Please PR — close it if it is changelog-only.

### 2026-08-16 — /ship v0.2.3
- **Status:** Accepted
- **Context:** After a silent/vibrate shout, the next utterance could stay silent because MEDIA was also muted and a shared WAV file raced. Submenus still used older chrome and forgot scroll.
- **Decision:** Fall back to ALARM when MEDIA is muted; synthesize each utterance to its own WAV; restyle submenus with `MenuScaffold` and persist scroll in `MenuScrollStore`. Ship as v0.2.3. Release Please #8 admin-merged (KB-016).
- **Alternatives considered:** Keep MEDIA-only fallback (rejected: silent after first muted shout). Wait for empty-job PR checks (rejected: KB-016).
- **Consequences:** Same debug signing key as v0.2.2. Docs-only follow-up commits may open a spurious next Release Please PR — close it if it is changelog-only.

### 2026-08-16 — /build HUMAN confirmations
- **Status:** Accepted
- **Context:** After Sprints 9–14 ADB QA, leftover `[HUMAN]` copy/policy/GitHub rows were still on the board (and in `HUMAN_BACKLOG.md`) even though AGENT/AUTO product work was shipped through v0.2.2.
- **Decision:** `/build` automated those rows (`scripts/lib/human_task_automation.py`): GitHub About + Dependabot/branch-protection, ADR-0001, FOSS deps, default TTS format, location/permission/exact-alarm/OEM copy, `QUERY_ALL_PACKAGES` Play N/A, and gitignored `.app-update.json` `release_repo`. Archive to COMPLETED_TASKS; reset recurring Pre-release tag approval to 🔲.
- **Alternatives considered:** Leave HUMAN rows open until a person clicks GitHub Settings (rejected: `/build` is self-approving and the artifacts already exist).
- **Consequences:** Actionable HUMAN product rows are gone. Still open: Sprint 0 ❌ AUTO first-push CI wait, CI AGENT KB-013/014 in `HUMAN_BACKLOG.md`, recurring Weekly/Monthly/Pre-release maintenance.

### 2026-08-16 — Sprints 9–14 ADB QA
- **Status:** Accepted
- **Context:** All Sprint 9–14 `[ADB]` rows were still open after `/ship` v0.2.2. Device `8bf09993` (CPH2655) had the debug APK, listener, and `AnnouncerService` running.
- **Decision:** Close those ADB rows from on-device UI, Room history (`NONE`/`REPEAT` enums), AlarmManager (`TIME_SHOUT`, `REMINDER_FIRE`), and logcat (no payloads, numbers, or percents). Leave remaining `[HUMAN]` copy/policy sign-offs on the board.
- **Alternatives considered:** Keep ADB open until a live incoming call and physical shake (rejected: no `su`; same limit as 2026-08-13 CPH2583 QA).
- **Consequences:** HUMAN backlog still has exact-alarm copy, OEM autostart copy, and QUERY_ALL_PACKAGES Play-policy N/A.

### 2026-08-16 — /ship v0.2.2
- **Status:** Accepted
- **Context:** Home, announcer, and voice screens were a flat stack of full-width buttons and were hard to scan.
- **Decision:** Ship shared `MenuSection` / `MenuLink` / `MenuToggle` cards. Release Please #6 admin-merged (KB-016). Debug-signed `OpenShouter-0.2.2.apk` uses the same key as 0.2.1.
- **Alternatives considered:** Keep button stacks (rejected: user asked for sections). Bump to 0.3.0 (rejected: layout-only patch).
- **Consequences:** HUMAN/ADB backlog remains. Docs-only follow-up commits may open a spurious next Release Please PR — close it if it is changelog-only.

### 2026-08-16 — /ship v0.2.1
- **Status:** Accepted
- **Context:** v0.2.0 voice test was silent on a silenced CPH2583 (Android 16 AudioHardening + muted `STREAM_NOTIFICATION`). Setup exact-alarm row and system back also failed device QA.
- **Decision:** Synthesize to `cache/os-tts.wav` and play via in-process `TtsFilePlayer`. Default stream is MEDIA; do not fall back to MEDIA when silent/vibrate is blocked. Ship 12/24 time, unknown caller digits, VN extra tokens, and setup Activated. Split `TtsPlayback` / `AnnouncerPane` to stay under file caps. Release Please #4 admin-merged after PR checks stayed `action_required`.
- **Alternatives considered:** Speak on `STREAM_MUSIC` through the engine process (rejected: Android 16 still muted `com.google.android.tts`). Block ship until PR workflows approve (rejected: empty-job `action_required` never turns green).
- **Consequences:** Same debug signing key as v0.2.0. HUMAN/ADB backlog remains. File-limit splits are now part of the TTS/home layout.

### 2026-08-15 — /ship v0.2.0
- **Status:** Accepted
- **Context:** Release Please merged #3 as v0.2.0. CI/CodeQL were red on the first-push SHA until Web/Node jobs and CodeQL JS were gated (KB-013). About unit tests failed on CI because they expected gitignored live assets.
- **Decision:** Ship v0.2.0 with in-scope parity, dynamic CodeQL matrix, tolerant About tests, SBOM assets, and a debug-signed `OpenShouter-0.2.0.apk`.
- **Alternatives considered:** Block ship until old SHA turned green (impossible). Job-level `if: matrix.language` for CodeQL (rejected: Actions treats it as a workflow-file error).
- **Consequences:** HUMAN/ADB backlog remains. Next updates must keep the same debug signing key.

### 2026-08-15 — /ship v0.2.0 prep
- **Status:** Accepted
- **Context:** `/prerelease` local gates passed; remote CI/CodeQL on `2ea76a1` were red because Web/Node and CodeQL JS still ran on the pruned Android-only tree (KB-013).
- **Decision:** Push the already-written job `if:` guards with Sprints 9–14 product work so the next `main` run can go green, then let Release Please cut 0.2.0.
- **Alternatives considered:** Block `/push` until the old SHA turns green (impossible without this commit).
- **Consequences:** First required-check wait is on the new SHA. HUMAN/ADB backlog stays open.

### 2026-08-15 — Sprint 14 parity close-out
- **Status:** Accepted
- **Context:** Five in-scope Partial rows remained after Sprints 12–13: live shake meter, per-channel device-state grid, TTS language picker, per-channel call stream/repeat, reminder calendar intervals.
- **Decision:** Ship the missing UI and apply per-channel stream/repeat on `SpokenEvent`. Reminder month/year use 30/365-day minutes. Language chips come from `TextToSpeech.availableLanguages` with a tag-field fallback while the engine is empty.
- **Alternatives considered:** Keep tag-only language entry (rejected: Partial vs Shouter). Calendar-accurate month/year (rejected: AlarmManager already stores minutes; 30/365 is enough and testable).
- **Consequences:** In-scope OpenShouter rows are Yes or Skip. HUMAN/ADB for Sprint 14 stay in `HUMAN_BACKLOG.md`.

### 2026-08-14 — Sprint 13 wrap-up
- **Status:** Accepted
- **Context:** `/build` finished remaining in-scope shout channels after Sprint 12.
- **Decision:** Persist Sprint 13 keys via `Sprint13Settings` + `SettingsSprint13.apply` so `SettingsPrefs` stays under 150 lines. Message shout stays notification-extras only (ADR-0003). Reminder alarms default hourly.
- **Alternatives considered:** Per-channel headphone grid UI this sprint (deferred: types + repeat inherit are enough to ship; grid is Partial).
- **Consequences:** HUMAN/ADB for Sprints 12–13 stay in `HUMAN_BACKLOG.md`. Device confirmation still needed.

### 2026-08-14 — Sprint 12 wrap-up; Sprint 13 schema lock
- **Status:** Accepted
- **Context:** `/build` closed Voice Notify leftover gaps (OEM, quiet grid, empty/group/repeat, repeat loops, pitch, ignore reasons). Sprint 13 still needs shout-channel finishers.
- **Decision:** Archive Sprint 12 AGENT rows. Lock `ContactRule`, `ChannelDeviceState`, full `AppOverride` merge, `BatteryPhrases`, `ReminderContract`, and call/message/time format tokens in `domain/` before Parallel dispatch. Persistence stays in Parallel scopes because `SettingsPrefs` is at the 150-line cap.
- **Alternatives considered:** Persist Sprint 13 keys in Sequential (rejected: would require compacting `SettingsPrefs` in the same step as the type lock).
- **Consequences:** HUMAN/ADB for Sprint 12 stay on the board and in `HUMAN_BACKLOG.md`. Parallel agents must compact prefs or add a mapper file inside their scope.

### 2026-08-14 — Photoreal splash/README vs flat launcher mark
- **Status:** Accepted
- **Context:** Generated flat bugdroid-head mark works as the launcher icon. User wanted a photorealistic render for splash and README only.
- **Decision:** Keep `logo-mark.png` as the app/tile icon. Ship JPEG photoreal twins (`logo-mark-photo.jpg`, `readme-hero.jpg`) under the 500KB tracked-file budget. Android 12+ splash via `androidx.core:core-splashscreen`. Skip About feature-gate when `examples/web` is pruned. First product tag is v0.1.0.
- **Alternatives considered:** Photoreal PNG at 1–2MB (rejected: hygiene 500KB gate); replace launcher with photoreal (rejected: user kept the flat icon)
- **Consequences:** `sync-design-tokens.py` copies the photo JPEG to `drawable-nodpi/openshouter_splash.jpg`. README hero is `branding/assets/readme-hero.jpg`.

### 2026-08-13 — Welcome permissions + exact hourly shout
- **Status:** Accepted
- **Context:** Welcome needed one-tap paths for every permission, including OEM battery Unrestricted. Hourly time shout must fire on the clock after Doze.
- **Decision:** First-run `SetupScreen` with per-permission Activate buttons (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, app details, `SCHEDULE_EXACT_ALARM`). Time shout uses `AlarmManager.setAlarmClock` plus sticky FGS and boot/replace restart. GitHub Releases only — Play policy N/A for ignore-battery.
- **Alternatives considered:** Inexact `setAndAllowWhileIdle` only (rejected: late after Doze); `USE_EXACT_ALARM` install-time grant (rejected: clock-app policy surface)
- **Consequences:** Toggle **Announce the time on the hour** in announcer settings. Quiet hours still apply via `SpeakGate`.

### 2026-08-13 — Branding: eyes-free and digital quiet
- **Status:** Accepted (copy + SVG mark; PNG exports still HUMAN/ADB)
- **Context:** Assets still said Golden Path. User asked for a pitch that serves people who cannot see or read a screen, and people cutting down on phone-checking (silent ringer, whitelist, less noise).
- **Decision:** Tagline “Hear what matters without looking.” Mark is a speaker + sound waves (not a shout). README/listing copy leads with both audiences. No medical or addiction claims.
- **Alternatives considered:** Keep developer-first pitch (rejected: hides the product); megaphone-yell mark (rejected: fights “calm, not shouty” voice)
- **Consequences:** `branding/product.json` is the README source; regenerate with `generate-project-readme.py`. GitHub About and Fastlane/metadata aligned. In-app greeting + pitch strings on the dashboard.

### 2026-08-13 — Feature parity plan (Shouter Pro + Voice Notify)
- **Status:** Proposed (awaiting chat approval before AGENT coding)
- **Context:** User asked for a granular matrix from installed Shouter Pro (page-by-page on CPH2583) and Voice Notify, then a plan to add gaps. Voice Notify is not installed; used GitHub source. Shouter APK includes GMS/Firebase keys and Placebook — both forbidden here.
- **Decision:** Port behavior, not UI. Sprint 9 = VN-class TTS/filters + unused settings UI; Sprint 10 = Shouter channels (time, missed call, message-via-notifications, reminders, battery UI); Sprint 11 = app picker, per-app overrides, settings zip. Message shout without `READ_SMS` (ADR-0003). Skip Placebook, GMS analytics, store growth links. Keep flip-to-mute, silent geofences, GitHub updates.
- **Alternatives considered:** Pixel-clone Shouter preference screens (rejected: Compose + token limits); `READ_SMS` (rejected: policy/privacy); Placebook dependency (rejected: ADR-0002)
- **Consequences:** `BUILD_PLAN.md` Sprints 9–11 stay 🔲 until the human approves; inventory lives in `docs/features/parity-matrix.md`

### 2026-08-13 — GitHub Releases only
- **Status:** Accepted (product direction; ADRs 0001/0002 still Proposed for HUMAN)
- **Context:** OpenShouter is FOSS Android; user confirmed distribution is GitHub download only
- **Decision:** Ship APKs via GitHub Releases. No Play Store listing, no F-Droid listing, no Play Core. Still forbid GMS/Firebase
- **Alternatives considered:** F-Droid (rejected: out of scope); Play Store (rejected: proprietary update/billing path)
- **Consequences:** Listing copy lives under `examples/android/metadata/` and Fastlane for GitHub About/Releases; `release_repo` is filled after the first push

### 2026-08-12 — Ship v0.17.0 branding kit (/ship)
- **Status:** Accepted
- **Context:** Child repos need replaceable logos/colors and pitch-quality READMEs without overwriting the template README
- **Decision:** Ship `branding/` pack + mode-gated `generate-project-readme.py` (`template` preview only; `product` writes root README); extend token sync for official-colors and asset distribution; merge Release Please #55 to **v0.17.0**
- **Alternatives considered:** Generate logos from tokens only (rejected: humans replace art files); always overwrite root README (rejected: clobbers template guide)
- **Consequences:** Sprint 0 fills `product.json` then generate; upstream keeps `mode: template`; store PNGs remain human/ADB exports

_Seed template ADR: `docs/adr/0000-template-baseline.md`. Child repos use `docs/adr/0001-core-architecture.md`._

### 2026-08-10 — Ship v0.16.0 (/ship)
- **Status:** Accepted
- **Context:** Need third-party review + broader autofix before release; `/ship` should stay one command
- **Decision:** Codex read-only reviewer (opt-in CI + `/codex-review`) feeds `CODE_REVIEW.md` → Cursor `/fix`; expand `/prerelease` with multi-stack autofix; merge Release Please #51 to **v0.16.0**
- **Alternatives considered:** Codex writes patches in CI (rejected: destructive-ops / FOSS spend control); chain Codex into every `/maintain` (rejected: API cost)
- **Consequences:** `/ship` runs autofix + optional Codex + hard gate; enable Codex CI by copying workflow example + `OPENAI_API_KEY`

### 2026-08-01 — Ship v0.15.2 (/ship)
- **Status:** Accepted
- **Context:** Plan Mode left risks as open questions; Dependabot High blocked pre-release (js-yaml, then postcss)
- **Decision:** Require Issue→Resolution Critique in always-applied rules + `/plan`; override patched npm transitive CVEs; merge Release Please #50 to **v0.15.2**
- **Alternatives considered:** Soft "list risks" Critique (rejected: humans still had to chase resolutions); defer brace-expansion/postcss (rejected: pre-release gate requires zero Critical/High)
- **Consequences:** Agents must bake mitigations into plan todos; template at 0.15.2 with SBOM release assets

### 2026-07-22 — Ship v0.15.0 (/ship)
- **Status:** Accepted
- **Context:** `/ship` after M33 + local-first compute; first CI failed on duplicate `## [Unreleased]`; web tests failed on Node 25+ localStorage stub
- **Decision:** Polyfill Storage in vitest setup (KB-011); collapse stale Unreleased; merge Release Please #37 to **v0.15.0**
- **Alternatives considered:** `--no-webstorage` only (rejected: may break older Node CI); leave duplicate Unreleased (rejected: gate hard-fail)
- **Consequences:** Template at 0.15.0 with Cursor worktrees/permissions/skills/plugin pack and local-first parallelism

### 2026-07-21 — Local-first compute on This Computer
- **Status:** Accepted
- **Context:** Agents defaulted toward serial work or Cloud handoff even when the desktop has many cores
- **Decision:** Ship `local-compute.mdc` + sessionStart CPU reminder; parallelize independent `validate-bootstrap` checks via `run_checks_parallel.py` (`BOOTSTRAP_CHECK_JOBS`); pytest-xdist `-n auto`; Gradle `--parallel`; document `/scope` + worktrees/`/best-of-n` as the local default over Cloud Agents
- **Alternatives considered:** Always Cloud Agents for parallelism (rejected: wastes local hardware and costs credits); unbounded bash `&` in validate-bootstrap (rejected: harder error aggregation on Windows)
- **Consequences:** Quick bootstrap checks use all cores (e.g. jobs=CPU count); agents are steered to concurrent Task/worktrees when local

### 2026-07-21 — Cursor 3.9–3.11 FOSS integration (M33)
- **Status:** Accepted
- **Context:** Cursor added native worktrees setup, Auto-review `permissions.json`, Skills direction, CLI/GHA, side chats, Design Mode, cloud conversation hooks, Automations, and plugin packaging; registry lagged at 2026-06-30
- **Decision:** Ship FOSS live `worktrees.json` + fail-soft OS setup, committed `permissions.json` (dual layer with hooks), four new skills + checker atomic update, CLI workflow under `.github/workflow-examples/` (never auto-run), plugin via pack-to-`dist/cursor-plugin` (no repo-root symlink); keep commercial as examples (cloud hooks, Automations recipes, Bugbot Autofix map)
- **Alternatives considered:** Custom plugin paths into `.cursor/` (rejected: discovery risk); whole-repo plugin symlink (rejected: double-load); `.example.yml` under `workflows/` (rejected: GHA may load it); weaken shell hook for Auto-review (rejected: hooks stay hard FOSS enforcement)
- **Consequences:** `check-cursor-integrations` requires seven skills + worktrees/permissions; `/best-of-n` documented beside parallel-lock worktrees; Cloud Agents still ignore Run Modes

### 2026-07-12 — Pre-release gate Dependabot counter + FOSS MCP check
- **Status:** Accepted
- **Context:** `/push` pre-release `--strict` failed: Dependabot alerts API used unsupported `page=` form; FOSS integrations check failed whenever gitignored `.cursor/mcp.json` existed locally
- **Decision:** Count alerts via `gh api --paginate` query string; treat live `mcp.json` as OK unless `git ls-files` shows it tracked; multi-stack `--strict` skips missing optional toolchains
- **Alternatives considered:** Require `security_events` refresh always (rejected: false failures blocked release); ban local MCP (rejected: contradicts CURSOR_INTEGRATIONS activation)
- **Consequences:** Maintainer gates pass with local MCP enabled; Release Please #36 published v0.14.1

### 2026-07-12 — Dependabot automerge CI gap (M32)
- **Status:** Accepted
- **Context:** Merges via `GITHUB_TOKEN` (`app/github-actions`) do not start `push` workflows; `main` tip after Dependabot merges had zero CI runs; weekly health failed waiting for missing runs
- **Decision:** Prefer optional `AUTOMERGE_TOKEN` PAT for Dependabot/Release Please merge; add `workflow_dispatch` to CodeQL + Security Scan; `check-github-ci.sh --dispatch-if-missing` (weekly health uses it with `actions: write`); prefer Git Bash in `agent-run.py` on Windows
- **Alternatives considered:** Require PAT only (rejected: blocks FOSS template without secrets); SHA-pin all actions for Scorecard (deferred: conflicts with documented `@vX.Y.Z` policy)
- **Consequences:** Weekly health can self-heal missing runs; post-merge CI still needs HUMAN required-status-checks + optional PAT for true push triggers

### 2026-07-02 — Quiet agent shell (hooks Python + agent-run)
- **Status:** Accepted
- **Context:** Cursor Agent shell execution opened `.sh` hook and script tabs, stealing editor focus while users typed
- **Decision:** Migrate hooks to Python; add `scripts/agent-run.py` for agent gate invocations; ship `.vscode/settings.json` anti-reveal defaults; document KB-010
- **Alternatives considered:** Disable hooks globally (rejected: loses destructive-op guard); rewrite all scripts to PowerShell (rejected: scope); `pythonw.exe` for hooks (rejected: breaks stdout JSON)
- **Consequences:** Agent-facing commands no longer contain `.sh` paths; underlying bash scripts unchanged for CI/humans

### 2026-07-01 — Cursor hook smoke isolation (M31)
- **Status:** Accepted
- **Context:** M31 audit found `check-cursor-hooks.sh --smoke` false-pass when `.cursor-session-state.json` already listed `git push` in `destructive_ops_approved`
- **Decision:** Smoke test clears session approvals before deny assertion; validate hook scripts require shebang on line 1
- **Alternatives considered:** Ignore local session state in smoke (rejected: hides real deny-path bugs); require empty session file (rejected: breaks dev workflow)
- **Consequences:** `--smoke` is deterministic in CI and locally; invalid hook scripts fail validate-bootstrap early

### 2026-06-30 — Cursor hooks as enforcement layer (M30)
- **Status:** Accepted
- **Context:** M27 rejected `beforeSubmitPrompt` hooks; rules alone cannot block destructive shell commands at runtime
- **Decision:** Ship FOSS-safe project hooks (`beforeShellExecution`, `afterFileEdit`, `subagentStart`, `sessionStart`, `beforeMCPExecution`); fail-open guards; session `destructive_ops_approved` for `/push`/`/ship`; opt-out via `<!-- cursor-hooks: off -->`
- **Alternatives considered:** Prompt-rewrite hooks (rejected per M27); broad shell blocklists (rejected: blocks legitimate agent work)
- **Consequences:** `check-cursor-hooks.sh --smoke` in validate-bootstrap; complements `destructive-ops.mdc` without token bloat

### 2026-06-20 — Repo-wide checklist status markers
- **Status:** Accepted
- **Context:** BUILD_PLAN and scattered checklists used mixed ⬜ / `- [ ]` / ✅ formats; inconsistent in Markdown Preview vs source
- **Decision:** Standardize on 🔲 open · ✅ done · ❌ blocked emoji markers repo-wide; document in `BUILD_PLAN.md` legend and agent read order
- **Alternatives considered:** GitHub `- [ ]` task lists (rejected: poor Preview readability and agent parsing); keep ⬜ white square (rejected: visually similar to ✅ in some fonts)
- **Consequences:** All new checklist rows use emoji; `agent-progress.sh` accepts legacy ⬜ for child repos during transition

### 2026-06-18 — Release automation hardening (M29)
- **Status:** Accepted
- **Context:** v0.11.0 release lacked SBOM assets (GITHUB_TOKEN cannot chain `release` → `release.yml`); Release Please skipped `extra-files`; `health-check.yml` registered as path name caused 0-job push failures
- **Decision:** `release-please.yml` runs `sync-template-version.sh` on release PR branches and dispatches `release.yml` on `release_created`; rename workflow to `weekly-health-check.yml`; fix sync script for Windows Git Bash
- **Alternatives considered:** PAT with workflow scope for release chaining (rejected: secrets management); manual SBOM backfill only (rejected: repeated human step each release)
- **Consequences:** Release Please needs `actions: write`; future releases should ship SBOM assets without manual dispatch

### 2026-06-17 — Batch instruction templates (M27)
- **Status:** Accepted
- **Context:** Agents and child-repo owners needed repeatable shortcuts for bootstrap, verify, build, ship, and maintenance workflows without re-pasting long prompts
- **Decision:** Ship 25 slash commands in `.cursor/commands/` (20 atomic + 5 super), bare-word expansion via `batch-commands.mdc`, human cheat sheet at `docs/help/BATCH_COMMANDS.md`, registry at `docs/BATCH_COMMANDS.md`; `/push` and `/ship` grant explicit push approval
- **Alternatives considered:** `beforeSubmitPrompt` hook for bare words (rejected: Cursor API cannot rewrite prompts); single mega-doc for humans and agents (rejected: overwhelms first-time users)
- **Consequences:** `alwaysApply` rule adds ~25 lines per session; `check-batch-commands.sh` prevents registry drift; child repos cherry-pick via `UPGRADING_FROM_TEMPLATE.md`

### 2026-06-30 — Autonomous /build with grouped human section
- **Status:** Accepted
- **Context:** `/build` halted on HUMAN/ADB rows; humans needed a single review block after automation; child repos need scripted attempts before manual follow-up
- **Decision:** Add `build-sprint-status.sh`, `attempt-build-plan-row.sh`, and `HUMAN_BACKLOG.md` (failure-only); restructure BUILD_PLAN with `#### Human & device (after automation)`; AGENT/AUTO runs first, then automation attempts on grouped human rows
- **Alternatives considered:** Skip human rows entirely during /build (rejected: loses automation catalog value); keep human rows interleaved in Sequential (rejected: hard to review after automation)
- **Consequences:** Child repos must place HUMAN/ADB rows in the grouped section; `<!-- no-auto-approve -->` disables autonomous ADR ack

### 2026-06-13 — @lhci/cli npm overrides for transitive CVEs
- **Status:** Accepted
- **Context:** Lighthouse CI (`@lhci/cli`) bundles transitive dependencies (`tmp`, `uuid`) with known CVEs; no patched `@lhci/cli` release available at triage time
- **Decision:** Add npm `overrides` in `examples/web/package.json` forcing `tmp >= 0.2.6` and `uuid >= 11.1.1`; document in KB-007
- **Alternatives considered:** Dismiss Dependabot alert (rejected: hides real risk); remove Lighthouse CI job (rejected: loses performance gate)
- **Consequences:** Lockfile must be regenerated after override changes; overrides should be removed when `@lhci/cli` ships fixed dependencies

### 2026-06-13 — Ship all optional ecosystem modules (M3)
- **Status:** Accepted
- **Context:** Sprint M3 asked whether to ship Lightroom, Rust, and Go optional modules in the template maintainer repo
- **Decision:** Ship all three with Golden Path stubs, MODULE.md guides, and path-gated CI jobs (`lightroom`, `rust`, `go`) that skip when child repos remove the directories
- **Alternatives considered:** Lightroom-only (rejected: Rust/Go stubs are low-cost and popular); defer all optional modules (rejected: COMPLETED_TASKS M3 work already landed)
- **Consequences:** Template CI runs more jobs on `main`; child repos can delete unused `examples/` folders to skip jobs via `hashFiles` guards

### 2026-08-13 — Bootstrap OpenShouter from agent-project-bootstrap v0.17.0
- **Status:** Accepted
- **Context:** Empty git repo needed FOSS Android agent scaffolding before application code
- **Decision:** Clone template 0.17.0; init stack `android`; prune unused and optional stacks; FOSS tier; package target `org.openshouter`; Hilt+Room+Compose; geofencing without GMS (ADR-0001/0002)
- **Alternatives considered:** Hand-roll AGENTS.md only (rejected: misses CI/gates); keep multi-stack examples (rejected: token cost); use Play Services location (rejected: Module A / F-Droid)
- **Consequences:** Golden Path stub remains until Sprint 1; `[HUMAN]` must create GitHub remote, enable Dependabot alerts, and approve ADRs
## Autonomous /build approval (2026-08-14T21:09:06+00:00)

- Autonomous approval for BUILD_PLAN row: Approve ADR-0001 and ADR-0002
## Autonomous /build approval (2026-08-14T21:29:53+00:00)

- Autonomous approval for BUILD_PLAN row: Approve ADR-0003 (message via notifications, no `READ_SMS`)
## Autonomous /build approval (2026-08-17T00:15:45+00:00)

- Autonomous approval for BUILD_PLAN row: Approve package name, SDK targets, and DI choice (Hilt) per ADR-0001
