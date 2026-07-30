# Roadmap & TODO — MVP v1 (10-ball)

Overall status: 🚧 Not started (framing complete, code to be started)

Legend: ⬜ to do · 🟨 in progress · ✅ done

## Step 0 — Project setup
- ⬜ `#SETUP-1` Create the Android project (Kotlin + Compose), package name (`com.racktrack`), min SDK
- ⬜ `#SETUP-2` Configure Git, standard Android `.gitignore`
- ⬜ `#SETUP-3` Add Gradle Version Catalog (`libs.versions.toml`) with base dependencies (Compose, Room, Hilt, tests)
- ⬜ `#SETUP-4` Configure ktlint + detekt, wire them into a Gradle task
- ⬜ `#SETUP-5` Add GitHub Actions CI workflow (lint + detekt + unit tests) — see `.github/workflows/ci.yml`

## Step 1 — Domain (top priority, before any UI)
- ⬜ `#DOM-1` Model `Player`, `Match`, `Rack`, `Shot`, `FoulType`, `ShotOutcome` (see `docs/03-domain-model.md`)
- ⬜ `#DOM-2` Implement break logic (legal / illegal)
- ⬜ `#DOM-3` Implement push-out
- ⬜ `#DOM-4` Implement normal shot flow (call shot, ascending order, pot)
- ⬜ `#DOM-5` Implement the 10-ball special case (respot)
- ⬜ `#DOM-6` Implement the consecutive foul counter + rack loss
- ⬜ `#DOM-7` Implement end-of-rack / end-of-match detection
- ⬜ `#DOM-TEST` Cover each rule above with at least 1 nominal test + 1 foul test

## Step 2 — Persistence
- ⬜ `#DATA-1` Model Room entities (Match, Rack, historized Shot)
- ⬜ `#DATA-2` Implement repositories (interfaces defined in `domain/`, implementation in `data/`)
- ⬜ `#DATA-3` Map Room entities ↔ domain models

## Step 3 — UI (after the domain layer is tested and reliable)
- ⬜ `#UI-1` Home screen (New match / History / Statistics)
- ⬜ `#UI-2` Match setup screen (players, number of racks)
- ⬜ `#UI-3` Break screen ("legal/illegal break" button, opponent choice on illegal break)
- ⬜ `#UI-4` Match screen — main scoreboard (score, current lowest ball, call shot)
- ⬜ `#UI-5` Match screen — shot input (called ball+pocket, outcome)
- ⬜ `#UI-6` Alert banner "2 consecutive fouls" / end of rack
- ⬜ `#UI-7` Rack/match result screen
- ⬜ `#UI-8` Match history screen
- ⬜ `#UI-9` Simple statistics screen

## Step 4 — MVP polish
- ⬜ `#POLISH-1` Large buttons / one-handed ergonomics (standing next to the table)
- ⬜ `#POLISH-2` Undo last shot (common input mistake in real usage)
- ⬜ `#POLISH-3` Final app icon and name

## V2 — After real-world MVP validation
- ⬜ Add 9-ball mode (similar rules, reuse the shared engine)
- ⬜ Add 8-ball mode
- ⬜ Evaluate actual need for cloud/sync before implementing
- ⬜ Evaluate 14.1 continuous (significantly more complex scoring — rack reformation, ball respotting)

## Progress notes

_Log decisions, blockers, and open questions here as the project moves forward._

- (example) 2026-07-30: chose 10-ball as the MVP mode, product and architecture framing done before any code.
- 2026-07-31: renamed project to RackTrack; all docs and code standardized to English; enforced modern Kotlin/Compose best practices (ktlint, detekt, CI, no `!!`, Hilt DI decided).
