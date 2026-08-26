# 14/1 Continuous — multi-ruleset notes + RackTrack coverage

> Rewritten technical / modeling notes — **not** official regulatory text.  
> Domain: `FourteenOneEngine` + `GameMode.FOURTEEN_ONE`. Spec: `docs/01-product-specification.md`.  
> PDF authorities: [`resources/README.md`](../resources/README.md).

## Authorities

| Entity | PDF | Primary refs |
|--------|-----|----------------|
| **FFB** | `resources/code-sportif-americain-2026-2027.pdf` | Ch. 6 arts **1.6.01–1.6.07**; general Ch. 2 |
| **WPA** | `resources/wpa-rules-2026-01-02.pdf` | §**7** (7.1–7.12) |
| **APA** | — | **N/A** — Team Manual Game Rules are 8 & 9 only |
| **BCA/CSI** | `resources/csi-official-rules-2025-08-12.pdf` | Rules Section **6** (6-1 … 6-18) |

Matchroom WNT is **9-Ball only** — see `02-game-rules-9-ball.md`.

---

## Common rules (entities that govern 14.1: FFB · WPA · BCA/CSI)

- **Objective:** fifteen object balls + cue; each legally pocketed called ball = **1 point** (extras on the same legal shot also score); first to the agreed **distance** wins (FFB also allows optional **innings / reprises** limit + OT — product already models this).
- **Call shot:** ball + pocket required (obvious-shot shortcuts vary by code / local agreement).
- **Continuous:** after **fourteen** balls are pocketed, fourteen are **re-racked** with apex vacant; the fifteenth stays; shooter continues the visit.
- **Opening break:** cue behind head string; legal if called ball is pocketed **or** cue + **two** object balls are driven to a rail after contacting the rack; failure → **−2** opening-break penalty and opponent may accept or force re-break (penalties can stack on repeated illegal opens).
- **Classic foul:** typically **−1**; balls pocketed on foul shots are **spotted**; cue scratch / jumped cue → opponent usually gets cue **behind the head string** (not free table BIH like race games).
- **Three successive fouls:** extra heavy penalty (**−15** in addition to the foul points) and re-rack / re-break consequences (wording aligned across FFB / WPA / CSI).
- Safety / miss ends the visit without automatic anywhere-BIH.

---

## Difference matrix

Final row **`RackTrack?`**: Yes / Partial / No. **Yes (already)** = coded today.

**Code cert (14/1 open):** `FourteenOneEngine.breakFoul` (−2, stays on open),
`acceptIllegalOpen`, re-break path; classic `FOUL` (−1) separate. Combined −3 path not one action.

