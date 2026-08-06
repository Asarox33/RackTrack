# Roadmap & TODO — MVP v1 (10-ball race board)

Overall status: 🟨 MVP v0.1 in progress (landscape race board)

Legend: ⬜ to do · 🟨 in progress · ✅ done

## Step 0 — Project setup
- ✅ `#SETUP-1` Create Android project (Kotlin + Compose), `com.racktrack`, min SDK 26
- ✅ `#SETUP-2` Android `.gitignore`
- ✅ `#SETUP-3` Gradle Version Catalog
- ✅ `#SETUP-4` ktlint + detekt wired
- ✅ `#SETUP-5` GitHub Actions CI (JDK 25 + lint + tests)

## Step 1 — Race domain (current MVP)
- ✅ `#DOM-RACE-1` `Match` / scores / breaker / foul counts
- ✅ `#DOM-RACE-2` +1, run-out, foul, undo, race completion
- ✅ `#DOM-RACE-TEST` Unit tests for award / foul / undo / completion

## Step 2 — Landscape UI
- ✅ `#UI-SETUP` Setup screen (names, race to N, who breaks)
- ✅ `#UI-BOARD` Split scoreboard on blue cloth (+1 / Run out / Foul / BREAK / undo)
- ⬜ `#UI-HISTORY` Match history (Room) — next

## Step 3 — Deferred deep rules (old shot-by-shot backlog)
- ⬜ Per-shot call / pocket / push-out / 10-ball respot / 3-foul rack loss
- ⬜ Camera / AI assistance

## Progress notes

- 2026-08-06: Pivoted MVP from shot-by-shot entry to landscape race board (too slow at the table).
- 2026-07-31: renamed project to RackTrack; docs in English; ktlint/detekt/CI direction set.
