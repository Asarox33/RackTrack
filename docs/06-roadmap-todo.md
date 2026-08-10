# Roadmap & TODO

Overall status: ✅ **v1.0.0** table scoreboard + local history + PDF share

Legend: ⬜ to do · 🟨 in progress · ✅ done

## Step 0 — Project setup
- ✅ Android project (Kotlin + Compose), `com.racktrack`, min SDK 26
- ✅ Gradle Version Catalog, ktlint, detekt, GitHub Actions CI

## Step 1 — Race domain
- ✅ Match / scores / breaker / foul counts
- ✅ +1, run-out, foul, undo, race completion
- ✅ 8-ball Early 8 / Dry; 9-ball Golden / Dry; 3-foul rack loss (9/10)
- ✅ Tap-to-clear consecutive fouls (9/10 only)
- ✅ Unit tests

## Step 2 — 14/1 domain + UI
- ✅ Points engine (PASS, fouls, break foul, 3-foul −15, innings/overtime)
- ✅ FourteenOne board + summary (net points per inning)
- ✅ Unit tests

## Step 3 — Boards & settings
- ✅ Setup (modes, race/distance/innings, who starts)
- ✅ Split boards (landscape + portrait), felt tones, cloth grain
- ✅ Settings (felt, screen on, haptics, defaults, FFB link)
- ✅ End-of-match summary modal (SHARE PDF + BACK to setup)
- ✅ Launcher icon (adaptive + round)

## Step 4 — Quality (pragmatic)
- ✅ Domain JaCoCo on CI + PR comment
- ✅ `MatchCoordinator` unit tests (no screenshot / UI golden tests)

## Step 5 — Match history & share
- ✅ Persist completed matches locally (JSON `MatchSummary` snapshots)
- ✅ History scoped to the selected game mode on setup
- ✅ Player-name filters (order-free) + reopen full end-of-match stats
- ✅ Delete match (red ✕ + confirmation dialog)
- ✅ Share styled PDF (start / end / duration; filename includes start date-time)
- ✅ Felt-colored HISTORY / BACK actions

## Step 6 — Break rule & 14/1 table balls
- ✅ Race `BreakRule` Alternate / Winner (setup + settings default)
- ✅ 14/1 `objectBallsOnTable` + board `On Table : n` on shooter panel

## Step 7 — 14/1 PASS / FOUL modal
- ✅ Board: dynamic **+(On Table − 1)** clear-to-re-rack (was fixed +14); no +1/+5/+15; **BREAK −2** only while opening break
- ✅ PASS / FOUL open end-of-inning modal: stepper for balls left (**2–15**), preview visit points
- ✅ Sync formula allows at most one continuous re-rack after live clear-rack taps

## Step 8 — v1.0.0 release pipeline
- ✅ Semver in `gradle.properties` (`versionName` / `versionCode`)
- ✅ Auto GitHub Release + APK after green CI on `main` when version bumps
- ✅ Protected `v*` tags (repository ruleset)
- ✅ Docs: versioning guide; MVP wording retired for v1 coverage
- ✅ Settings About: version, build type/time, GitHub repo

## Post-v1 backlog (optional)
- ⬜ Play Store / dedicated upload keystore (replace debug-keystore release APKs)
- ⬜ Camera / AI ball detection — only if product revisits automatic scoring

## Explicitly dropped
- ~~Deeper FFB shot-by-shot mode (call/pocket/push-out)~~ — useless without camera auto-detection
- ~~Screenshot / UI golden tests~~ — no high-value client journeys to lock
- ~~PNG share of match summary~~ — PDF only

## Progress notes

- 2026-08-10: Declare **v1.0.0**; semver + cut-release / tag protection.
- 2026-08-10: 14/1 visit-end modal (balls-left stepper); board clear-rack / hide BREAK −2 off opening.
- 2026-08-10: Alternate/Winner break for 8/9/10; 14/1 On Table balls remaining.
- 2026-08-09: History (mode-scoped, filters, delete), styled PDF share, BACK labels; dropped deep FFB / screenshots / PNG share.
- 2026-08-08: Cleanup pass — dead APIs, assets, `appearance/` for felt, docs aligned to multi-mode board.
- 2026-08: Expanded beyond 10-ball-only landscape board to 8/9/10 + 14/1 and settings.
- 2026-08-06: Pivoted from shot-by-shot entry to race board (too slow at the table).
