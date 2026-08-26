# 8-Ball — multi-ruleset notes + RackTrack coverage

> Rewritten technical / modeling notes — **not** official regulatory text.  
> Product: race scoreboard only (`GameMode.EIGHT_BALL`). Spec: `docs/01-product-specification.md`.  
> PDF authorities: [`resources/README.md`](../resources/README.md).

## Authorities

| Entity | PDF | Primary refs |
|--------|-----|----------------|
| **FFB** | `resources/code-sportif-americain-2026-2027.pdf` | Ch. 3 arts **1.3.01–1.3.06**; general Ch. 2 |
| **WPA** | `resources/wpa-rules-2026-01-02.pdf` | §**4** (4.1–4.11) + general §§1–3 |
| **APA** | `resources/apa-team-manual-english.pdf` | Game Rules §3 (8-Ball) |
| **BCA/CSI** | `resources/csi-official-rules-2025-08-12.pdf` | Rules Section **2** (2-1 … 2-11) |

Matchroom WNT is **9-Ball only** — see `02-game-rules-9-ball.md`.

---

## Common rules (entities that govern 8-Ball: FFB · WPA · APA · BCA/CSI)

Shared core — scoreboard- and play-relevant — where the PDFs agree in substance:

- **Objective:** clear one’s group of seven (solids 1–7 or stripes 9–15), then legally pocket the **8** to win the rack.
- **Equipment:** fifteen object balls + cue ball; triangle rack with **8 in the center** of the three-ball row; one solid and one stripe typically required in the two rear corners (FFB / WPA / CSI; APA allows remaining balls “any order” but still centers the 8).
- **Break:** cue ball in hand **behind the head string**; break is **not** a called shot; cue need not hit a named ball first (APA: must strike head ball or second row — failure is **not** a foul, but legal break still needs pocket or four rails).
- **Legal open (no pocket):** at least **four** object balls must hit a rail (or a ball is pocketed) — otherwise illegal / defective break handling (details differ — see matrix).
- **Open table** until groups are established by a legal post-break pocket of a group ball.
- **Continue** after legally pocketing a ball of one’s group; miss / foul → hand passes (subject to foul penalties).
- **Win:** legally pocket the 8 after clearing one’s group (APA league: **marked** pocket for the 8).
- **Lose-rack family** includes early 8, 8 scratched / with foul, 8 in wrong / uncalled pocket, 8 jumped off table (wording and break exceptions differ — see matrix).
- Match format for RackTrack: **race-to-N racks** (APA league also uses handicap / team scoring outside our scoreboard scope).

---

## Difference matrix

Columns = rule subjects · rows = governing entities. Cells cite the authority PDF.

Final row **`RackTrack?`**: Yes / Partial / No — scoreboard encoding for multi-ruleset.
**Yes (already)** = taps/counters exist today; operator judges geometry (no auto detect).

