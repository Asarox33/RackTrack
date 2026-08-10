# RackTrack — American pool match scorer

Native Android app (Kotlin + Jetpack Compose) for scoring **8-ball / 9-ball / 10-ball races**
and **14/1 continuous** at the table, without slowing the game down.

## Project status

Ready for table use — race + 14/1 boards, settings, history, PDF share.

## Scope (current)

- Landscape **and** portrait (`fullSensor`), split-screen boards on billiard cloth
- Setup: game mode, race length or 14/1 distance (+ optional innings), who starts
- Race actions: **+1**, **Run out**, **Foul**; 9-ball Golden/Dry; 8-ball Early 8/Dry
- 14/1: **+1 / +5 / +14**, **PASS**, **FOUL (−1)**, **BREAK −2**, 3-foul **−15**
- Break / hand indicator, undo, end-of-match summary (**SHARE PDF** + **BACK**)
- Match history (per selected mode): player filters, reopen stats, delete with confirm
- Styled PDF share (start / end / duration; filename includes start date-time)
- Settings: felt color, keep screen on, haptics, default race/distance/innings, FFB rules link
- Local only — no accounts, no cloud, no per-shot ball/pocket entry

Out of scope: shot-by-shot call/pocket scoring, camera/AI, iOS, sync, PNG share.
FFB mode notes live in `docs/02-game-rules-*.md`; official text in the PDF under `resources/`.

## Run / build

```bash
./gradlew testDebugUnitTest
./gradlew :app:domainCoverage   # domain JaCoCo → app/build/reports/jacoco/domainCoverage/
./gradlew ktlintCheck detekt
./gradlew :app:assembleDebug
```

On pull requests to `main`, CI posts a **Domain coverage** comment (JaCoCo) and uploads the HTML report as an artifact.

Open in Android Studio (recent) or install the debug APK on a phone/tablet.

## Docs

| Document | Content |
|---|---|
| [docs/01-product-specification.md](docs/01-product-specification.md) | Product goal / scope |
| [docs/02-game-rules-8-ball.md](docs/02-game-rules-8-ball.md) | FFB 8-ball + app coverage |
| [docs/02-game-rules-9-ball.md](docs/02-game-rules-9-ball.md) | FFB 9-ball + app coverage |
| [docs/02-game-rules-10-ball.md](docs/02-game-rules-10-ball.md) | FFB 10-ball + app coverage |
| [docs/02-game-rules-14-1.md](docs/02-game-rules-14-1.md) | FFB 14/1 + app coverage |
| [docs/03-domain-model.md](docs/03-domain-model.md) | Domain model (race + 14/1) |
| [docs/04-architecture.md](docs/04-architecture.md) | Stack & layers |
| [docs/05-conventions.md](docs/05-conventions.md) | Conventions |
| [docs/06-roadmap-todo.md](docs/06-roadmap-todo.md) | Roadmap / backlog |
| `resources/code-sportif-americain-2026-2027.pdf` | Official FFB text |

## Tech stack

- Kotlin 2.4, Jetpack Compose, Material 3
- Gradle Version Catalog, ktlint, detekt
- JUnit 5 domain / data unit tests
- Local JSON match history + SharedPreferences settings
