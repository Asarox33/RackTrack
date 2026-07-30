# RackTrack — American pool match scorer (MVP: 10-ball, FFB rules)

Native Android app (Kotlin + Jetpack Compose) to track and score American pool matches, starting with **10-ball**, following the FFB (French Billiards Federation) sporting code for the 2026-2027 season.

## Why this project

Scoring American pool by hand (points, fouls, break, push-out) is tedious and error-prone. The goal is a fast, simple app to use club-side or in casual play, that correctly encodes the official FFB rules to avoid common refereeing mistakes (call shots, three consecutive fouls, early 10-ball pocketed, etc.).

## Project status

🚧 Framing phase — no code written yet. See [docs/06-roadmap-todo.md](docs/06-roadmap-todo.md) for actual progress.

## MVP scope (v1)

- **One game mode only: 10-ball**, FFB rules (see [docs/02-game-rules-10-ball.md](docs/02-game-rules-10-ball.md))
- Local scoring only — no user account, no cloud, no networking
- Android only (Kotlin + Jetpack Compose)
- Simple local persistence (Room), match history

Out of scope for v1: 8-ball, 9-ball, 14.1 continuous, blackball, snooker, user accounts, sync, iOS.
Full scope rationale in [docs/01-product-specification.md](docs/01-product-specification.md).

## Project documentation

| Document | Content |
|---|---|
| [docs/01-product-specification.md](docs/01-product-specification.md) | Product goal, target users, MVP vs V2+ scope |
| [docs/02-game-rules-10-ball.md](docs/02-game-rules-10-ball.md) | 10-ball FFB rules, rewritten for modeling purposes (not the official legal text — see `resources/`) |
| [docs/03-domain-model.md](docs/03-domain-model.md) | Domain entities, rack state machine, encoded foul rules |
| [docs/04-architecture.md](docs/04-architecture.md) | Tech stack, Clean Architecture, folder structure |
| [docs/05-conventions.md](docs/05-conventions.md) | Code conventions, tooling, and modern best practices — enforced, not optional |
| [docs/06-roadmap-todo.md](docs/06-roadmap-todo.md) | MVP backlog, broken down into tasks |
| `resources/code-sportif-americain-2026-2027.pdf` | Official FFB text (source of truth in case of doubt) |

## Using this repo with Cursor

1. Open the repo in Cursor.
2. `.cursor/rules.md` is automatically read by Cursor and defines the code generation rules (architecture, conventions, allowed scope). **Do not delete or bypass it.**
3. Always reference `docs/03-domain-model.md` before generating anything in the `domain` layer.
4. When in doubt about a game rule, the source of truth is `resources/code-sportif-americain-2026-2027.pdf`, not the model's memory.

## Tech stack (summary)

- Kotlin, Jetpack Compose, Material 3
- Clean Architecture (domain / data / presentation)
- Room (local persistence)
- No backend in v1
- Enforced tooling: ktlint, detekt, GitHub Actions CI (see [docs/05-conventions.md](docs/05-conventions.md))

Full details: [docs/04-architecture.md](docs/04-architecture.md)

## License

To be defined by the project owner.
