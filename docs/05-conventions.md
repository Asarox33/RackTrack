# Project Conventions & Enforced Best Practices

These are enforced rules for anyone (human or agent) writing code in this repo.

## 1. Kotlin naming

- Classes / objects / enums / sealed types: `PascalCase`
- Functions / properties / locals: `camelCase`
- Top-level constants: `UPPER_SNAKE_CASE`
- Files: one main public type per file, same name
- Composables: `PascalCase`, named after what they render
- Packages: all lowercase, no underscores (`com.racktrack.domain.model`)

## 2. Enforced modern Kotlin practices

- **Immutability by default**: `val`; domain models are immutable `data class`. Mutable state
  only behind `StateFlow`/`MutableStateFlow` in a ViewModel.
- **No `!!`**. Use safe calls, `requireNotNull` with a message, or explicit null handling.
- **Sealed types** for finite result/state families where useful.
- **No global mutable singletons** for app state.
- **Coroutines + Flow only** for async/reactive code.
- **Explicit return types** on public functions in `domain/` and `data/`.
- **No wildcard imports.**
- Prefer extension functions over static helper classes where idiomatic.

## 3. Compose-specific practices

- **State hoisting**: composables receive state and lambdas; they do not own business state.
- No business logic inside `@Composable` — engines / ViewModel hold rules.
- Use `remember` / `rememberSaveable` correctly; durable state lives in the ViewModel.

## 4. Tooling (enforced)

- **ktlint** + **detekt** must pass (local + CI).
- **Gradle Version Catalog** — no scattered inline versions for catalogued deps.
- **CI**: ktlint, detekt, unit tests + domain JaCoCo on every push/PR to `main`.
  PRs get a coverage comment (`madrapps/jacoco-report`); HTML is also a workflow artifact.
  Local report: `./gradlew :app:domainCoverage`.

## 5. Git commit conventions

Format: `type(scope): short imperative description`

Types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`.

Examples:
```
feat(domain): award golden break in 9-ball
fix(fourteenone): include foul penalties in inning net points
docs(readme): reflect multi-mode v1 product
```

## 6. Branching & PR discipline

1. One coherent change set per branch/PR when practical.
2. Prefer not mixing large domain rule changes with unrelated UI polish in one commit.
3. Engine changes in `domain/` ship with unit tests (nominal + at least one edge/foul case).
4. No commit is done if ktlint/detekt would fail.
5. Shipping a user-facing build: bump `racktrack.versionName` + `racktrack.versionCode` in
   `gradle.properties` (see `docs/07-versioning-and-releases.md`).

## 7. Testing

- **Highest priority: `domain/` engines** (`MatchEngine`, `FourteenOneEngine`, `MatchStats`).
- **Next: `MatchCoordinator`** (setup → board routing, engine dispatch, settings mirrors). Keep it Android-free.
- **Not in v1 scope:** Compose screenshot / golden-image tests (high maintenance vs value).
- Test naming: `` `given X, when Y, then Z` `` (JUnit 5 backticks).
- No E2E required for v1.

## 8. In-code documentation

Functions that encode a specific FFB article should cite it briefly, e.g.:
```kotlin
// FFB 9-ball art. 1.4.03 — 9 pocketed on a legal break wins the rack.
```

## 9. What agents must enforce

- Read `docs/03-domain-model.md` before changing `domain/`.
- Flag new dependencies not listed in `docs/04-architecture.md`.
- Ask rather than invent ambiguous game rules — PDF is authority.
- Never violate §2 / §3 (no `!!`, no business logic in composables, no mutable public state).
- Treat failing ktlint/detekt as blocking.
