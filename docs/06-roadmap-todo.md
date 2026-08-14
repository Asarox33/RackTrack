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
- ✅ Auto GitHub Release notes after green CI on `main` when version bumps (no APK asset)
- ✅ Protected `v*` tags (repository ruleset)
- ✅ Docs: versioning guide; MVP wording retired for v1 coverage
- ✅ Settings About: version, build type/time, GitHub repo

## On `main` — line 1.x

### 1.0.2 — scroll discoverability
- ✅ Modal scroll hints (fade + ▼) on match summary, settings, 14/1 visit-end

### 1.1.0 — 14/1 solo training
- ✅ **Solo training (14/1 only)** — practice scorekeeping without an opponent:
  - Setup: when mode is 14/1, toggle **Solo** (single name; hide player 2)
  - Solo board (one score column); same points / PASS / FOUL / BREAK −2 / On Table
  - Summary + history + PDF adapted (one innings column; title like `Alex — solo`)
  - Same HR / avg / innings / fouls stats as duel 14/1
  - Ship before KMP/`rc/2.0.0`

### Later on 1.x (optional)
- ⬜ Play Store / dedicated upload keystore (replace debug-keystore release APKs)
- ⬜ Camera / AI ball detection — only if product revisits automatic scoring
- ⬜ Accounts / find players + shared post-match stats — **never** remote live scoring
- ⬜ **FFB player timeouts** (art. **1.2.16**) — distinct from club pause in 1.0.1

## Explicitly dropped
- ~~Deeper FFB shot-by-shot mode (call/pocket/push-out)~~ — useless without camera auto-detection
- ~~Screenshot / UI golden tests~~ — no high-value client journeys to lock
- ~~PNG share of match summary~~ — PDF only
- ~~Remote / distant live multiplayer scoring~~ — one device scores the match; not a product fit
- ~~Per-shot shot clock / time foul (art. 1.2.13)~~ — fights race/innings scoreboard model

## Progress notes

- 2026-08-14: **1.1.0** — 14/1 solo training (`Match.solo` / setup SOLO toggle; one-column board;
  summary / history / PDF `Alex — solo`).
- 2026-08-13: Patch **1.0.2** — scroll hints (fade + ▼) on overflow modals (summary, settings,
  14/1 visit-end).
- 2026-08-12: Backlog **1.1.0** — 14/1 solo training; keep off 1.0.x patches; before KMP.
- 2026-08-12: Patch **1.0.1** — club match pause (top-right, next to settings): freezes
  duration math only; no max, no pause stats/PDF; not FFB 5‑min timeout / shot clock.
- 2026-08-12: Patch **1.0.1** — 14/1 PDF + summary: one paired innings table
  (# / P1 pts+end / P2 pts+end) instead of two side columns.
- 2026-08-12: Patch **1.0.1** — 14/1 visit-end modal: scroll + height cap so CANCEL/CONFIRM
  stay visible when balls-left > On Table warning shows (landscape + portrait).
- 2026-08-12: Patch **1.0.1** — foul clear (9/10): Golden/Dry stay off after clear; foul chip
  shows TAP TO CLEAR; match foul totals still count FOUL events; 8-ball clear remains no-op.
- 2026-08-10: Backlog player timeouts (1×5 min / player / match, all modes); keep shot clock dropped.
- 2026-08-10: Product boundary — never remote live scoring; optional future accounts only for shared stats.
- 2026-08-10: Declare **v1.0.0**; semver + cut-release / tag protection.
- 2026-08-10: 14/1 visit-end modal (balls-left stepper); board clear-rack / hide BREAK −2 off opening.
- 2026-08-10: Alternate/Winner break for 8/9/10; 14/1 On Table balls remaining.
- 2026-08-09: History (mode-scoped, filters, delete), styled PDF share, BACK labels; dropped deep FFB / screenshots / PNG share.
- 2026-08-08: Cleanup pass — dead APIs, assets, `appearance/` for felt, docs aligned to multi-mode board.
- 2026-08: Expanded beyond 10-ball-only landscape board to 8/9/10 + 14/1 and settings.
- 2026-08-06: Pivoted from shot-by-shot entry to race board (too slow at the table).
