# 10-Ball — multi-ruleset notes + RackTrack coverage

> Rewritten technical / modeling notes — **not** official regulatory text.  
> Domain: `MatchEngine` + `GameMode.TEN_BALL`. Spec: `docs/01-product-specification.md`.  
> PDF authorities: [`resources/README.md`](../resources/README.md).

## Authorities

| Entity | PDF | Primary refs |
|--------|-----|----------------|
| **FFB** | `resources/code-sportif-americain-2026-2027.pdf` | Ch. 5 arts **1.5.01–1.5.07**; general Ch. 2 |
| **WPA** | `resources/wpa-rules-2026-01-02.pdf` | §**6** (6.1–6.11) |
| **APA** | — | **N/A** — Team Manual Game Rules cover 8 & 9 only |
| **BCA/CSI** | `resources/csi-official-rules-2025-08-12.pdf` | Rules Section **4** (4-1 … 4-9) |

Matchroom WNT is **9-Ball only** — see `02-game-rules-9-ball.md`.

---

## Common rules (entities that govern 10-Ball: FFB · WPA · BCA/CSI)

- **Objective family:** rotation 1→10 with cue + ten object balls; win by legally pocketing the **10** under each code’s win condition (see matrix — **CSI wording differs** from FFB/WPA on “early 10”).
- **Call shot:** every non-break shot must be called (ball + pocket) — all three codes.
- **Rack:** triangle / dedicated shape; **1** at apex on foot spot; **10** in the center of the rack; other balls largely random (CSI additionally fixes **2** and **3** on the rear corners).
- **Break:** cue behind head string; must contact **1** first; if no pocket, ≥4 object balls to rail/cushion else foul; foul → opponent BIH anywhere.
- **Push-out:** available after a **non-foul** break for the player with the shot; suspends lowest-ball-first and rail-after-contact; **10** pocketed on push-out → spotted; opponent chooses take or give-back if clean.
- **10 special:** 10 pocketed / jumped irregularly → **spotted** (except on the winning shot under that code).
- **Three consecutive / successive fouls** → lose the rack/game.
- Match model for RackTrack: **race-to-N racks**.

---

## Difference matrix

Final row **`RackTrack?`**: Yes / Partial / No. **Yes (already)** = taps exist; operator
judges rails (no geometry auto). **10-ball has no `DRY`** — break failures use `FOUL`.

| Entity | Win condition | Call / safety | Legal break | Push-out | Early / irregular 10 | Foul → BIH | Three consecutive fouls |
|--------|---------------|---------------|-------------|----------|----------------------|------------|-------------------------|
| **FFB** | Legal **10 last** after 1–9 down (1.5.01, 1.5.06). | Every shot announced (1.5.01); “Safe” allowed — if a ball is pocketed, opponent chooses take or give-back (1.5.05). | Hit **1**; ≥4 rails if no pocket; Masters **break box** (1.5.03). | After legal break (1.5.04). | Early legal pocket → **spot 10**, shooter **continues**; foul/unannounced → spot + hand passes (1.5.06). | Foul → BIH **anywhere** (break covered in 1.5.03). | Lose rack; warn after 2nd; unwarned 3rd stays at 2 (1.5.07). |
| **WPA** | Legal 10 when it is the **only** object ball left (intro §6 / 6.7). | Call shot (6.5); **no** “safety” call at 10-Ball. | Hit pattern + ≥4 rails if no pocket (6.3). | 6.4 after non-foul break. | 10 spotted if off table or pocketed other than final win (6.8). Wrongful pocket → opponent chooses next shooter (6.6). | Standard foul → BIH **anywhere** (6.9). | 3.13 → lose rack (6.10). |
| **APA** | N/A | N/A | N/A | N/A | N/A | N/A | N/A |
| **BCA/CSI** | “Pocket the 10 on **any legal shot after the break**” (4-1) / “legally pockets the 10” (4-5-3) — **early called 10 can win**; 10 pocketed **in addition** to another called ball is spotted (4-5-2). | Call shot; illegally pocketed non-10 stay down; opponent option on illegal pocket (4-7, 4-8). | Must hit **1**; pocket or ≥4 cushions (4-3). | 4-4 (same structure as CSI 9-Ball). | 10 on break → spot & continue (4-3-3); illegally pocketed / jumped 10 spotted (4-5, 4-7). | Foul → BIH (general CSI). | Three successive fouls → lose (4-9). |
| **RackTrack?** | **Partial** — no ball tracking; pack text for +1 / run-out; CSI early-10 = judgment | **No** | **Yes (already)** — miss 1 / fewer than 4 rails → **`FOUL`** (no `DRY` on 10); clears push-out | **Yes (already)** — same tree as 9; pack: APA **off** | **No** — respot/continue operator | **No** — no BIH UI | **Yes (already)** — `THREE_FOULS_LOSS`; pack: APA **off** |

---

## RackTrack v1 — what is implemented

Race scoreboard — not shot-by-shot referee. Players judge legality; app records outcomes.

| Topic | v1 |
|---|---|
| Race-to-N racks; +1 / Run out award rack | **Yes** — `PLUS_ONE` / `RUN_OUT` |
| Alternating / winner break | **Yes** — setup `BreakRule` |
| Foul increments consecutive counter | **Yes** — `FOUL` |
| 3 consecutive fouls → lose rack | **Yes** — `THREE_FOULS_LOSS` |
| Warning after 2 consecutive fouls | **Yes** — on-screen banner |
| Legal shot resets consecutive fouls | **Partial** — tap foul chip (`FOULS_CLEARED`) |
| Golden break / Dry break buttons | **No** — `supportsGoldenBreak` / `supportsDryBreak` off for 10 |
| Illegal / soft break (≥4 rails, miss 1) | **Yes** — **`FOUL`** (clears push-out); no separate DRY |
| Push-out after legal break | **Yes** — same tree as 9-ball; summary / PDF / history count |
| Call ball + pocket, 10 respot, BIH, ball order | **No** — operator judgment |
| Undo; match summary (incl. foul / push-out counts) | **Yes** |

---

## FFB narrative (modeling baseline)

### 1. Objective
Balls 1–10; ascending contact; **call** every shot; win by legal **10 last** (1.5.01 / 1.5.06).

### 2–4. Rack / break / push-out
Dedicated triangle; 1 apex on foot spot; 10 center (1.5.02). Break & push-out (1.5.03–1.5.04).

### 5–8. Flow / 10 / fouls / three fouls
1.5.05–1.5.07. Full foul list art. 1.2.09. **v1:** warning UI + auto rack loss; “no warning → 3rd doesn’t count” **not** modeled.

### Deliberately not modeled
Shot clock; disciplinary cards; Masters break box; per-shot call/pocket entry; respot geometry; BIH state; federal Title III.
