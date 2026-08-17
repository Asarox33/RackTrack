# Versioning & releases — RackTrack

## Version source of truth

Product version lives in [`gradle.properties`](../gradle.properties):

```properties
racktrack.versionName=1.2.0
racktrack.versionCode=10201
```

- **versionName** — classic semver `X.Y.Z` (users + GitHub Releases).
- **versionCode** — Android integer, always strictly increasing for Play uploads.

**Formula (from 1.2.1 onward):** `major*100000 + minor*1000 + patch*10`

| versionName | Base versionCode | Notes |
|-------------|------------------|-------|
| `1.2.1` | `102010` | First AAB for that name |
| Internal rebuilds of `1.2.1` | `102011`…`102019` | Same name, new Play upload |
| `1.2.10` | `102100` | Patch 10 — no clash with `1.2.1` |
| `1.2.99` | `102990` | Soft cap: patch ≤ 99, minor ≤ 99 |
| `1.3.0` | `103000` | Next minor |

Room for 99 patches and 99 minors per major; Play only cares that each upload’s
`versionCode` is **strictly greater** than the previous one.

**1.2.0 exception (already on Play):** used the short range `10200`–`10209` for the
first monetization Internal smokes. Do not reuse those numbers; next user-facing bump
is `1.2.1` → `102010` (or keep climbing `10202`… only while still on name `1.2.0`).

Older 1.0.x / 1.1.x used `major*10000 + minor*100 + patch`. Do not rewrite history.

Always bump `versionCode` for every Play AAB; bump `versionName` only for a user-facing
release (and add `CHANGELOG.md`).

## Semver meaning

| Bump | When |
|------|------|
| **MAJOR** (`X.0.0`) | Breaking change to saved data / match model, or intentional new product generation |
| **MINOR** (`x.Y.0`) | New user-facing capability without breaking local data |
| **PATCH** (`x.y.Z`) | Bug fix / polish shipped in an APK |

**1.0.0** is the first table-ready product release (MVP phase closed).

## Release notes (`CHANGELOG.md`)

User-facing notes live in [`CHANGELOG.md`](../CHANGELOG.md).

For every version bump PR:

1. Add a top section `## X.Y.Z — YYYY-MM-DD` with at least:
   - `### Features` and/or
   - `### Bug fixes`
2. Keep wording short and product-facing (what players notice).
3. Merge only when that section exists — CI **fails the release cut** if it is missing.

Workflows `cut-release.yml` and `release.yml` run
`.github/scripts/extract-changelog.sh X.Y.Z` and paste the section into the
**GitHub Release** body (Releases list on the repo). Releases are **notes-only** —
no APK/AAB asset is attached (binaries go to app stores).

## How a release is produced

1. Open a PR that bumps `racktrack.versionName` + `racktrack.versionCode` **and** updates
   `CHANGELOG.md` for that version.
2. Merge to **`main`** after CI is green.
3. Workflow **Cut release from main** (`cut-release.yml`) runs after **CI** succeeds on `main`.
   If GitHub Release `v{versionName}` does not exist, it:
   - re-checks quality gates
   - creates annotated tag + GitHub Release with changelog notes (**no APK**)
4. Workflow **Release** (`release.yml`) also runs on manual `vX.Y.Z` tag pushes
   (admins / ruleset bypass). It no-ops if the release already exists.

Prefer version bump → merge → automatic cut. Do not invent tags that disagree with
`gradle.properties`.

## Protected tags

Repository ruleset **Protect semver tags** targets `refs/tags/v*`:

- Restrict **update**, **deletion**, and **non-fast-forward** (tags stay immutable once cut)
- Bypass: repository **admins**
- Creation is left open so GitHub Actions can publish via `gh release create`
  (personal repos cannot add the Actions app as a ruleset bypass actor). Prefer the
  automatic cut path; do not hand-push random `v*` tags.

Recreate if missing:

```bash
gh api --method POST repos/{owner}/{repo}/rulesets \
  --input .github/tag-ruleset.json
```

Optional hardening: add a `creation` rule and store an admin `RELEASE_PAT` secret used by
`cut-release.yml` / `release.yml` instead of `GITHUB_TOKEN`.

## Signing note

CI does not publish binaries. Store builds use a dedicated upload keystore via local
`keystore.properties` (see `keystore.properties.example` + `docs/08-play-store.md`).
Without that file, `assembleRelease` / `bundleRelease` still falls back to the **debug**
keystore for sideload testing only.

## Play end-user gate

Closed testing is **not** used (owner **internal** smoke only). The **first open /
production** Play release aimed at end users is **1.2.0** and must ship with monetization
as specified in `docs/09-monetization.md`. See `docs/00-release-trains.md`.
