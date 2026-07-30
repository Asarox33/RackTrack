# Project Conventions & Enforced Best Practices

These are not suggestions — they are enforced rules for anyone (human or Cursor) writing code in this repo. Anything not explicitly allowed here should be flagged before being introduced.

## 1. Kotlin naming

- Classes / objects / enums / sealed types: `PascalCase` (`RackPhase`, `RecordShotUseCase`)
- Functions / properties / local variables: `camelCase`
- Top-level constants: `UPPER_SNAKE_CASE`
- Files: one file = one main public class/type, same name (`RecordShotUseCase.kt`)
- Composables: `PascalCase`, named after what they render (`ScoreboardCard`, not `ScoreboardComponent1`)
- Use cases: infinitive verb + `UseCase` suffix (`StartMatchUseCase`, not `MatchStarter`)
- Packages: all lowercase, no underscores (`com.racktrack.domain.rule`)

## 2. Enforced modern Kotlin practices

These are non-negotiable for this codebase:

- **Immutability by default**: use `val` everywhere unless mutation is strictly required. Domain models are always immutable `data class`. Any mutable state lives only behind a `StateFlow`/`MutableStateFlow` in a ViewModel, never exposed as mutable outward.
- **No `!!` (non-null assertion operator)**. Use safe calls, `requireNotNull` with a message, or restructure the code so the null case is handled explicitly.
- **Sealed interfaces/classes for finite result types** (e.g. `ShotOutcome`), not boolean flags or nullable fields standing in for state.
- **No global mutable singletons.** All shared state is scoped and injected via Hilt.
- **Coroutines + Flow only** for async/reactive code. No callbacks, no RxJava, no `Thread` usage directly.
- **Explicit function return types** for any public function in `domain/` and `data/` (type inference is fine for small private/local functions only).
- **No wildcard imports.**
- **Extension functions** over utility/helper classes with static methods, where idiomatic.

## 3. Compose-specific practices

- **State hoisting**: composables receive state and lambdas as parameters; they do not own business state themselves.
- Every non-trivial composable must have a `@Preview` for manual visual verification.
- No business logic inside a `@Composable` function — logic lives in the ViewModel or domain layer, the composable only renders.
- Use `remember`/`rememberSaveable` correctly — never store domain objects that should survive process death without `rememberSaveable` or a ViewModel-backed source.

## 4. Tooling (enforced, not optional)

- **ktlint**: enforces formatting. Must pass with zero violations before any commit is considered done.
- **detekt**: static analysis for complexity, code smells, and the rules above (e.g. can be configured to flag `!!` usage). Must pass in CI.
- **Gradle Version Catalog** (`gradle/libs.versions.toml`): every dependency version is declared once, referenced everywhere else. No inline version strings in module `build.gradle.kts` files.
- **CI (GitHub Actions)**: every push/PR runs, at minimum: `ktlint check`, `detekt`, unit tests (`domain` layer tests are mandatory to pass — see `.github/workflows/ci.yml`).

## 5. Git commit conventions

Format: `type(scope): short imperative description`

Allowed types:
- `feat` — new feature
- `fix` — bug fix
- `refactor` — code change with no behavior change
- `test` — adding/changing tests
- `docs` — documentation only
- `chore` — config, dependencies, tooling

Examples:
```
feat(domain): implement the three-consecutive-fouls rule
fix(rule): correct 10-ball respot after a fouled push-out
docs(readme): update MVP scope
```

Suggested scope = the folder concerned (`domain`, `data`, `presentation`, `rule`, `readme`...).

## 6. Branching & PR discipline

Even solo, keep this discipline — it makes reviewing your own work with Cursor much easier:
1. One roadmap task = one branch = one coherent set of commits.
2. Never mix a business rule change with a UI change in the same commit.
3. Any addition to `domain/rule` must ship with a test (minimum: nominal case + one foul case).
4. No commit is "done" if ktlint or detekt would fail on it.

## 7. Testing

- **Highest test coverage priority: `domain/rule`.** This is the layer encoding FFB rules — a mistake here is the most costly.
- Test naming: `` `given X, when Y, then Z` `` (backtick function names, JUnit5 style), e.g.:
  ```kotlin
  @Test
  fun `given an illegal break, when resolved, then opponent gets ball-in-hand`() { ... }
  ```
- No end-to-end test required for v1; domain unit tests come first, ViewModel tests second, UI tests are optional for the MVP.

## 8. In-code documentation

Functions in `domain/rule` that encode a specific FFB rule must reference the corresponding article in a comment:
```kotlin
// FFB 10-ball rule, art. 1.5.03: the break is illegal if no ball is pocketed
// and fewer than 4 object balls contacted a rail.
fun isBreakLegal(...): Boolean { ... }
```
This makes it fast to trace a rule back to its source when reviewing or fixing a bug.

## 9. What we explicitly ask Cursor to enforce

- Always read `docs/03-domain-model.md` before touching `domain/`.
- Never introduce a new external dependency not listed in `docs/04-architecture.md` without flagging it explicitly.
- Ask a question rather than guess an ambiguous game rule — point back to `docs/02-game-rules-10-ball.md` / the source PDF.
- Never generate code that violates §2 or §3 above (no `!!`, no business logic in composables, no mutable public state, no callbacks/RxJava).
- Treat a failing ktlint/detekt check as a blocking issue, not a follow-up task.
