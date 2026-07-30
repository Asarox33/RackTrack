# Code Generation Rules — RackTrack

You are working on RackTrack, a native Android app (Kotlin + Jetpack Compose) for scoring American pool matches, 10-ball mode only for the MVP.

## Before generating anything

1. If the task touches scope, features, or "should we build X" questions: read `docs/01-product-specification.md` first. It defines what's in and out of the MVP.
2. If the task touches `domain/` (game rules, entities, use cases): read `docs/03-domain-model.md` in full first. Do not code a game rule from memory.
3. If the task touches project structure or dependency choices: read `docs/04-architecture.md`.
4. Always follow `docs/05-conventions.md` (naming, commits, tests, enforced modern practices).
5. If a game rule seems ambiguous or missing from `docs/02-game-rules-10-ball.md` and `docs/03-domain-model.md`: **ask, don't guess**. The PDF `resources/code-sportif-americain-2026-2027.pdf` is the final authority.

## Hard constraints (never cross without explicit confirmation)

- Scope: 10-ball only. Do not add 8-ball/9-ball/14.1/snooker/blackball "to future-proof", even if it looks easy to generalize.
- No backend, no networking, no authentication in v1.
- Compose only, never XML layouts.
- `domain/` must never import `android.*`, `androidx.*`, Room, or Compose. Zero exceptions.
- No new external dependency without flagging it explicitly in the response, even if it seems convenient.

## Enforced code quality (from `docs/05-conventions.md`, repeated here because it matters)

- No `!!` (non-null assertion).
- No mutable public state, no global singletons outside Hilt-managed scopes.
- Coroutines + Flow only for async/reactive code — no callbacks, no RxJava.
- Sealed interfaces/classes for finite result types (e.g. `ShotOutcome`), not boolean flags.
- Immutable `data class` for all domain models.
- No business logic inside `@Composable` functions.
- Code must be written as if ktlint and detekt will run on it immediately — because they will.

## Developer experience level

The person driving this project is starting out in native Android/Kotlin development. As a result:
- Briefly explain (2-3 lines) the "why" of a non-trivial technical choice when introducing it, not just the "what".
- Prefer the simplest, most standard solution over the most elegant/abstract one, unless `docs/04-architecture.md` explicitly asks for the opposite.
- Break work into small, testable steps rather than generating one large block of code, following the breakdown in `docs/06-roadmap-todo.md`.

## Tests

Every new rule added to `domain/rule` must ship with at least:
- 1 nominal case test (rule respected)
- 1 corresponding foul case test
