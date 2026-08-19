# Domain Model — Race board & 14/1

Reference for the **current** `domain/` layer. This is a race / points scoreboard, not a
shot-by-shot referee. Per-mode FFB notes and v1 coverage:
`docs/02-game-rules-8-ball.md`, `9-ball`, `10-ball`, `14-1`.

Authority for mode-specific rules: `resources/code-sportif-americain-2026-2027.pdf`.

## 1. Core entities

### `Player` / `PlayerId`
```kotlin
data class Player(val id: PlayerId, val name: String)
```

### `GameMode`
`EIGHT_BALL` | `NINE_BALL` | `TEN_BALL` | `FOURTEEN_ONE`

Flags derived from the mode: `isPointScoring`, `supportsGoldenBreak`, `supportsDryBreak`,
`supportsThreeFoulRackLoss`, `supportsEightBallLoss`.

### `Match`
In-memory match for races or 14/1.

| Field | Race meaning | 14/1 meaning |
|---|---|---|
| `score1` / `score2` | Racks won | Points (may be negative) |
| `racksToWin` | Race length | unused (0) |
| `pointsToWin` | unused | Distance |
| `inningsLimitBase` / `inningsLimit` | unused | Optional innings cap (+ overtime) |
| `breakRule` | `ALTERNATE` or `WINNER` | ignored |
| `openingBreakerId` | First breaker (undo rebuild) | same |
| `foul1` / `foul2` | Consecutive fouls this rack | Consecutive fouls until legal shot |
| `currentBreakerId` | Who breaks next rack | Opening-break indicator |
| `currentShooterId` | Synced with breaker | Player at the table |
| `currentRun` / `highRun*` | unused / n/a | Current inning / high run |
| `objectBallsOnTable` | unused | Object balls left (1–15; continuous re-rack) |
| `solo` | must be false | 14/1 solo training (placeholder `player2`, no handoff) |
| `history` | `List<MatchEvent>` for undo + stats | same |

Status: `IN_PROGRESS` | `COMPLETED`. Winner from scores when completed (solo → always player1).

### `MatchEvent` / `MatchEventType`
Append-only log used for undo and summaries.

Race-oriented types include: `PLUS_ONE`, `RUN_OUT`, `FOUL`, `FOULS_CLEARED`, `GOLDEN_BREAK`,
`DRY_BREAK`, `THREE_FOULS_LOSS`, `EIGHT_BALL_LOSS`.

14/1 types include: `POINTS` (with `value`), `PASS`, `BREAK_FOUL`, `THREE_FOUL_PENALTY`.

## 2. Engines (pure Kotlin)

### `MatchEngine`
Race modes only. Records +1 / run-out / foul / golden / dry / early-8; advances breaker
per `BreakRule` (`ALTERNATE` flips breaker, `WINNER` gives next break to the rack winner);
undo rebuilds breaker from `openingBreakerId` + history.
three consecutive fouls end the rack when the mode supports it; completes when a player
reaches `racksToWin`; supports undo.

### `FourteenOneEngine`
14/1 only. Adds points (and decrements `objectBallsOnTable` with continuous re-rack), pass,
classic foul (−1), break foul (−2), three-foul extra (−15, resets table to 15);
tracks innings / high run / opening break; optional innings overtime; completes at distance
or innings end; supports undo. When `match.solo`, hand never switches and innings-limit /
distance use player1 only.

### `MatchStats` / `MatchSummary`
Builds `MatchSummary` from a completed (or in-progress) match: scores, counters, racks or
14/1 reprises, `startedAtMillis` / `endedAtMillis` / `totalDurationMillis`, `solo`.

### `MatchSummaryReport`
Shared labels, duration formatting, and PDF filename stem
(`racktrack_<mode>_<p1>_vs_<p2>_<yyyyMMdd_HHmm>` from match start; solo uses `_solo_` instead of `_vs_`).

## 3. Persistence (outside `domain/`)

- Settings: SharedPreferences (`data/AppPreferences`)
- Completed matches: JSON file (`data/JsonMatchHistoryStore`) storing `MatchSummary` snapshots
  so history can reopen the same stats as the end-of-match modal; delete supported
- Filters: `MatchHistoryFilter` (game mode + order-free player name queries)

## 4. Deliberately not modeled

- Ball positions, call ball/pocket, 10-ball respot geometry, ball-in-hand state machine
- Camera / automated refereeing
- Deep FFB shot-by-shot referee UI (needs camera to be useful)

For ambiguous FFB questions, prefer the PDF over inventing rules.
