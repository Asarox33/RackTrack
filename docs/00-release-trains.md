# Locked release trains — RackTrack

**Status:** locked 2026-08-15 (owner answers applied) · keep in sync when trains change.  
**Agents:** read this **before** inventing version numbers or dropping backlog items.
Canonical backlog detail: [`06-roadmap-todo.md`](06-roadmap-todo.md).

---

## Trains at a glance

| Train | Channel | Contents |
|-------|---------|----------|
| **1.0.x – 1.1.1** | GitHub notes + sideload / **internal** smoke | Table product; **1.1.0** solo/PDF/Golden; **1.1.1** OFL font notices |
| **1.2.0** | **First open / production Play** for strangers | Monetization (`09-monetization.md`) + upload keystore + privacy/Data safety for ads + R8 mapping + native debug symbols |
| **1.2.x** | Play hotfixes after launch | Crash / store-review fixes only (no new optional feature trains planned) |
| **2.0.0** on `rc/2.0.0` | Merge to `main` only when ready | i18n, multi-ruleset, KMP/CMP/iOS, **Blue Night + Cyan** brand/DS (APP vs GAME), **accounts / shared stats** (never live remote scoring) |
| **3.x+** (maybe) | Not scheduled | Camera / AI ball detection — only if owner reopens (`06` Maybe 3.x+) |

```text
main (1.x patchable)
  │
  ├─ 1.1.0 ✅ shipped (GitHub)
  ├─ 1.1.1 ✅ OFL font attribution
  ├─ 1.2.0 / 1.2.1 / **1.2.2** 🔧 Play gate (Internal CI ✅ · Closed 12×14 ⏳ · §E ✅ · prod ⬜)  ← main
  └─ 1.2.x hotfixes on `main` if needed
       │
       └── branch **rc/2.0.0** ✅ opened 2026-08-22 (work while 12×14 runs)
                 → merge to `main` only when ready as **2.0.0** (after Play production)
```

---

## Owner decisions (locked)

1. **Play Closed testing is required for production access** on this developer account
   (Google policy: ≥12 closed testers opted in for ≥14 consecutive days). **Internal**
   remains owner smoke only and does **not** unlock production. Put monetized **1.2.0**
   on Closed while the clock runs; **open / production for strangers still = 1.2.0 with
   ads** (do not soft-launch ad-free).
2. **`rc/2.0.0` may open while Closed 12×14 runs** (owner 2026-08-22) so design / KMP /
   i18n can start off `main`. **Do not merge `rc/2.0.0` → `main` until Play production
   1.2.x is live** (and the train is ready). Keep 1.2 hotfixes on `main` only.
3. **FFB player timeouts (art. 1.2.16) are out of product** — too close to per-action
   match officiating; club **pause** (duration freeze) remains. See Explicitly dropped.

---

## Hard product gates

1. **Do not** publish an **ad-free** open/production Play build, then add ads later.
2. **Do not** put i18n / multi-ruleset / KMP on the **1.2.0** Play launch critical path.
3. **Do not** silently delete backlog lines from `06-roadmap-todo.md` when editing it —
   move to ✅ / Explicitly dropped / another train with a progress note.
4. Pool **domain** never imports AdMob / Billing / UMP (`09-monetization.md` §9).
5. Official binaries = **stores** (or local build). GitHub Releases = **notes only** (`LICENSE`).
6. **Do not** implement FFB player timeouts / shot-clock-like pause quotas (dropped).
7. Production access needs **Closed 12×14** on this account; keep **Internal** for owner
   smoke. Do not skip Closed hoping Internal alone unlocks Production.

---

## Next sequence (human + agents)

| # | Who | Action |
|---|-----|--------|
| 1 | Human | Finish Google Play account validation ✅ |
| 2 | Human | Create upload keystore locally; fill `keystore.properties` (never commit) ✅ |
| 3 | Repo | Privacy URL live (see `privacy-policy.md` + `08-play-store.md`) ✅ |
| 4 | Code | Implement **1.2.0** monetization per `09-monetization.md` ✅ |
| 5 | Human | Internal smoke ✅ → **Closed** (≥12 ✅, 14 days ⏳ from 2026-08-20) → Console `08` §E ✅ → **production 1.2.0+** ⬜ |
| 6 | Repo | Branch **`rc/2.0.0`** opened 2026-08-22 — work order in `06`; merge to `main` after prod |
| — | Repo | CI → Play Internal (`play-internal.yml`) ✅ smoke 102015 |

---

## Doc map

| Doc | Role |
|-----|------|
| `01-product-specification.md` | Product scope |
| `06-roadmap-todo.md` | Checklist backlog |
| `07-versioning-and-releases.md` | Semver + CI notes releases |
| `08-play-store.md` | Play Console / AAB / listing checklist |
| `09-monetization.md` | Ads + IAP + architecture |
| `privacy-policy.md` | Store privacy URL content (stub → expand for ads) |
| `../LICENSE` | Source-available; **owner** may monetize stores; others may not redistribute |
