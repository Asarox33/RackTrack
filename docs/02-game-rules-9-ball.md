# 9-Ball — multi-ruleset notes + RackTrack coverage

> Rewritten technical / modeling notes — **not** official regulatory text.  
> Domain: `MatchEngine` + `GameMode.NINE_BALL`. Spec: `docs/01-product-specification.md`.  
> PDF authorities: [`resources/README.md`](../resources/README.md).

## Authorities

| Entity | PDF | Primary refs |
|--------|-----|----------------|
| **FFB** | `resources/code-sportif-americain-2026-2027.pdf` | Ch. 4 arts **1.4.01–1.4.07**; general Ch. 2 |
| **WPA** | `resources/wpa-rules-2026-01-02.pdf` (+ regs) | §**5** (5.1–5.9); Reg. **16** three-ball break |
| **Matchroom** | `resources/matchroom-wnt-event-rules.pdf` | WNT Event Rules over “Standard Rules and Regulations” (9-Ball tour) |
| **APA** | `resources/apa-team-manual-english.pdf` | Game Rules §3 (9-Ball); Masters notes |
| **BCA/CSI** | `resources/csi-official-rules-2025-08-12.pdf` | Rules Section **3** (3-1 … 3-7) |

---

## Common rules (FFB · WPA · Matchroom* · APA · BCA/CSI)

\*Matchroom = standard 9-Ball play **plus** WNT exceptions (break box, forceful break, shot clock, etc.).
In RackTrack Settings, **Matchroom (9)** applies to **9-ball only**; other modes stamp **FFB**
(app default).

Shared core:

- **No call shot:** ball + pocket are **not** required (unlike 8-Ball / 10-Ball / 14.1). All five authorities agree.
- **Objective:** pocket the **9** legally to win the rack (APA **league** also scores ball-count points for handicaps; Masters / rack win aligns with “pocket the 9”).
- **Rotation:** cue must contact the **lowest-numbered** object ball on the table first; combinations / caroms OK if that contact is first.
- **Rack:** diamond; **1** at apex; **9** in the center (on / at the foot-spot geometry per diagram); other balls random / no intentional pattern.
- **Break:** cue behind head string; must hit **1** first (APA: failing to hit 1 first is **not** itself a foul; FFB/WPA/CSI: wrong first ball → foul).
- **Open / continue:** legally pocket any ball → continue; legally pocket **9** (except push-out) → win rack (subject to scratch / foul exceptions).
- **9 irregular:** 9 pocketed with foul / jumped → typically **spotted**; other balls stay down (details differ for APA dead-ball scoring).
- Race-to-N racks is the pro / FFB / WPA / CSI match model RackTrack uses.

---

## Difference matrix

Only subjects that **actually diverge** between entities (identical topics stay under Common).

Final row **`RackTrack?`**: Yes / Partial / No. **Yes (already)** = taps exist; operator
judges rails / three-ball / soft (no geometry auto).

**Code cert (9-ball break):** `MatchEngine.recordDryBreak` — empty/soft open, blocks
Golden/Run out, **keeps push-out** (`PushOutEngine`: `DRY_BREAK` does not clear phase).
`MatchEngine.recordFoul` / history — **clears push-out** (`FOUL` → `PushOutPhase.NONE`).
`GameMode.supportsDryBreak` = 8 + 9 only.

