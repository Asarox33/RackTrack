# Product Specification — RackTrack v0.1

## 1. Project name

RackTrack

## 2. Goal

Provide a simple, reliable Android app to score American pool matches, following official FFB rules, starting with 10-ball.

## 3. Target users

- Amateur or FFB-licensed player who wants to track their score in a casual or club match
- Eventually, a club referee/scorekeeper in amateur tournaments (out of scope for v1)

## 4. Problem solved

Manually tracking score, fouls, and edge cases (illegal break, push-out, early 10-ball, three consecutive fouls) is error-prone and a common source of disputes. The app encodes the rules to make scoring reliable and keep the game flowing.

## 5. Game modes

### V1 (MVP)
- **10-ball** (FFB rules, 2026-2027 season)

### V2 (after MVP validation)
- 9-ball
- 8-ball
- (14.1 continuous and Artistic pool: evaluated later, significantly higher scoring complexity)

## 6. MVP features

- Create a match (2 players, player names)
- Choose the format (number of racks needed to win)
- Handle the break (legal / illegal, opponent's choice on illegal break)
- Handle the push-out after a legal break
- Call shot (ball + pocket) on every shot
- Pocket a ball (+1 point), enforce ascending ball order
- Handle fouls (FFB list), with ball-in-hand for the opponent
- Handle the 10-ball special case (pocketed early / uncalled / knocked off table → respotted)
- Consecutive foul counter (3 consecutive fouls = loss of rack)
- End of rack / end of match (best of N racks)
- Match history
- Simple statistics (racks won/lost per player, basic average)

## 7. Explicitly out of scope for v1

- User accounts, authentication
- Cloud, multi-device sync
- Network / remote multiplayer mode
- Any game mode other than 10-ball
- Shot clock / timing (FFB code article 1.2.13 — too complex for MVP)
- Disciplinary card/penalty management (article 1.2.17)
- Anything related to federal competitions (Title III of the sporting code: categories, leagues, national rankings) — out of scope for a match-scoring app
- iOS

## 8. Constraints

- The project owner is starting from zero in native Android development. The development pace must stay realistic; the MVP must be achievable through short, testable iterations.
- Rules must stay faithful to the FFB sporting code for 10-ball (see `docs/02-game-rules-10-ball.md` and the source PDF in `resources/`).

## 9. MVP success criteria

- A full 10-ball rack can be scored end-to-end following core FFB rules (excluding timing and disciplinary matters, explicitly out of scope).
- The app correctly handles at minimum: legal/illegal break, push-out, ascending ball order, the 10-ball special case, three consecutive fouls, ball-in-hand.
- Smooth one-handed use in real playing conditions (large buttons, minimal text to read).
