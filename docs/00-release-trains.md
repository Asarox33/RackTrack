# Locked release trains — RackTrack

**Status:** locked 2026-08-15 (owner answers applied) · keep in sync when trains change.  
**Agents:** read this **before** inventing version numbers or dropping backlog items.
Canonical backlog detail: [`06-roadmap-todo.md`](06-roadmap-todo.md).

---

## Trains at a glance

| Train | Channel | Contents |
|-------|---------|----------|
| **1.0.x – 1.1.1** | GitHub notes + sideload / **internal** smoke | Table product; **1.1.0** solo/PDF/Golden; **1.1.1** OFL font notices |
| **1.2.0** | **First open / production Play** for strangers | Monetization (`09-monetization.md`) + upload keystore + privacy/Data safety for ads |
| **1.2.x** | Play hotfixes after launch | Crash / store-review fixes only (no new optional feature trains planned) |
| **2.0.0** on `rc/2.0.0` | Merge to `main` only when ready | i18n, multi-ruleset, KMP/CMP/iOS, optional accounts |

```text
main (1.x patchable)
  │
  ├─ 1.1.0 ✅ shipped (GitHub)
  ├─ 1.1.1 ✅ OFL font attribution
  ├─ 1.2.0 ⬜ Play end-user gate (ads + IAP)   ← next product code
  └─ 1.2.x hotfixes if needed
       │
       └── branch rc/2.0.0  (open AFTER 1.2.0 is on Play production)
                 → 2.0.0 merge when ready
```

---

## Owner decisions (locked)

1. **No Play closed testing track.** Validation = **internal** (owner device(s) only).
   Whether the first Console upload is still **1.1.x** or already **1.2.0** depends on
   Google account validation timing — either is fine for internal smoke. **Open /
   production for strangers still requires 1.2.0 with ads** (do not soft-launch ad-free).
2. **`rc/2.0.0` opens after 1.2.0 is on Play production** (not in parallel with the
   monetization rush).
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
7. Play testing is **internal only** — do not assume a closed-test (12×14) requirement in plans.

---

## Next sequence (human + agents)

| # | Who | Action |
|---|-----|--------|
| 1 | Human | Finish Google Play account validation |
| 2 | Human | Create upload keystore locally; fill `keystore.properties` (never commit) |
| 3 | Repo | Privacy URL live (see `privacy-policy.md` + `08-play-store.md`) |
| 4 | Code | Implement **1.2.0** monetization per `09-monetization.md` |
| 5 | Human | Internal smoke on device → **production 1.2.0** (first stranger-facing build with ads) |
| 6 | Repo | Open `rc/2.0.0` **after** 1.2.0 is live on Play |

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