| Entity | Legal break | Illegal / soft / three-ball | 9 on the break | Push-out | Foul → BIH | Three consecutive fouls | Scoring model |
|--------|-------------|-----------------------------|----------------|----------|------------|-------------------------|----------------|
| **FFB** | Hit **1** first; if no pocket ≥4 object balls to rail else foul; foul → BIH anywhere (1.4.03). | Soft / Magic Rack: **three-ball** rule (pocketed and/or cross head string); illegal → hand passes; 9 spotted if down; opponent accept (**no** push-out) **or** return shot (**push-out OK**). U17 / classic triangle exemptions (1.4.03). | Legal break pockets 9 → **win** (golden) (1.4.03). | After **legal** break; suspends lowest-first & rail-after-contact; 9 on push-out → spot; opponent take or give back (1.4.04). | Standard foul → BIH **anywhere** (Ch. 2 / 1.4). | **Lose rack**; must warn after 2nd; unwarned 3rd stays at 2 (1.4.07). | Race-to-N racks. |
| **WPA** | 5.3: ≥4 to rail if no pocket else foul; **three-ball** illegal break via Reg. 16. | Three-ball / illegal break per regulations. | Legal 9 on break wins rack (5.5). | 5.4 after non-foul break; opponent chooses next shooter. | Standard foul → BIH **anywhere** (5.7). | Serious foul **3.13** → lose rack (5.8). | Race-to-N racks. |
| **Matchroom** | **Break box** mandatory; **forceful** break — soft → warn, 2nd → foul + BIH (WNT §§1,4). | Relies on standard rules + forceful-break policy. | Same as standard (win on legal 9). | Assumed standard push-out (WNT does not repeal it). | Standard + WNT shot-clock / conduct fouls. | Standard three-foul loss unless event sheet says otherwise. | Race-to-N; WNT adds shot clock (30s + 1 ext; 60s after break/push-out), no time-outs, no conceding, etc. |
| **APA** | Behind head string; pocket **or** ≥4 to rail; must strike **1** first (miss ≠ foul). Soft break not allowed (League Mgmt). | Illegal → same breaker re-breaks; illegal + scratch → opponent breaks. | **9-on-the-Snap = win** unless scratch → spot 9 (or high ball) & turn passes. | **Not allowed** in handicapped league; **allowed** in Masters (US Amateur-style); balls on push-out spotted. | Foul on break → BIH **anywhere**; mid-game same. | Not the WPA three-foul rack-loss model in standard league play. | League: balls 1–8 = 1 pt, 9 = 2 pts toward race; Masters: rack win on legal 9. |
| **BCA/CSI** | Must hit **1** first; pocket **or** ≥4 cushions else foul (3-3). | Foul ends inning; jumped non-9 stay down; jumped 9 spotted. | Legal 9 on break → **win**; foul + 9 → spot 9 (3-3). | After non-foul break; opponent acknowledge; 9 spotted if pocketed (3-4). | Foul → BIH (general / 3-4 foul on push-out). | **Three successive fouls** → lose game (3-6). | Race-to-N racks (league events may add CSI tournament regs). |
| **RackTrack?** | **Yes (already)** — operator + `DRY` / `FOUL` | **Yes (already)** — ≥4 rails / three-ball / soft via `DRY` (push-out stays) or `FOUL` (push-out off); **no** accept/return illegal-break UI | **Yes (already)** — `GOLDEN`; pack: APA scratch-on-snap | **Yes (already)** — tree; pack: APA league **off** | **No** — no BIH UI | **Yes (already)** — `THREE_FOULS_LOSS`; pack: APA **off** | **Partial** — race-to-N **Yes**; APA ball-count **No** |

---

## RackTrack v1 — what is implemented

Race scoreboard. Richest race mode (golden + dry + three-foul loss + push-out).

| Topic | v1 |
|---|---|
| Race-to-N racks; +1 / Run out | **Yes** — `PLUS_ONE` / `RUN_OUT` |
| Alternating / winner break | **Yes** — setup `BreakRule` |
| Golden break — legal break pockets 9 | **Yes** — **GOLDEN** → awards rack |
| Dry / empty / soft / three-ball open | **Yes** — **DRY**; blocks Run out / Golden; push-out **stays** available |
| Illegal break as foul (miss 1, scratch, …) | **Yes** — **FOUL**; clears push-out |
| Illegal-break accept/return choice tree | **No** — not a separate UI (use DRY vs FOUL by judgment) |
| Push-out after legal / dry break | **Yes** — **PUSH OUT** → CLEAN/FOUL → TAKE / GIVE BACK |
| 9 respotted when pocketed/ejected irregularly | **No** — operator |
| 3 consecutive fouls → lose rack | **Yes** — `THREE_FOULS_LOSS` |
| Warning after 2 fouls | **Yes** — on-screen banner |
| Legal shot resets consecutive fouls | **Partial** — tap foul chip (`FOULS_CLEARED`) |
| Ball order / combo legality / BIH | **No** |
| Undo; summary (incl. golden / dry / push-out counts) | **Yes** |

Unlike 10-ball, FFB/WPA 9-ball is generally **without call**, except special cases.

---

## FFB narrative (modeling baseline)

### 1. Objective
Balls 1–9 + cue; ascending contact; win on legal **9**; race-to-N (1.4.01).

### 2. Rack
Diamond; 1 apex; 9 center on foot spot; others random; vary each rack (1.4.02). U17: 1 on foot spot; no three-ball rule.

### 3. Break
Hit 1; ≥4 rails if no pocket; foul → BIH anywhere; golden on legal 9; three-ball soft-break when Magic Rack / cloth marks used (1.4.03).

### 4. Push-out
After legal break only (1.4.04).

### 5–8. Flow / 9 / fouls / three fouls
See 1.4.05–1.4.07. **v1:** banner + auto loss on 3rd FOUL; “unwarned 3rd doesn’t count” **not** modeled.

### Deliberately not modeled
Rail / three-ball **auto-detect**; illegal accept/return as own phase (`DRY` keeps push-out,
`FOUL` clears it); respot geometry; BIH state; shot clock; federal machinery.
