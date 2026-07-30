# Technical Architecture

## 1. Stack

- **Language**: Kotlin (latest stable, null-safety and idioms enforced — see `docs/05-conventions.md`)
- **UI**: Jetpack Compose, Material 3
- **Local persistence**: Room
- **Dependency injection**: Hilt (decided — no manual DI, to avoid boilerplate/service-locator anti-patterns as the codebase grows)
- **Concurrency**: Kotlin Coroutines + Flow (no callbacks, no RxJava)
- **Build system**: Gradle with **Version Catalogs** (`libs.versions.toml`) — no hardcoded dependency versions scattered across build files
- **Tests**: JUnit5 + Turbine (for Flow testing), MockK if mocking is needed in `data`/`presentation` (never in `domain`)
- **Static analysis**: ktlint (formatting) + detekt (static analysis) — enforced in CI, not optional (see `docs/05-conventions.md`)
- **No backend in v1.** Architecture is ready to add one later (see §5).

## 2. Clean Architecture — 3 layers

```
app/
├── presentation/
│   ├── screen/            # one folder per screen (HomeScreen, NewMatchScreen, RackScreen, HistoryScreen...)
│   ├── component/         # reusable Compose components (Scoreboard, BallTracker, FoulBanner...)
│   └── viewmodel/          # one ViewModel per screen, consumes use cases via StateFlow
│
├── domain/
│   ├── model/               # Match, Rack, Player, Shot, FoulType, ShotOutcome... (see docs/03-domain-model.md)
│   ├── usecase/             # StartMatchUseCase, RecordShotUseCase, etc.
│   ├── repository/          # repository *interfaces* only (implementations live in data/)
│   └── rule/                 # the 10-ball rules engine (pure Kotlin, zero Android dependency)
│
└── data/
    ├── local/
    │   ├── database/         # Room: entities, DAO, Database
    │   └── mapper/           # mapping between Room entities and domain models
    └── repository/            # repository implementations (implements domain/repository interfaces)
```

### Strict dependency rule
`presentation` → `domain` ← `data`

`domain` **never** depends on `data`, `presentation`, or Android (`android.*`, `androidx.*`). This is what makes the 10-ball rules engine unit-testable in pure Kotlin, with no emulator required and sub-second test runs.

## 3. Why Clean Architecture here (not just plain MVVM)

Pool has a lot of non-trivial business rules (break, push-out, ball order, consecutive fouls). Isolating this rules engine in `domain/rule`, with no UI or database dependency, allows:
- Exhaustive unit testing (foul cases, sequencing) without any Android dependency;
- Reuse as-is if a 9-ball or 8-ball mode is added in V2, by factoring out the rules shared across American pool variants (see `docs/02-game-rules-10-ball.md` — most of article 1.2.09's fouls are common across modes).

## 4. Data flow (example: recording a shot)

```
RackScreen (Compose)
   → RackViewModel.onShotRecorded(input)
      → RecordShotUseCase(rackId, shotInput)
         → domain/rule: computes the ShotOutcome from current Rack state
         → RackRepository.save(updatedRack)  // implemented in data/, persisted via Room
      ← new Rack state (StateFlow)
   ← RackScreen recomposes with the updated score / state
```

## 5. Future evolution (out of v1, informational only)

```
V1 :  Android App → Room → phone (no network at all)
V2+ : Android App → Repository → API (Ktor or Supabase) → Backend → Database
```
Do not build this layer preemptively in v1 — the repository interfaces already defined in `domain/` are enough to make this change possible later without rewriting the domain layer.

## 6. UI: dynamic components (lightweight SDUI — optional, not a priority)

If considered later: only drive the layout/ordering of existing components (e.g. block order in `RackScreen`) via a simple JSON config, never full app generation from a server. **Do not implement in v1** — noted here for future reference only; Cursor should not build this unless explicitly requested.

## 7. What Cursor must NOT do on its own initiative

- Add a backend, authentication, or a network dependency.
- Add any game mode other than 10-ball.
- Use XML layouts (Compose only).
- Make `domain/` depend on Android, Room, or Compose directly.
- Invent game rules not present in `docs/02-game-rules-10-ball.md` or `docs/03-domain-model.md`.
- Introduce a new external library not already listed in this document without flagging it explicitly.
- Use `!!` (non-null assertion), mutable public state, or global singletons outside of Hilt-managed scopes — see `docs/05-conventions.md` for the full list of enforced modern practices.
