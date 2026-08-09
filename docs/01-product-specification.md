# Product Specification — RackTrack

## 1. Project name

RackTrack

## 2. Goal

Provide a simple, reliable Android app to follow American pool **races (8 / 9 / 10)** and
**14/1 continuous** at the table without interrupting play. Deep FFB shot-by-shot refereeing
is deferred (possible later with camera/AI).

## 3. Target users

Amateur or club players who want a fast scoreboard next to the table (phone/tablet, any orientation).

## 4. Problem solved

Typing every shot (ball + pocket) breaks game flow. Players need fast taps for rack wins / fouls /
14/1 points, a clear **break / hand indicator**, and both scores visible at a glance.

## 5. Game modes

### Current MVP
- **8-ball race** — +1, Run out, Foul, Early 8, Dry break
- **9-ball race** — +1, Run out, Foul, Golden break, Dry break; 3 consecutive fouls = rack loss
- **10-ball race** — +1, Run out, Foul; 3 consecutive fouls = rack loss
- **14/1 continuous** — +1/+5/+14, PASS, FOUL (−1), BREAK −2, 3-foul −15; distance + optional innings

### Later
- Deeper FFB shot rules (call ball/pocket, push-out, respot), optional camera/AI assistance

Mode-by-mode FFB summary vs what the MVP encodes: `docs/02-game-rules-*.md`.

## 6. MVP features

- Create a match (2 names, mode, race length or 14/1 distance/innings, who starts)
- Split-screen board on billiard cloth (felt tones: Forest, Blue, Burgundy, Charcoal, Pink)
- Landscape and portrait (`fullSensor`)
- Break / shooter indicator; alternate break after each race rack
- Undo last action; end-of-match summary
- Settings: felt, keep screen on, haptics, default race/distance/innings, open FFB PDF rules link

## 7. Explicitly out of scope for now

- Per-shot call ball + pocket entry
- Push-out UI, full illegal-break geometry checks
- User accounts, cloud, sync, iOS
- Shot clock / disciplinary / federal competition machinery
- Match history persistence (Room) — not required for table play

## 8. Constraints

- Owner is learning native Android — keep iterations short and testable
- Domain logic stays pure Kotlin (no Android deps) for fast JVM tests

## 9. Success criteria

Standing at a table, two players can run an 8/9/10 race or a 14/1 to a set distance with the
relevant taps, see who breaks / has the hand, finish, and review a short summary — without
entering shot details.
