# Code Generation Rules — RackTrack

You are working on RackTrack, a native Android app (Kotlin + Jetpack Compose) for scoring American pool **8/9/10 races and 14/1 continuous**.

## Product shape (current)

- Landscape + portrait split-screen board on billiard cloth (`fullSensor`)
- Setup: game mode (8 / 9 / 10 / 14/1); race length or 14/1 distance + innings; who starts
- Race modes: **+1**, **Run out**, **Foul**; 9-ball Golden/Dry; 8-ball Early 8/Dry
- 14/1: clear-rack **+(On Table − 1)**, **PASS** / **FOUL** (balls-left modal), **BREAK −2** on opening
- Break/hand indicator; undo; end-of-match summary (SHARE PDF + BACK; 14/1: HR, avg, innings, reprises)
- Local history: mode-scoped list, player filters, delete with confirm, reopen stats
- Styled PDF share of full MatchSummary (start/end/duration) — PDF only, not PNG/screenshot
- Settings gear: cloth color, keep screen on, haptics, default race/distance/innings, FFB rules link
- About in Settings: versionName/code, build type, build time (UTC), GitHub repo
- Product version **1.0.0+** (semver in `gradle.properties`; see `docs/07-versioning-and-releases.md`)
- No per-shot ball + pocket entry (too slow at the table; needs camera/AI to be useful)

## Before generating anything

1. Scope/features: read `docs/01-product-specification.md`.
2. Domain race/points logic: keep `domain/` pure Kotlin (no Android imports).
3. Structure/deps: `docs/04-architecture.md` + `gradle/libs.versions.toml`.
4. Conventions: `docs/05-conventions.md`.
5. Ambiguous FFB questions for scoreboard encoding: ask; PDF in `resources/` is authority.
   Do **not** build a deep call-shot referee mode without an explicit product request (+ camera).

## Hard constraints

- No backend, networking, or auth in v1.
- Compose only; activity uses `fullSensor` (portrait + landscape).
- `domain/` never imports `android.*` / `androidx.*` / Room / Compose.
- Felt colors live in `appearance/` (shared by `data` prefs + UI) — do not put Compose UI types in `domain/`.
- No new external dependency without flagging it.
- AGP 9 has built-in Kotlin — do not apply `org.jetbrains.kotlin.android`.
- Do **not** add Hilt unless the user explicitly asks.

## Enforced quality

- No `!!`, no mutable public state, Coroutines/Flow only, sealed results, immutable domain models.
- No business logic inside `@Composable`.
- ktlint + detekt must pass.
