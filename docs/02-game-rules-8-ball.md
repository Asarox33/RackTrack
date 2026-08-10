# 8-Ball Game Rules (FFB, 2026-2027) — Modeling + MVP status

> Rewritten technical specification, not the official regulatory text. Authority:
> `resources/code-sportif-americain-2026-2027.pdf` (Chapter 3, arts 1.3.01–1.3.06;
> general Chapter 2, arts 1.2.01–1.2.20).
>
> Domain: `MatchEngine` + `GameMode.EIGHT_BALL`. Product scope: `docs/01-product-specification.md`.

## RackTrack MVP — what is implemented

Race scoreboard only. No solids/stripes table state; players judge groups and calls.

| FFB topic | MVP |
|---|---|
| Race-to-N racks; +1 / Run out award rack | **Yes** — `PLUS_ONE` / `RUN_OUT` |
| Alternating / winner break | **Yes** — setup `BreakRule` |
| Early-8 / lose rack cases (1.3.06) | **Yes** — single **EARLY 8** → opponent wins rack (`EIGHT_BALL_LOSS`) |
| Dry / empty break as stat (1.3.03) | **Yes** — **DRY** on breaker; does not end rack; blocks Run out this rack |
| Illegal-break opponent choices (accept / re-rack / force re-break) | **No** — operator only |
| 8 pocketed on break → respot or re-rack choices | **No** — not a button (not a golden win) |
| Foul as ball-in-hand | **Partial** — **FOUL** increments a counter only; **no** 3-foul rack loss |
| Call every ball; open table / group choice | **No** |
| Undo; match summary (incl. Early 8 count) | **Yes** |

---

## 1. Objective

- Fifteen numbered balls + cue ball.
- Each player has a group of seven (solids or stripes).
- Win the rack by pocketing all of one’s group, then the **8-ball** legally in the called pocket.
- Match = several racks; race length set before the match.
- Every object ball (after the break) must be **called**.

## 2. Rack setup

- Standard triangle: 8-ball in the center; apex ball on the foot spot (any ball); one solid and one stripe in the rear corners (typical FFB diagram — see PDF).

## 3. Break

- Breaker: ball-in-hand behind the head string. **No call** on the break; cue may contact any ball in the rack.
- If a ball is pocketed legally → breaker continues.
- If no ball is pocketed: at least **four** object balls must hit a rail, else the break is **illegal**.
- Illegal break — opponent chooses: accept table; or re-rack and break themselves; or re-rack and make the original breaker re-break.
- **8 on the break** (no cue-ball foul): not a loss — breaker may respot the 8 and continue, or re-rack and re-break. Other pocketed balls stay down.
- **8 on the break + cue foul**: opponent chooses respot-8 + BIH behind the line, or re-rack and re-break.
- Object balls off the table on the break → foul.

## 4. Open table / groups

- Table is open until a player legally pockets a called group ball (details in art. 1.3.04).
- After groups are set, each player may only shoot their group, then the 8.

## 5. Normal rack flow

- Call ball + pocket for each shot.
- Legally pocket called ball → continue; miss / fail call → hand to opponent (pocketed balls stay, subject to foul rules).
- After clearing one’s group, call the 8 and its pocket.

## 6. Loss of rack (art. 1.3.06)

The shooter **loses the rack** if:
- The 8 is pocketed before that player’s group is cleared (**early 8**).
- The 8 leaves the table (except on the break).
- The 8 is pocketed **with a foul**.
- The 8 is pocketed in a pocket other than the one called.

**MVP:** all of these collapse into one **EARLY 8** action on the losing player (opponent scores the rack).

## 7. Fouls

- Typical foul → opponent ball-in-hand **anywhere** (except break-specific cases in 1.3.03).
- Full foul list: art. 1.2.09.
- **There is no three-consecutive-fouls rack loss** in the 8-ball chapter.

**MVP:** FOUL is a soft counter / reminder only; clearing via foul-chip tap is **disabled** for 8-ball.

## 8. End of rack / end of match

- Rack ends on legal 8 in the called pocket, or on a §6 loss.
- Match ends when a player reaches the race length.

## 9. Deliberately not modeled (deep mode / later)

- Open-table / group assignment state machine
- Illegal-break and 8-on-break choice trees
- Per-shot call entry, BIH zones, shot clock, disciplinary / federal competition machinery
