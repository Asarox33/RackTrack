# 14/1 Continuous Game Rules (FFB, 2026-2027) — Modeling + v1 coverage

> Rewritten technical specification, not the official regulatory text. Authority:
> `resources/code-sportif-americain-2026-2027.pdf` (Chapter 6, arts 1.6.01–1.6.07;
> general Chapter 2, arts 1.2.01–1.2.20).
>
> Domain: `FourteenOneEngine` + `GameMode.FOURTEEN_ONE`. Product scope: `docs/01-product-specification.md`.

## RackTrack v1 — what is implemented

Points scoreboard with hand indicator (current shooter). Approximate object-ball count
(`objectBallsOnTable` / UI **TABLE n**) tracks continuous re-rack for the scoreboard;
geometry and call-shot are **not** enforced — players enter points in chunks.

| FFB topic | v1 |
|---|---|
| Race to distance (points to win) | **Yes** — setup `pointsToWin` |
| Optional innings (reprises) limit + +5 OT on tie (1.6.05) | **Yes** |
| Add points during a visit | **Yes** — board **+(n−1)** clear-to-re-rack from On Table n; PASS/FOUL modal syncs last partial |
| Object balls remaining (scoreboard approx.) | **Yes** — start 15; −n on points; at 1 left → re-rack to 15; PASS/FOUL modal sets **2–15**; 3-foul → 15 |
| Legal end of turn (pass hand) | **Yes** — **PASS** → balls-left stepper modal |
| Classic foul −1, hand to opponent, no BIH (1.6.07) | **Yes** — **FOUL** |
| Legal shot / pass clears consecutive fouls | **Yes** — via `addPoints` / `PASS` (no separate clear chip) |
| 3 consecutive fouls → −1 then extra **−15**, re-break (1.6.07) | **Yes** — `THREE_FOUL_PENALTY`; same player opens again |
| Illegal opening break −2 (1.6.03) | **Yes** — **BREAK −2** while `awaitingOpeningBreak` |
| Opponent may force re-break after illegal open (cumulate −2) | **Yes** — ACCEPT / RE-BREAK modal; −2 stacks on re-break |
| Classic foul on legal open = −1 (not −2) | **Yes** — use FOUL, not BREAK −2 |
| Combined illegal open + classic foul (−3) | **No** — single action only |
| Call ball + pocket; continuous re-rack with 1 left; object respot | **Partial** — table count only; no call/geometry |
| High run / current run / innings stats | **Yes** |
| Undo; summary with net per-inning points | **Yes** |

Only the **current shooter**’s actions are enabled (unlike the race board).

---

## 1. Objective

- Fifteen numbered balls + cue ball.
- Each legally pocketed ball = **1 point**; first to the agreed distance wins.
- Optionally: innings (reprises) limit — if distance not reached, higher score wins; tie → +5 innings overtime until a winner (art. 1.6.05).
- Every shot: **call** ball + pocket.
- “Continuous”: after fourteen balls are pocketed, the fifteenth stays on the table; fourteen are re-racked (no apex ball); shooter continues.

## 2. Rack setup

- Opening: fourteen-ball triangle + fifteenth placed per PDF (art. 1.6.02). Breaker has ball-in-hand behind the head string.

## 3. Opening break (art. 1.6.03)

- Legal if, after contacting the rack, the cue ball and **two** object balls each contact a rail — **or** the called ball is pocketed (then rail criteria waived).
- Illegal opening break → **−2**; opponent may accept the table or demand a re-break (penalties accumulate on repeated illegal opens).
- Classic foul on a **legal** open → **−1** and normal foul consequences.
- Illegal open **plus** classic foul → **−3** (PDF); then opponent’s accept / re-break choice.

## 4. Normal play

- Call and pocket → score +1 per ball, continue the visit.
- Miss / safety end of visit → opponent plays the table as left (no automatic BIH).
- Continuous re-rack when fourteen are down (art. 1.6.04 / 1.6.06 special cases for frozen / interfering balls — PDF).

## 5. Innings (reprises)

- An inning is one player’s turn at the table.
- Optional limit stops the match by innings if distance not hit; OT +5 on tie.

## 6. Fouls and penalties (art. 1.6.07)

- Classic foul → **−1**; opponent takes the same table (**no** BIH), except cue scratched/off table → opponent BIH behind the head string.
- Balls pocketed on the foul shot are respotted per art. 1.6.04.
- A **legal** shot resets that player’s consecutive foul count.
- **Three consecutive fouls:** −1 per foul (so −3 from the three fouls) **plus** extra **−15** (−18 total from the sequence as written in FFB). Count resets; **fifteen-ball** re-rack; the fouling player must **open-break** again.
- Unsportsmanlike: extra −15 + warning (art. 1.2.17) — not modeled.

**v1 scoring note:** each FOUL applies −1 immediately; on the 3rd consecutive FOUL the engine also applies −15 in the same sequence (`FOUL` then `THREE_FOUL_PENALTY`). Opening illegal break is a separate **BREAK −2**.

## 7. End of match

- A player reaches the distance, **or**
- Innings limit expires (with OT rules if tied).

## 8. Deliberately not modeled (deep mode / later)

- Per-ball call entry and which numbered balls are down
- Continuous re-rack geometry and special cases (1.6.06)
- BIH-behind-line after scratch (table state)
- −3 combined open+foul as one coded path
- Shot clock, disciplinary cards, federal competition machinery
