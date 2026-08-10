# 9-Ball Game Rules (FFB, 2026-2027) — Modeling + v1 coverage

> Rewritten technical specification, not the official regulatory text. Authority:
> `resources/code-sportif-americain-2026-2027.pdf` (Chapter 4, arts 1.4.01–1.4.07;
> general Chapter 2, arts 1.2.01–1.2.20).
>
> Domain: `MatchEngine` + `GameMode.NINE_BALL`. Product scope: `docs/01-product-specification.md`.

## RackTrack v1 — what is implemented

Race scoreboard. Richest race mode in the app (golden + dry + three-foul loss).

| FFB topic | v1 |
|---|---|
| Race-to-N racks; +1 / Run out | **Yes** — `PLUS_ONE` / `RUN_OUT` |
| Alternating / winner break | **Yes** — setup `BreakRule` |
| Golden break — legal break pockets 9 (1.4.03) | **Yes** — **GOLDEN** → awards rack |
| Dry break (no object ball pocketed) as stat | **Yes** — **DRY**; blocks Run out / Golden this rack |
| Illegal break / “three-ball” soft-break rule | **No** — operator; may use FOUL or DRY by judgment |
| Push-out after legal break | **No** |
| 9 respotted when pocketed/ejected irregularly (1.4.06) | **No** — operator |
| 3 consecutive fouls → lose rack (1.4.07) | **Yes** — `THREE_FOULS_LOSS` |
| Warning after 2 fouls | **Yes** — on-screen banner |
| Legal shot resets consecutive fouls | **Partial** — tap foul chip (`FOULS_CLEARED`) |
| Ball order / combo legality / BIH | **No** |
| Undo; summary (incl. golden / dry counts) | **Yes** |

Unlike 10-ball, FFB 9-ball is generally **without call**, except special cases (art. 1.4.01).

---

## 1. Objective

- Nine numbered balls (1–9) + cue ball.
- Contact in ascending order (combinations/caroms OK if lowest ball is contacted first).
- Rack won when the **9** is legally pocketed (including on a legal break — golden break).
- Match = several racks; race length set before the match.

## 2. Rack setup

- Diamond rack: 1 on the foot spot (apex), 9 in the center; other balls placed freely (vary each rack).

## 3. Break

- Breaker: ball-in-hand behind the head string.
- Must contact the **1** first, else foul.
- If 1 contacted and nothing pocketed: at least **four** object balls must hit a rail, else foul.
- Foul on break → opponent ball-in-hand anywhere.
- **Golden break:** if the break is **legal** and the **9** is pocketed → breaker **wins the rack** (art. 1.4.03).
- Soft / “three-ball” illegal-break rule (Magic Ball Rack / cloth marking contexts): see PDF; U17 and classic triangle exemptions. On illegal break with 9 down → 9 respotted; opponent may accept (no push-out) or return the shot (push-out allowed).

## 4. Push-out

- After a **legal** break, the player with the shot may announce a push-out.
- Suspends lowest-ball-first and rail-after-contact for that shot only.
- 9 pocketed on push-out → respotted; other balls stay.
- No foul → opponent chooses to take the table or pass it back; foul → opponent BIH anywhere.

## 5. Normal rack flow

- Generally no call (unlike 10-ball).
- Must contact lowest ball first; continue after a legal pocket.
- Safety / miss → hand to opponent under general rules (Chapter 2).

## 6. Special case: the 9-ball

- If the 9 is ejected or pocketed **irregularly** (with a foul) → respot on the foot spot (or behind if occupied); other balls stay; play continues under applicable rules (art. 1.4.06).
- Early 9 on a **legal** combination/carom that contacted the lowest ball first can win the rack (standard rotation).

## 7. Fouls

- Foul → opponent ball-in-hand **anywhere** (break exceptions above).
- List: art. 1.2.09.

## 8. Three consecutive fouls rule

- Three consecutive fouls without a legal shot in between → **lose the rack**.
- After the 2nd consecutive foul, opponent/referee **must** clearly warn the player; without warning, a further foul does not count as the third (art. 1.4.07).
- A legal shot resets the counter.

**v1:** banner + auto loss on 3rd FOUL; manual clear via foul chip. The “unwarned 3rd doesn’t count” exception is **not** modeled.

## 9. End of rack / end of match

- Rack ends on legal 9 (incl. golden), or three-foul loss.
- Match ends at race length.

## 10. Deliberately not modeled (deep mode / later)

- Push-out UI and illegal-break choice tree
- Three-ball soft-break automation
- Respot geometry, BIH state, shot clock, disciplinary / federal machinery
