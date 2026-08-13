# Changelog

User-facing notes for GitHub Releases (notes only — no APK attached). When bumping
`racktrack.versionName`, add a `## X.Y.Z` section **before** merging to `main`.
CI (`cut-release.yml` / `release.yml`) copies that section into the GitHub Release body.

Format: [Keep a Changelog](https://keepachangelog.com/)-style headings under each version.

## 1.0.2 — 2026-08-13

### Features

- **Scroll hints on modals** — when summary, settings, or 14/1 visit-end content overflows,
  a bottom fade + ▼ chevron appears until you reach the end (so scroll is discoverable).

### Bug fixes

- **14/1 PDF innings table** — vertical column separators after the inning `#` and after
  player 1 (pts + end), so the two sides read more clearly.

## 1.0.1 — 2026-08-12

### Features

- **Club match pause** — pause/resume control next to Settings on the board. Stops match and
  rack duration accounting for a break (toilet / smoke / drink). No max length; pause time is
  not shown in stats or the PDF (not an FFB player timeout, not a shot clock).
- **14/1 innings table (summary + PDF)** — one paired table: inning `#`, player 1 points + end,
  player 2 points + end (readable score sheet instead of two separate columns).
- **Foul clear affordance (9/10)** — when consecutive fouls can be reset after a legal shot, the
  foul chip shows a border and **TAP TO CLEAR**.

### Bug fixes

- **14/1 visit-end modal** — when balls left is raised above On Table, the warning no longer
  pushes CANCEL/CONFIRM off-screen (height cap + scrollable body; landscape and portrait).
- **Golden / Dry after foul clear (9/10)** — clearing the consecutive-foul counter no longer
  re-enables Golden, Dry, or break-and-run for the same rack. Match foul totals still count
  every `FOUL` event.
- **8-ball** — consecutive-foul clear remains unavailable; Dry stays disabled after a foul in
  the rack.

## 1.0.0 — 2026-08-10

### Features

- First table-ready release: 8/9/10 race boards + 14/1 continuous, settings, local history,
  styled PDF share, alternate/winner break, 14/1 visit-end modal and On Table tracking.
- Semver + automatic GitHub Release APK after green CI on `main`.