| Entity | Call shot | Rack / placement | Legal break | Illegal / soft break | 8 on the break | Foul → ball-in-hand | Three consecutive fouls | Early-8 / lose-rack cases |
|--------|-----------|------------------|-------------|----------------------|----------------|---------------------|-------------------------|---------------------------|
| **FFB** | Every shot after break: ball + pocket (1.3.01, 1.3.05). | Triangle; 8 center; apex any on foot spot; different groups in rear corners (1.3.02). | Behind head string; no call; any first contact; pocket **or** ≥4 object balls to rail (1.3.03). | Opponent may **accept**, **re-rack & break**, or **re-rack & force re-break** (1.3.03). | Legal: spot 8 & continue **or** re-rack/re-break. + cue foul: opponent spots 8 with BIH behind line **or** re-rack (1.3.03). | Mid-rack foul → BIH **anywhere** (1.3.05); break-specific choices often behind line (1.3.03). | **None** in Ch. 3. | Early 8; 8 off table (not break); 8 + foul; 8 wrong pocket (1.3.06). |
| **WPA** | Standard call shot after break (4.6 / 1.7); safety OK. | Apex on foot spot; 8 first ball below apex; one of each group on lower corners (4.2). | Same rail/pocket rule as FFB (4.3). | Same three options as FFB (4.3(d)). | Same spot/re-break options as FFB (4.3(e)(f)). | Standard foul → BIH **anywhere** (4.9). | **None** (3.13 not applied to 8-Ball). | 4.8: 8+foul; early 8; uncalled pocket; 8 off table (not break). |
| **APA** | Generally **no** call (except **mark** 8 pocket in league; Masters may call) — Game Rules. | Triangle; 8 center; other balls any order. | Behind head string; pocket **or** ≥4 to rail; cue must not hit rail before rack; strike head / 2nd row (not a foul if missed). | Illegal break → **same breaker** re-racks/re-breaks; illegal + scratch → **opponent** breaks. Soft break discouraged (penalties via League Mgmt). | **Win** unless cue foul → **loss**. | Mid-game BIH **anywhere**; foul on **legal break** → BIH **behind head string** (8-Ball). | **None** (league). | Early 8 / scratch on 8 / wrong pocket etc. per Game Rules; 8-on-break special win/loss above. |
| **BCA/CSI** | Call shot game (2-1); safeties declared (2-8). | Apex on foot spot; 8 middle of three-ball row; opposite groups on rear corners (2-2). | Pocket **or** ≥4 cushions; soft break must be notified (2-3). | Illegal break: opponent **must** choose re-rack & break **or** force re-break — **no accept-table**; game cannot continue until legal break (2-3-3). | Legal: spot & accept **or** re-rack/re-break. + foul: opponent spots with BIH **anywhere** **or** re-racks (2-4). | Foul on legal break (no 8) → BIH **anywhere** (2-3-4). | **None** for 8-Ball. | 2-10: illegal 8; 8 jumped (not break); 8 with last group ball; uncalled non-obvious 8; some General Rule losses. |
| **RackTrack?** | **No** — no call tracking | **No** | **Yes (already)** — operator; empty/illegal open → `DRY` (`GameMode.supportsDryBreak` on 8) | **Yes (already)** — same `DRY` / `FOUL` taps; **no** accept/re-rack/re-break choice UI | **Yes** — pack: APA **`8OB`** win tap; scratch → `EARLY 8`; FFB/WPA/CSI spot remains operator | **No** — soft `FOUL` counter; no BIH zones | **No** — unused in 8 | **Yes (already)** — `EARLY 8` |

---

## RackTrack v1 — what is implemented

Race scoreboard only. No solids/stripes table state; players judge groups and calls.

| Topic | v1 |
|---|---|
| Race-to-N racks; +1 / Run out award rack | **Yes** — `PLUS_ONE` / `RUN_OUT` |
| Alternating / winner break | **Yes** — setup `BreakRule` |
| Early-8 / lose rack cases | **Yes** — single **EARLY 8** → opponent wins rack (`EIGHT_BALL_LOSS`) |
| Dry / empty / soft / illegal open (≥4 rails) | **Yes** — **DRY** (`supportsDryBreak`); blocks Run out; no accept/re-rack UI |
| Illegal-break opponent choices (accept / re-rack / force) | **Partial** — use `DRY` / `FOUL`; **no** choice-tree UI |
| 8 pocketed on break → respot or re-rack choices | **No** — not a button (not a golden win) |
| Foul as ball-in-hand | **Partial** — **FOUL** increments a counter only; **no** 3-foul rack loss |
| Call every ball; open table / group choice | **No** |
| Undo; match summary (incl. Early 8 count) | **Yes** |

Current app default authority for modeling text historically: **FFB**. Multi-ruleset packs (WPA / APA / BCA&CSI; **Matchroom** = 9-Ball overlay only) are the **2.0** backlog item.

---

## FFB narrative (modeling baseline)

### 1. Objective
Fifteen numbered balls + cue ball; group of seven then legal **8**; race over several racks; every object ball after the break **called** (1.3.01).

### 2. Rack setup
Standard triangle: 8 center; apex on foot spot; one solid and one stripe in rear corners (1.3.02).

### 3. Break
BIH behind head string; no call; illegal if no pocket and fewer than 4 object balls to rail → opponent accept / re-break self / force re-break. 8 on break options per 1.3.03 (not a loss).

### 4. Open table / groups
Open until legal called group ball (1.3.04).

### 5. Normal rack flow
Call ball + pocket; continue on legal pocket; then call the 8 (1.3.05).

### 6. Loss of rack (1.3.06)
Early 8; 8 off table (except break); 8 with foul; 8 wrong pocket. **v1:** one **EARLY 8** action.

### 7. Fouls
Typical foul → BIH anywhere (1.3.05). Full list Ch. 2 art. 1.2.09. **No** three-foul rack loss in 8-Ball.

### 8. Deliberately not modeled
Open-table state machine; illegal-break **choice-tree UI** (scoreboard uses `DRY`/`FOUL`);
8-on-break spot/re-rack UI; per-shot call; BIH zones; shot clock; rail auto-detect; federal machinery.
