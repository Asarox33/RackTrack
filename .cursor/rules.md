# Code Generation Rules — RackTrack

You are working on RackTrack, a native Android app (Kotlin + Jetpack Compose) for scoring American pool **10-ball races**.

## MVP v0.1 product shape (current)

- Landscape-only split-screen race board on blue billiard cloth
- Per player: **+1**, **Run out**, **Foul**; break indicator; who breaks first; undo
- No per-shot ball + pocket entry (too slow at the table; camera/AI is future)

## Before generating anything

1. Scope/features: read `docs/01-product-specification.md`.
2. Domain race logic: keep `domain/` pure Kotlin (no Android imports).
3. Structure/deps: `docs/04-architecture.md` + `gradle/libs.versions.toml`.
4. Conventions: `docs/05-conventions.md`.
5. Ambiguous FFB rules for a future deep mode: ask; PDF in `resources/` is authority.

## Hard constraints

- Do not add 8-ball/9-ball/14.1 without explicit ask.
- No backend, networking, or auth in v1.
- Compose only; activity stays landscape.
- `domain/` never imports `android.*` / `androidx.*` / Room / Compose.
- No new external dependency without flagging it.
- AGP 9 has built-in Kotlin — do not apply `org.jetbrains.kotlin.android`.

## Enforced quality

- No `!!`, no mutable public state, Coroutines/Flow only, sealed results, immutable domain models.
- No business logic inside `@Composable`.
- ktlint + detekt must pass.
