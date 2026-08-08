# Technical Architecture

## 1. Stack

- **Language**: Kotlin (null-safety and idioms — see `docs/05-conventions.md`)
- **UI**: Jetpack Compose, Material 3
- **Local settings**: SharedPreferences (`data/AppPreferences`) — no Room in this MVP
- **DI**: none (thin `AndroidViewModel` + constructor prefs) — no Hilt unless explicitly requested
- **Concurrency**: Kotlin Coroutines + Flow
- **Build**: Gradle Version Catalogs (`libs.versions.toml`)
- **Tests**: JUnit 5 on `domain/` (JVM)
- **Static analysis**: ktlint + detekt (CI)
- **No backend.**

## 2. Package layout

```
app/src/main/kotlin/com/racktrack/
├── MainActivity.kt
├── appearance/            # FeltTone / FeltPalette / LocalFeltPalette (shared UI + prefs)
├── presentation/
│   ├── screen/            # Setup, MatchBoard, FourteenOneBoard, Summary, Settings, FeltBackground
│   ├── component/         # Icons, textured controls, felt noise, haptics, gear
│   ├── theme/             # Typography, action colors, RackTrackTheme
│   ├── viewmodel/         # MatchViewModel
│   └── MatchFormatOptions.kt
├── domain/
│   ├── model/             # Match, Player, GameMode, events, status
│   ├── MatchEngine.kt
│   ├── FourteenOneEngine.kt
│   └── MatchStats.kt
└── data/
    ├── AppPreferences.kt
    └── UserSettings.kt
```

### Dependency rule
`presentation` → `domain`  
`presentation` → `data` / `appearance` (thin MVP wiring)  
`data` → `appearance` (persist felt tone) — **not** → `presentation`  
`domain` **never** depends on `data`, `presentation`, `appearance`, or Android.

## 3. Why this shape

Pool scoring rules stay in pure engines so JVM unit tests stay fast. The UI is a split
scoreboard, not a deep referee — no use-case/repository scaffolding until persistence or
multi-screen history actually lands.

## 4. Data flow (example: race +1)

```
MatchBoardScreen
  → MatchViewModel.awardPlusOne(playerId)
     → MatchEngine.awardPlusOne(match, …)
  ← StateFlow<Match> updates
  ← Compose recomposes scores / break indicator
```

14/1 follows the same path via `FourteenOneEngine`.

## 5. Future evolution (informational)

```
Now : App → in-memory Match + SharedPreferences settings
Later (if needed): Room match history, repository ports in domain/, optional backend
```

Do not add Room/Hilt/network preemptively.

## 6. What agents must NOT do on their own

- Add a backend, auth, or network dependency.
- Reintroduce shot-by-shot entry UI without an explicit product request.
- Use XML layouts (Compose only).
- Make `domain/` depend on Android, Room, or Compose.
- Invent game rules not backed by the PDF / existing engines.
- Introduce a new external library without flagging it.
- Use `!!`, mutable public state, or global singletons.
