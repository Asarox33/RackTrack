# Technical Architecture

## 1. Stack

- **Language**: Kotlin (null-safety and idioms — see `docs/05-conventions.md`)
- **Modules**: `:app` (Android UI / data / monetization) + `:shared` (KMP `commonMain`
  domain + i18n catalogs en/fr/de/es/it/nl/pt; targets **android** + **jvm**)
- **UI**: Jetpack Compose, Material 3 (Android `:app` only for now — no CMP UI yet)
- **Local settings**: SharedPreferences (`data/AppPreferences`)
- **Match history**: JSON file under app files (`data/JsonMatchHistoryStore`) — no Room/KSP
- **Share**: styled PDF via `PdfDocument` + `FileProvider` (`presentation/share/`)
- **DI**: none (thin `AndroidViewModel` + constructor stores) — no Hilt unless explicitly requested
- **Concurrency**: Kotlin Coroutines + Flow
- **Build**: Gradle Version Catalogs (`libs.versions.toml`); AGP 9
  `com.android.kotlin.multiplatform.library` on `:shared`
- **Tests**: JUnit 5 on `:shared` JVM (`jvmTest`) + pure `data/` helpers in `:app`
- **Static analysis**: ktlint + detekt (CI)
- **No backend.**

## 2. Package layout

```
shared/src/commonMain/kotlin/com/racktrack/
├── domain/
│   ├── model/             # Match, Player, GameMode, events, status, PushOutPhase
│   ├── MatchEngine.kt
│   ├── PushOutEngine.kt
│   ├── FourteenOneEngine.kt
│   ├── MatchStats.kt      # MatchSummary DTO + summarize()
│   └── MatchSummaryReport.kt  # shared labels / filename stem for PDF (uses StringProvider)
└── i18n/                  # StringKey + AppLanguage + En/Fr/De/Es/It/Nl/Pt + LocaleCatalogs

app/src/main/kotlin/com/racktrack/
├── MainActivity.kt            # resolves AppLanguage / system → Strings.provider
├── appearance/            # FeltTone / FeltPalette (UI + prefs)
├── presentation/
│   ├── i18n/              # LocalStrings CompositionLocal
│   ├── screen/            # Setup, boards, Summary, History, Settings, About
│   ├── share/             # MatchSummaryPdfWriter + MatchSummaryShare
│   ├── component/         # BoardMetrics, SwipeIntPicker, SwipeLanguagePicker, icons, …
│   ├── theme/             # Typography, AppThemeMode, RackTrackTheme
│   ├── viewmodel/         # MatchViewModel + MatchCoordinator
│   └── MatchFormatOptions.kt
├── monetization/          # AdMob / Billing / UMP (Android-only)
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
`:app` → `:shared`  
`presentation` → `domain` (via shared)  
`presentation` → `data` / `appearance` (thin wiring)  
`data` → `appearance` (persist felt tone) + `domain` (MatchSummary) — **not** → `presentation`  
`domain` / `i18n` in `:shared` **never** depend on `data`, `presentation`, `appearance`, or Android APIs.

## 3. Why this shape

Pool scoring rules stay in pure engines so JVM unit tests stay fast (`:shared:jvmTest`).
The UI is a split scoreboard on Android. Completed matches are snapshotted as
`MatchSummary` for history replay and PDF share. Locale catalogs live in `:shared`
(`LocaleCatalogs` + `AppLanguage` + `Strings`); Compose reads them via `LocalStrings` /
`Strings.get`. Settings can override the system language. Compose Multiplatform UI /
iOS targets are train **3.0.0** (not on Android 2.x path).

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
