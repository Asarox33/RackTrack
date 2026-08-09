# Domain Model — Race board & 14/1

Reference for the **current** `domain/` layer. This is a race / points scoreboard, not a
shot-by-shot referee. Per-mode FFB notes and MVP coverage:
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
| `foul1` / `foul2` | Consecutive fouls this rack | Consecutive fouls until legal shot |
| `currentBreakerId` | Who breaks next rack | Opening-break indicator |
| `currentShooterId` | Synced with breaker | Player at the table |
| `currentRun` / `highRun*` | unused / n/a | Current inning / high run |
| `history` | `List<MatchEvent>` for undo + stats | same |

Status: `IN_PROGRESS` | `COMPLETED`. Winner from scores when completed.

### `MatchEvent` / `MatchEventType`
Append-only log used for undo and summaries.

Race-oriented types include: `PLUS_ONE`, `RUN_OUT`, `FOUL`, `GOLDEN_BREAK`, `DRY_BREAK`,
`THREE_FOULS_LOSS`, `EIGHT_BALL_LOSS`.

14/1 types include: `POINTS` (with `value`), `PASS`, `BREAK_FOUL`, `THREE_FOUL_PENALTY`.

## 2. Engines (pure Kotlin)

### `MatchEngine`
Race modes only. Records +1 / run-out / foul / golden / dry / early-8; advances breaker;
three consecutive fouls end the rack when the mode supports it; completes when a player
reaches `racksToWin`; supports undo.

### `FourteenOneEngine`
14/1 only. Adds points, pass, classic foul (−1), break foul (−2), three-foul extra (−15);
tracks innings / high run / opening break; optional innings overtime; completes at distance
or innings end; supports undo.

### `MatchStats`
Builds `MatchSummary` from a completed (or in-progress) match: duration, race rack lines,
or 14/1 per-inning **net** points (includes penalties).

## 3. Deliberately not modeled

- Ball positions, call ball/pocket, push-out, 10-ball respot geometry
- Camera / automated refereeing
- Persistence of match history (settings only, via SharedPreferences outside `domain/`)

For ambiguous FFB questions, prefer the PDF over inventing rules.
