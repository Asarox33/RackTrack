# Versioning & releases — RackTrack

## Version source of truth

Product version lives in [`gradle.properties`](../gradle.properties):

```properties
racktrack.versionName=1.0.0
racktrack.versionCode=10000
```

- **versionName** — classic semver `X.Y.Z` (users + GitHub Releases).
- **versionCode** — Android integer: `major*10000 + minor*100 + patch`
  (e.g. `1.2.3` → `10203`). Always bump together with versionName.

The app module reads these properties in `app/build.gradle.kts`.

## Semver meaning

| Bump | When |
|------|------|
| **MAJOR** (`X.0.0`) | Breaking change to saved data / match model, or intentional new product generation |
| **MINOR** (`x.Y.0`) | New user-facing capability without breaking local data |
| **PATCH** (`x.y.Z`) | Bug fix / polish shipped in an APK |

**1.0.0** is the first table-ready product release (MVP phase closed).

## How a release is produced

1. Open a PR that bumps `racktrack.versionName` + `racktrack.versionCode` when you intend to ship.
2. Merge to **`main`** after CI is green.
3. Workflow **Cut release from main** (`cut-release.yml`) runs after **CI** succeeds on `main`.
   If GitHub Release `v{versionName}` does not exist, it:
   - re-checks quality gates
   - builds `assembleRelease`
   - creates annotated tag + GitHub Release with `RackTrack-{version}.apk`
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

Release APKs on GitHub Releases use the **debug keystore** until a dedicated upload
keystore exists. Fine for club / sideload; not for Play Store.
