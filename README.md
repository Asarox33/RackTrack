# RackTrack — American pool match scorer

Native Android app (Kotlin + Jetpack Compose) for scoring **8-ball / 9-ball / 10-ball races**
and **14/1 continuous** at the table, without slowing the game down.

## Project status

MVP ready for table testing — race + 14/1 boards, settings, end-of-match summary.

## Scope (current)

- Landscape **and** portrait (`fullSensor`), split-screen boards on billiard cloth
- Setup: game mode, race length or 14/1 distance (+ optional innings), who starts
- Race actions: **+1**, **Run out**, **Foul**; 9-ball Golden/Dry; 8-ball Early 8/Dry
- 14/1: **+1 / +5 / +14**, **PASS**, **FOUL (−1)**, **BREAK −2**, 3-foul **−15**
- Break / hand indicator, undo, match summary (racks or 14/1 innings with net points)
- Settings: felt color, keep screen on, haptics, default race/distance/innings, FFB rules link
- Local only — no accounts, no cloud, no per-shot ball/pocket entry

Out of scope for now: shot-by-shot call/pocket scoring, camera/AI, iOS, sync.
Detailed FFB rules live in `docs/02-game-rules-10-ball.md` and the official PDF (future deep mode).

## Run / build

```bash
./gradlew testDebugUnitTest
./gradlew ktlintCheck detekt
./gradlew :app:assembleDebug
```

Open in Android Studio (recent) or install the debug APK on a phone/tablet.

## Docs

| Document | Content |
|---|---|
| [docs/01-product-specification.md](docs/01-product-specification.md) | Product goal / scope |
| [docs/02-game-rules-10-ball.md](docs/02-game-rules-10-ball.md) | FFB 10-ball rules (deep mode reference) |
| [docs/03-domain-model.md](docs/03-domain-model.md) | Domain model (race + 14/1) |
| [docs/04-architecture.md](docs/04-architecture.md) | Stack & layers |
| [docs/05-conventions.md](docs/05-conventions.md) | Conventions |
| [docs/06-roadmap-todo.md](docs/06-roadmap-todo.md) | Backlog |
| `resources/code-sportif-americain-2026-2027.pdf` | Official FFB text |

## Tech stack

- Kotlin 2.4, Jetpack Compose, Material 3
- AGP 9.3 (built-in Kotlin), Gradle 9.6, compileSdk/targetSdk 36, minSdk 26
- JDK 25 for builds; pure `domain/` with JVM unit tests
- SharedPreferences for user settings (no Room / Hilt in this MVP)
- ktlint + detekt + GitHub Actions CI

## Assets

Launcher source + build script: `assets/build_launcher_icon.py`, `assets/racktrack-icon-c-rack.png`.
Draft PNGs under `assets/` are gitignored.
