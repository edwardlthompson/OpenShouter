# Publish release super workflow

Invoking this command grants explicit approval for `git push` per destructive-ops rules. When running `/compact`, set `"destructive_ops_approved": ["git push"]` in session state for Cursor shell hooks.

Read and execute each sub-command in order. After each step, summarize pass/fail.

1. Read @.cursor/commands/prerelease.md — execute fully (autofix + optional Codex + hard gate)
2. Read @.cursor/commands/push.md — execute fully
3. After the GitHub Release exists, run `python3 scripts/agent-run.py publish-foss-apk` — Gradle **release-signed** `openshouter-X.Y.Z-foss.apk` on This Computer (`keystore.properties` or `RELEASE_*`). Halt if unsigned, debug-signed, or upload fails. Do not upload a CI-built APK.
4. Read @.cursor/commands/regress.md — execute fully

Begin now.
