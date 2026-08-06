# RackTrack — American pool match scorer (MVP: 10-ball race board)

Native Android app (Kotlin + Jetpack Compose) to follow a **10-ball race** at the table
without slowing the game down.

## Project status

MVP v0.1 — landscape race board (split screen, +1 / Run out / Foul, break indicator).

## MVP scope (v1)

- **Landscape only** (designed for Samsung S22 FHD+ 2340×1080 and similar)
- Split screen: both players always visible on blue billiard cloth
- Per player: **+1**, **Run out**, **Foul**
- Choose race length + who breaks first
- Alternate break after each rack; undo last action
- Local only — no accounts, no cloud, no per-shot ball/pocket entry

Out of scope for v1: shot-by-shot call/pocket scoring, camera/AI, 8-ball/9-ball, iOS, sync.
Detailed FFB rules remain documented for a future deeper mode.

## Run / build

```bash
./gradlew testDebugUnitTest
./gradlew :app:assembleDebug
```

Open in Android Studio (Ladybug+ / recent) or install the debug APK on a device locked to landscape.

## Docs

| Document | Content |
|---|---|
| [docs/01-product-specification.md](docs/01-product-specification.md) | Product goal / scope |
| [docs/02-game-rules-10-ball.md](docs/02-game-rules-10-ball.md) | FFB 10-ball rules (future deep mode) |
| [docs/03-domain-model.md](docs/03-domain-model.md) | Domain notes (race MVP + future) |
| [docs/04-architecture.md](docs/04-architecture.md) | Stack & layers |
| [docs/05-conventions.md](docs/05-conventions.md) | Conventions |
| [docs/06-roadmap-todo.md](docs/06-roadmap-todo.md) | Backlog |
| `resources/code-sportif-americain-2026-2027.pdf` | Official FFB text |

## Tech stack

- Kotlin 2.4, Jetpack Compose, Material 3
- AGP 9.3 (built-in Kotlin), Gradle 9.6, compileSdk/targetSdk 36
- JDK 25 for builds; clean `domain/` with JVM unit tests
- ktlint + detekt + GitHub Actions CI
