# Product Specification — RackTrack

## 1. Project name

RackTrack

## 2. Goal

Provide a simple, reliable Android app to follow American pool **races (8 / 9 / 10)** and
**14/1 continuous** at the table without interrupting play. Deep FFB shot-by-shot refereeing
is out of scope without camera/AI ball detection.

## 3. Target users

Amateur or club players who want a fast scoreboard next to the table (phone/tablet, any orientation).

## 4. Problem solved

Typing every shot (ball + pocket) breaks game flow. Players need fast taps for rack wins / fouls /
14/1 points, a clear **break / hand indicator**, both scores at a glance, and a way to revisit
or share past match stats.

## 5. Game modes

### Current
- **8-ball race** — +1, Run out, Foul, Early 8, Dry break
- **9-ball race** — +1, Run out, Foul, Golden break, Dry break; 3 consecutive fouls = rack loss;
  **push-out** decision tree after a legal break
- **10-ball race** — +1, Run out, Foul; 3 consecutive fouls = rack loss; **push-out** (same tree)
- **14/1 continuous** — clear-rack +N, PASS/FOUL (balls-left swipe picker), BREAK −2 on opening
  with ACCEPT / RE-BREAK; distance + optional innings
- **14/1 solo training** — same 14/1 scoring with one player (setup Solo toggle); history stays under 14/1

### Later (only if product asks)
- Camera / AI ball detection — prerequisite for any automatic shot scoring

Mode-by-mode FFB summary vs what the app encodes: `docs/02-game-rules-*.md`.

## 6. Current features

- Create a match (2 names, mode, race length or 14/1 distance/innings, who starts;
  race: Alternate vs Winner break; **14/1 Solo** = one name / no opponent)
- Race length via **inline swipe** picker on setup (1–15, default 6); same strip for
  14/1 distance & innings (∞ on the same strip). Settings defaults use the same controls.
- Split-screen board on billiard cloth (felt tones: Forest, Blue, Burgundy, Charcoal, Pink, Golden)
- Landscape and portrait (`fullSensor`); live boards scale from available pane size
  (touch floor + tablet ceiling)
- Break / shooter indicator; alternate or winner break after each race rack
- 14/1: `TABLE n` object balls remaining on the shooter’s panel
- Undo last action; end-of-match summary (**SHARE PDF** + **BACK** to setup)
- Club **pause** (top-right, next to settings): stops match/rack duration accounting for a
  break (toilet / smoke / drink) — no max length, not recorded in stats/PDF; **not** an FFB
  player timeout (those are out of product — see Explicitly dropped / roadmap)
- Local match history:
  - Entry from setup (**HISTORY**, felt color), scoped to the selected game mode
  - Filter by player names (either seat); reopen the same stats as end-of-match
  - Delete with red ✕ + confirmation
- Share styled PDF of the full summary (start / end / duration; not a scrolled screenshot)
- **Settings** full screen (like History): theme, keep screen on, haptics, **Match
  defaults** as mode rows (8 / 9 / 10 / 14/1) with summary → drill-down editor, ruleset
  pack + official-rules link, Remove ads / Restore on one row
- About (from Setup): version, build type/time, credits, GitHub repo link

## 7. Explicitly out of scope

- **Remote / distant live scoring** (two players on two tables or two phones driving
  one match) — forever out of product sense; **one device is the scoreboard of record**
- Per-shot call ball + pocket entry (and any deep FFB referee UI without camera)
- Illegal-break geometry automation, respot geometry, ball-in-hand state machine
- iOS
- Shot clock / per-shot timing (art. 1.2.13) and disciplinary / federal competition machinery
- FFB **player timeouts** / pause quotas (art. 1.2.16) — club duration **pause** only
- Screenshot / UI golden tests
- PNG export of match summary (PDF only)

Accounts / cloud identity and **peer match import** (QR / nearby / file) are **out**
(owner 2026-08-26). Match handoff stays **PDF share** + local history on the scoring phone.

**Later (2.0 / `rc/2.0.0`, see roadmap):** **i18n**, multi-ruleset packs, KMP `:shared`
(feature scope closed aside from polish / store). UI language ≠ rule authority.

**Later (3.0):** Compose Multiplatform UI + **iOS** (see `00-release-trains.md`).

## 8. Constraints

- Owner is learning native Android — keep iterations short and testable
- Domain logic stays pure Kotlin (no Android deps) for fast JVM tests

## 9. Success criteria

Standing at a table, two players can run an 8/9/10 race or a 14/1 to a set distance with the
relevant taps, see who breaks / has the hand, finish, review or share a summary, and later
find that match in history — without entering shot details.

## 10. Product version

**1.2.2** on `main` / Closed — 14/1 innings table layout (on top of **1.2.1** adaptive boards
+ **1.2.0** monetization). **2.0.0** develops on `rc/2.0.0` (merge after 1.2 prod).
Versioning and GitHub Release notes: `docs/07-versioning-and-releases.md`. Binaries for
end users ship via app stores (see `LICENSE`).

**Play production gate:** first open / production release to strangers is **1.2.2** and must
include monetization per `docs/09-monetization.md` (AdMob interstitial on Start only,
5‑min cooldown, never block start, lifetime Remove Ads ~**4,99 €** + Restore; code since
**1.2.0**). Do not publish an ad-free Play build then add ads later.
