# Technical Architecture

## 1. Stack

- **Language**: Kotlin (null-safety and idioms — see `docs/05-conventions.md`)
- **UI**: Jetpack Compose, Material 3
- **Local settings**: SharedPreferences (`data/AppPreferences`)
- **Match history**: JSON file under app files (`data/JsonMatchHistoryStore`) — no Room/KSP
- **Share**: styled PDF via `PdfDocument` + `FileProvider` (`presentation/share/`)
- **DI**: none (thin `AndroidViewModel` + constructor stores) — no Hilt unless explicitly requested
- **Concurrency**: Kotlin Coroutines + Flow
- **Build**: Gradle Version Catalogs (`libs.versions.toml`)
- **Tests**: JUnit 5 on `domain/` + pure `data/` helpers (JVM)
- **Static analysis**: ktlint + detekt (CI)
- **No backend.**

## 2. Package layout

```
app/src/main/kotlin/com/racktrack/
├── MainActivity.kt
├── appearance/            # FeltTone / FeltPalette / LocalFeltPalette (shared UI + prefs)
├── presentation/
│   ├── screen/            # Setup, boards, Summary, History, Settings, FeltBackground
│   ├── share/             # MatchSummaryPdfWriter + MatchSummaryShare
│   ├── component/         # Icons, textured controls, felt noise, haptics, gear
│   ├── theme/             # Typography, action colors, RackTrackTheme
│   ├── viewmodel/         # MatchViewModel + MatchCoordinator
│   └── MatchFormatOptions.kt
├── domain/
│   ├── model/             # Match, Player, GameMode, events, status
│   ├── MatchEngine.kt
│   ├── FourteenOneEngine.kt
│   ├── MatchStats.kt      # MatchSummary DTO + summarize()
│   └── MatchSummaryReport.kt  # shared labels / filename stem for PDF
└── data/
    ├── AppPreferences.kt
    ├── UserSettings.kt
    ├── MatchHistoryStore.kt
    ├── JsonMatchHistoryStore.kt
    ├── InMemoryMatchHistoryStore.kt
    ├── MatchHistoryFilter.kt
    ├── MatchSummaryJson.kt
    └── StoredMatch.kt
```

### Dependency rule
`presentation` → `domain`  
`presentation` → `data` / `appearance` (thin wiring)  
`data` → `appearance` (persist felt tone) + `domain` (MatchSummary) — **not** → `presentation`  
`domain` **never** depends on `data`, `presentation`, `appearance`, or Android.

## 3. Why this shape

Pool scoring rules stay in pure engines so JVM unit tests stay fast. The UI is a split
scoreboard. Completed matches are snapshotted as `MatchSummary` for history replay and PDF share.

## 4. Data flow (example: race +1)

```
MatchBoardScreen
  → MatchViewModel.plusOne(playerId)
     → MatchEngine.recordPlusOne(match, …)
  ← StateFlow<Match> updates
  ← Compose recomposes scores / break indicator
  → on COMPLETED: persist MatchStats.summarize(match)
```

14/1 follows the same path via `FourteenOneEngine`. History list filters by current setup
`GameMode` + optional player-name queries (`MatchHistoryFilter`).

## 5. What agents must NOT do on their own

- Add a backend, auth, or network dependency.
- Reintroduce shot-by-shot / deep FFB referee UI without an explicit product request (+ camera).
- Use XML layouts (Compose only).
- Make `domain/` depend on Android, Room, or Compose.
- Invent game rules not backed by the PDF / existing engines.
- Introduce a new external library without flagging it.
- Use `!!`, mutable public state, or global singletons.
