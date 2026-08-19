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
│   ├── screen/            # Setup, boards, Summary, History, SettingsScreen, FeltBackground
│   ├── share/             # MatchSummaryPdfWriter + MatchSummaryShare
│   ├── component/         # BoardMetrics, SwipeIntPicker, icons, textured controls, haptics
│   ├── theme/             # Typography, action colors, RackTrackTheme
│   ├── viewmodel/         # MatchViewModel + MatchCoordinator
│   └── MatchFormatOptions.kt
├── domain/
│   ├── model/             # Match, Player, GameMode, events, status, PushOutPhase
│   ├── MatchEngine.kt
│   ├── PushOutEngine.kt
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

## 5. Planned 1.2.0 — ads & billing

**Locked architecture:** `docs/09-monetization.md` §9.

Keep `domain/` and match engines free of AdMob / Billing / UMP.

| Seam | Responsibility |
|------|----------------|
| `monetization/RemoveAdsStore` | IAP `remove_ads`, restore, entitlement cache |
| `monetization/InterstitialAdManager` | Preload, 5‑min cooldown, show / bypass |
| `monetization/MonetizationGate` | Premium? → skip ads; else maybe interstitial; then run start callback |
| Setup start buttons | `gate.runAfterAdOpportunity { viewModel.start… }` only |

Premium owned ⇒ **no** interstitial load or show; Start → board immediately.

## 6. What agents must NOT do on their own

- Add a backend, auth, or **general** network/API layer (AdMob / Play Billing / UMP only
  when implementing `docs/09-monetization.md` for **1.2.0**).
- Reintroduce shot-by-shot / deep FFB referee UI without an explicit product request (+ camera).
- Use XML layouts (Compose only).
- Make `domain/` depend on Android, Room, or Compose.
- Invent game rules not backed by the PDF / existing engines.
- Introduce a new external library without flagging it (monetization SDKs are expected for 1.2.0).
- Use `!!`, mutable public state, or global singletons.
- Add banner ads, mid-board ads, or subscriptions contrary to `docs/09-monetization.md`.
