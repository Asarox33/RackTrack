# Locked release trains — RackTrack

**Status:** locked 2026-08-15 · keep this file in sync when trains change.  
**Agents:** read this **before** inventing version numbers or dropping backlog items.
Canonical backlog detail: [`06-roadmap-todo.md`](06-roadmap-todo.md).

---

## Trains at a glance

| Train | Channel | Contents |
|-------|---------|----------|
| **1.0.x – 1.1.0** | GitHub notes + sideload / closed test OK | Table product; **1.1.0** = solo + PDF SAVE/footer + Golden felt |
| **1.2.0** | **First open / production Play** for strangers | Monetization (`09-monetization.md`) + upload keystore + privacy/Data safety for ads |
| **1.2.x / 1.3.x** | Play updates after launch | Hotfixes; optional FFB player timeouts |
| **2.0.0** on `rc/2.0.0` | Merge to `main` only when ready | i18n, multi-ruleset, KMP/CMP/iOS, optional accounts |

```text
main (1.x patchable)
  │
  ├─ 1.1.0 ✅ shipped (GitHub)
  ├─ 1.2.0 ⬜ Play end-user gate (ads + IAP)   ← next product code
  └─ …
       │
       └── branch rc/2.0.0 (open after 1.2.0 is stable enough)
                 → 2.0.0 merge when ready
```

---

## Hard product gates

1. **Do not** publish an **ad-free** open/production Play build, then add ads later.
2. **Do not** put i18n / multi-ruleset / KMP on the **1.2.0** Play launch critical path.
3. **Do not** silently delete backlog lines from `06-roadmap-todo.md` when editing it —
   move to ✅ / Explicitly dropped / another train with a progress note.
4. Pool **domain** never imports AdMob / Billing / UMP (`09-monetization.md` §9).
5. Official binaries = **stores** (or local build). GitHub Releases = **notes only** (`LICENSE`).

---

## Next sequence (human + agents)

| # | Who | Action |
|---|-----|--------|
| 1 | Human | Finish Google Play account validation |
| 2 | Human | Create upload keystore locally; fill `keystore.properties` (never commit) |
| 3 | Repo | Privacy URL live (see `privacy-policy.md` + `08-play-store.md`) |
| 4 | Code | Implement **1.2.0** monetization per `09-monetization.md` |
| 5 | Human | Closed test → production **1.2.0** (first stranger-facing build with ads) |
| 6 | Repo | Open `rc/2.0.0` for i18n / ruleset / KMP (can start after 1.2.0 coded or live) |

Open questions for the owner (answer in PR / issue, then update this file):

- Closed testing: allow **1.1.x without ads**, or only **1.2.0 with ads** in the closed track?
- Open `rc/2.0.0` **in parallel** with 1.2.0 coding, or **only after** 1.2.0 is on production?
- FFB **player timeouts** (1.2.16): target **1.3.0** after Play, or defer to post-2.0?

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
