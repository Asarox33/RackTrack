# 10-Ball Game Rules (FFB, 2026-2027) — Modeling + v1 coverage

> Rewritten technical specification, not the official regulatory text. Authority:
> `resources/code-sportif-americain-2026-2027.pdf` (Chapter 5, arts 1.5.01–1.5.07;
> general Chapter 2, arts 1.2.01–1.2.20).
>
> Domain: `MatchEngine` + `GameMode.TEN_BALL`. Product scope: `docs/01-product-specification.md`.

## RackTrack v1 — what is implemented

The shipped app is a **race scoreboard**, not a shot-by-shot referee. Players (or a
table partner) judge legality; the app records outcomes.

| FFB topic | v1 |
|---|---|
| Race-to-N racks; +1 / Run out award rack | **Yes** — `PLUS_ONE` / `RUN_OUT` |
| Alternating / winner break | **Yes** — setup `BreakRule`; cue follows breaker |
| Foul increments consecutive counter | **Yes** — `FOUL` |
| 3 consecutive fouls → lose rack (1.5.07) | **Yes** — `THREE_FOULS_LOSS` |
| Warning after 2 consecutive fouls | **Yes** — on-screen “1 MORE FOUL = RACK LOSS” |
| Legal shot resets consecutive fouls | **Partial** — tap foul chip (`FOULS_CLEARED`); no auto “legal shot” |
| Golden break / Dry break buttons | **No** — flags off for 10-ball |
| Push-out after legal break | **Yes** — same decision tree as 9-ball (`PUSH OUT`); summary / PDF / history count announcements |
| Call ball + pocket, 10 respot, ball-in-hand geometry, ball order | **No** — operator judgment |
| Undo last event; match summary (incl. foul / push-out counts) | **Yes** |

---

## 1. Objective

- 10 numbered balls (1 to 10) + cue ball.
- Balls must be contacted/played in ascending numerical order (a higher-numbered ball may be pocketed via carom/combination as long as the lowest-numbered ball on the table is contacted first).
- Every shot must be **called**: target ball + target pocket.
- The rack is won by legally pocketing the 10-ball **last**, in the called pocket.
- A match is played over several racks (number defined before the match).

## 2. Rack setup

- Dedicated 10-ball triangle rack: ball 1 at the apex on the foot spot, ball 10 in the center, other balls placed freely around it (different arrangement for each new rack).

## 3. Break

- The breaking player has ball-in-hand behind the head string.
- Must contact ball 1 first, otherwise → **foul**.
- If no ball is pocketed: at least 4 object balls must contact a rail, otherwise → **foul** ("illegal break").
- On a foul break: the opponent gets ball-in-hand anywhere on the table.
- *(Masters category only: "break box" rule — mandatory break from a defined zone. Out of v1 scope.)*

## 4. Push-out

- Only available after a **legal break**, for the player with the shot.
- Must be explicitly called before being played.
- During this shot, the normal "must contact the lowest ball" and "a ball must contact a rail after contact" rules are suspended.
- If the 10-ball is pocketed during the push-out → it is respotted; other pocketed balls stay pocketed.
- If the push-out is played without a foul: the **opponent** chooses whether to play the table as-is or pass the shot back to the push-out player.
- If the push-out results in a foul: the opponent gets ball-in-hand anywhere on the table.

## 5. Normal rack flow

- The player at the table must call ball + pocket on every shot.
- Must contact the lowest-numbered ball still on the table first (may pocket another ball via combination/carom as long as the call matches).
- As long as the player legally pockets the called ball, they continue their turn.
- If a non-called ball is pocketed, or the called ball goes in the wrong pocket: not a foul, but the turn passes to the opponent (pocketed balls stay pocketed, except the 10-ball — see §6).
- **Safety**: the player may call "Safety" instead of calling a pot; the turn passes to the opponent at the end of the shot. If a ball is pocketed during a called safety, the opponent chooses whether to play the table as-is or let the shooter continue.

## 6. Special case: the 10-ball

The 10-ball must only be pocketed last, legally, in the called pocket. In every other case where it is pocketed or leaves the table early:
- It is **respotted** (on the foot spot, or just behind it if occupied).
- Other balls pocketed on the same shot stay pocketed.
- Turn: if pocketed early on an otherwise **legal** called shot → shooter **continues**; if irregular / foul / unannounced → hand to opponent (art. 1.5.06).

## 7. Fouls (applicable to 10-ball)

A foul results in: **ball-in-hand for the opponent, anywhere on the table** (except break-specific cases already covered above).

Main fouls to model (see article 1.2.09 of the sporting code, full list in the PDF):
- Cue ball pocketed or knocked off the table
- Wrong ball contacted first (ascending order violation)
- No rail contacted after contact, when no ball is pocketed
- Player's foot not touching the floor during the shot
- Object ball knocked off the table
- Ball accidentally touched/moved outside of a normal shot
- Shooting while balls are still in motion
- Ball-in-hand misplacement (in front of the head string when required behind it)
- Shooting out of turn
- Shooting with an object ball instead of the cue ball

*(v1: unsportsmanlike conduct, slow play/shot clock, and dispute cases requiring a human referee are not modeled — trusted play / self-refereeing.)*

## 8. Three consecutive fouls rule

- If a player commits 3 consecutive fouls (not interrupted by a legal shot), they **immediately lose the rack**.
- After the 2nd consecutive foul, the opponent or referee **must** warn the player; if a 3rd foul occurs without that notification, FFB keeps the count at 2 (art. 1.5.07).
- A legal shot resets the consecutive foul counter to zero.

**v1:** warning UI + auto rack loss on 3rd foul; clear via tap on foul chip. The “no warning → 3rd doesn’t count” exception is **not** modeled.

## 9. End of rack / end of match

- A rack ends when the 10-ball is legally pocketed last, or when a player loses due to three consecutive fouls.
- The match ends when a player reaches the required number of racks won.

## 10. Deliberately not modeled (deep mode / later)

- Shot clock / timing (article 1.2.13)
- Disciplinary cards and sanctions (article 1.2.17)
- Masters category specific rules (break box)
- Federal context (leagues, rankings, official competitions — Title III of the sporting code)
- Per-shot call/pocket entry, respot geometry, ball-in-hand state