| Entity | Scoring / match end | Call / safety | Opening break | Opening + classic foul | Mid-game foul | Three successive fouls | Re-rack / interference |
|--------|---------------------|---------------|---------------|------------------------|---------------|------------------------|------------------------|
| **FFB** | Race to points; optional reprises limit; tie → +5 OT (1.6.01, 1.6.05). | Call ball+pocket; “Safe” OK; pocketed on safety spotted (1.6.04). | Cue + 2 object balls to rail **or** called ball pocketed; illegal → **−2**; opponent accept or force re-break; repeats **stack −2** (1.6.03). | Illegal open + classic foul → **−3** then accept/re-break choice (1.6.03(g)). | Classic foul **−1**; no free-table BIH; scratch → BIH behind line (1.6.07). | −1 per foul + extra **−15**; count resets; fifteen-ball re-rack; fouler must open again (1.6.07). | Continuous re-rack + special frozen/interfering cases (1.6.04 / 1.6.06). |
| **WPA** | First to required score (7.4, 7.7); scores may go negative. | Standard call; safety → pocketed balls spotted (7.5–7.6). | Same rail/called-ball test; breaking foul **−2**; non-breaker accept or require another opening break until legal or accepted (7.3, 7.10). | Covered under breaking foul / serious fouls (7.10–7.11). | Fouls subtract points; spotting per 7.6 / 7.9. | Serious foul path for three consecutive fouls (7.11) — rack/penalty per general 3.13 adapted to 14.1 scoring. | Special racking when cue/15th interfere (7.8). No ball-rack template (7.2). |
| **APA** | N/A | N/A | N/A | N/A | N/A | N/A | N/A |
| **BCA/CSI** | Predetermined point total (6-1). | Call shot; illegally pocketed / safety balls spotted (6-10). | Same legal test (6-3); violation **−2**; accept or force re-rack/re-break; repeats until legal or accepted (6-4). Opening violation **does not** count toward successive fouls. | Scratch/foul **and** open violation → **−2 only** (treated as open violation); opponent BIH behind line **or** force re-break (6-6) — **not** FFB’s −3 split. | Legal open + scratch/foul → **−1**; scratch → BIH behind line (6-5). | Three successive fouls → additional **15-point** penalty (6-18). | Subsequent racks: player racks fourteen, apex vacant; placement rules when cue/15th interfere (6-8, 6-9). Rear corners **1** and **5** required on opening rack (6-2). |
| **RackTrack?** | **Yes (already)** — distance / innings / OT | **No** | **Yes (already)** — `BREAK −2` + ACCEPT / RE-BREAK (`breakFoul` / `acceptIllegalOpen`) | **Yes** — FFB/WPA stack classic foul → −3; BCA/CSI pack: open stays −2 only | **Yes (already)** — `FOUL` −1 | **Yes (already)** — `THREE_FOUL_PENALTY` −15 | **Partial** — On Table count; no geometry |

---

## RackTrack v1 — what is implemented

Points scoreboard with hand indicator. `objectBallsOnTable` approximates continuous re-rack; geometry / call-shot **not** enforced.

| Topic | v1 |
|---|---|
| Race to distance (points to win) | **Yes** — setup `pointsToWin` |
| Optional innings limit + +5 OT on tie | **Yes** |
| Add points during a visit | **Yes** — **+(n−1)** clear-to-re-rack; PASS/FOUL modal syncs last partial |
| Object balls remaining (approx.) | **Yes** — start 15; PASS/FOUL sets **2–15**; 3-foul → 15 |
| Legal end of turn (pass hand) | **Yes** — **PASS** |
| Classic foul −1, hand to opponent, no free BIH | **Yes** — **FOUL** |
| Legal shot / pass clears consecutive fouls | **Yes** — via `addPoints` / `PASS` |
| 3 consecutive fouls → −1 then extra **−15**, re-break | **Yes** — `THREE_FOUL_PENALTY` |
| Illegal opening break −2 | **Yes** — **BREAK −2** while `awaitingOpeningBreak` |
| Opponent ACCEPT / RE-BREAK after illegal open | **Yes** — −2 stacks on re-break |
| Classic foul on legal open = −1 | **Yes** — use FOUL, not BREAK −2 |
| Combined illegal open + classic foul (−3) | **No** — single action only |
| Call ball + pocket; continuous re-rack geometry | **Partial** — table count only |
| High run / current run / innings stats | **Yes** |
| Undo; summary with net per-inning points | **Yes** |

Only the **current shooter**’s actions are enabled (unlike the race board).

---

## FFB narrative (modeling baseline)

### 1. Objective
+1 per legal ball; distance or reprises (+5 OT) (1.6.01, 1.6.05). Continuous re-rack after fourteen (1.6.01).

### 2–3. Rack / opening break
1.6.02–1.6.03 (−2 / accept / re-break; −3 combined path).

### 4–6. Play / innings / fouls
1.6.04–1.6.07. **v1:** each FOUL −1; 3rd also applies −15; opening illegal = **BREAK −2**.

### Deliberately not modeled
Per-ball identity; continuous re-rack geometry (1.6.06); BIH-behind-line table state; −3 combined open+foul as one coded path; shot clock; disciplinary cards.
