# Roadmap & TODO

Overall status: ✅ MVP ready for table testing (8/9/10 races + 14/1)

Legend: ⬜ to do · 🟨 in progress · ✅ done

## Step 0 — Project setup
- ✅ Android project (Kotlin + Compose), `com.racktrack`, min SDK 26
- ✅ Gradle Version Catalog, ktlint, detekt, GitHub Actions CI

## Step 1 — Race domain
- ✅ Match / scores / breaker / foul counts
- ✅ +1, run-out, foul, undo, race completion
- ✅ 8-ball Early 8 / Dry; 9-ball Golden / Dry; 3-foul rack loss (9/10)
- ✅ Unit tests

## Step 2 — 14/1 domain + UI
- ✅ Points engine (PASS, fouls, break foul, 3-foul −15, innings/overtime)
- ✅ FourteenOne board + summary (net points per inning)
- ✅ Unit tests

## Step 3 — Boards & settings
- ✅ Setup (modes, race/distance/innings, who starts)
- ✅ Split boards (landscape + portrait), felt tones, cloth grain
- ✅ Settings (felt, screen on, haptics, defaults, FFB link)
- ✅ End-of-match summary modal
- ✅ Launcher icon (adaptive + round)

## Step 4 — Quality (pragmatic)
- ✅ Domain JaCoCo on CI + PR comment
- ✅ `MatchCoordinator` unit tests (no screenshot / UI golden tests)

## Step 5 — Optional next
- ⬜ Match history persistence (Room) if still wanted after table use
- ⬜ Deeper FFB shot-by-shot mode (call/pocket/push-out) — only if product asks
- ⬜ Camera / AI assistance
- ⬜ Screenshot tests only if UI stabilizes and visual regressions appear in the wild

## Progress notes

- 2026-08-08: Cleanup pass — dead APIs, assets, `appearance/` for felt, docs aligned to multi-mode MVP.
- 2026-08: Expanded beyond 10-ball-only landscape board to 8/9/10 + 14/1 and settings.
- 2026-08-06: Pivoted MVP from shot-by-shot entry to race board (too slow at the table).
