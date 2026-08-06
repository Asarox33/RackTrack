# Product Specification — RackTrack v0.1

## 1. Project name

RackTrack

## 2. Goal

Provide a simple, reliable Android app to follow American pool **10-ball races** at the table
without interrupting play. Deep FFB shot-by-shot refereeing is deferred (possible later with camera/AI).

## 3. Target users

Amateur or club players who want a fast race scoreboard next to the table (landscape phone/tablet).

## 4. Problem solved

Typing every shot (ball + pocket) breaks game flow. Players need **+1 / run-out / foul** taps,
a clear **break indicator**, and both scores visible at a glance.

## 5. Game modes

### V1 (MVP)
- **10-ball race board** (race to N racks), landscape split screen

### V2+
- Deeper FFB shot rules, 9-ball / 8-ball, optional camera/AI assistance

## 6. MVP features

- Create a match (2 player names, race length, who breaks first)
- Landscape split screen on blue billiard cloth
- Per player: **+1**, **Run out**, **Foul**
- Break indicator; alternate break after each rack win
- Undo last action; match completes at race target
- Designed for Samsung S22 FHD+ (2340×1080 landscape) and similar

## 7. Explicitly out of scope for v1

- Per-shot call ball + pocket entry
- Full consecutive-fouls auto rack loss, push-out UI
- User accounts, cloud, sync, iOS
- Shot clock / disciplinary / federal competition machinery

## 8. Constraints

- Owner is learning native Android — keep iterations short and testable
- Domain logic stays pure Kotlin (no Android deps) for fast JVM tests

## 9. MVP success criteria

Standing at a table in landscape, two players can run a race to 6 with +1 / Run out / Foul,
see who breaks, and finish — without entering shot details.
