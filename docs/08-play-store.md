# Google Play Store — RackTrack

Publish **binaries via Play** (AAB). GitHub Releases stay **notes-only** (`LICENSE`).

**End-user / production gate:** version **1.2.0** with monetization
([`09-monetization.md`](09-monetization.md)). Do **not** soft-launch ad-free production
then add ads later. See [`00-release-trains.md`](00-release-trains.md).

**Testing:** **Internal** = owner smoke. **Closed** = mandatory before production on this
account (Google: ≥**12** closed testers opted in ≥**14** consecutive days + Console
questionnaire). Internal alone does **not** unlock Production. Put monetized **1.2.0** on
Closed while waiting; strangers only after production access.

---

## Privacy policy URL (HTTPS)

Source: [`privacy-policy.md`](privacy-policy.md) (Jekyll site under `docs/`).

| Step | Action |
|------|--------|
| 1 | Merge scaffolding that adds `docs/_config.yml`, `docs/index.md`, privacy front matter |
| 2 | Repo **Settings → Pages** → Source: **Deploy from a branch** → Branch **`main`** / folder **`/docs`** |
| 3 | Wait for the Pages deploy; open the URL below |

**Canonical URL (after Pages is live):**

`https://asarox33.github.io/RackTrack/privacy-policy.html`

Also: `https://asarox33.github.io/RackTrack/` (index with link).

Paste that privacy URL into Play Console → App content → Privacy policy.

---

## Checklist — Internal testing 1.1.1

### A. Local upload keystore (once)

- [x] Generate upload keystore (PKCS12 preferred): `racktrack-upload.p12`, alias `racktrack`
- [x] Root `keystore.properties` (gitignored) from [`keystore.properties.example`](../keystore.properties.example)
- [x] Offline backup of passwords (`keystore.BACKUP.local.txt` — gitignored; keep encrypted elsewhere)
- [x] `./gradlew :app:bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`
- [ ] Optional regen helper: `pwsh scripts/generate-upload-keystore.ps1` (**only if you lost the key** — new upload key breaks Play continuity)

**Never commit** `*.p12` / `*.jks` / `keystore.properties` / `keystore.BACKUP.local.txt`.

### B. GitHub secrets (signing + Play Internal)

Repo → **Settings → Secrets and variables → Actions** — add:

| Secret | Value |
|--------|--------|
| `RACKTRACK_KEYSTORE_BASE64` | Base64 of `racktrack-upload.p12` |
| `RACKTRACK_KEYSTORE_PASSWORD` | store password |
| `RACKTRACK_KEY_ALIAS` | `racktrack` |
| `RACKTRACK_KEY_PASSWORD` | key password |
| `RACKTRACK_PLAY_SERVICE_ACCOUNT_JSON` | Full Play publisher SA JSON (see CI → Play Internal below) |

**Encode keystore (PowerShell):**

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$PWD\racktrack-upload.p12")) | Set-Clipboard
```

**Encode keystore (bash):**

```bash
base64 -w0 racktrack-upload.p12 | pbcopy   # or clip.exe / xclip
```

Workflow **Signed release AAB** ([`signed-aab.yml`](../.github/workflows/signed-aab.yml)):
**`workflow_dispatch` only** — GitHub artifact `app-release-aab` (backup; not a public download).

Workflow **Play Internal** ([`play-internal.yml`](../.github/workflows/play-internal.yml)):
auto after green CI on `main` when `versionCode` bumps (see below).

Decode helper: [`.github/scripts/setup-upload-signing.sh`](../.github/scripts/setup-upload-signing.sh).
**Never** attach an AAB/APK to a GitHub **Release** (notes-only / `LICENSE`).

### C. Play Console — create app + listing (owner)

1. [Play Console](https://play.google.com/console) → **Create app**
   - Name: **RackTrack**
   - App (not game) · Free · complete base declarations
   - Package / `applicationId`: **`com.racktrack`** (must match Gradle)
2. Enable **Play App Signing** on first AAB upload (Google holds app signing key; you keep upload key).
3. Minimum for Internal testing (Console will guide):
   - Store listing basics: title, short/full description, icon (`assets/` / `mipmap`)
   - Content rating questionnaire
   - **Data safety**: local-only for 1.1.1 (no accounts, no ads) — see sketch below
   - **Privacy policy URL**: `https://asarox33.github.io/RackTrack/privacy-policy.html`
   - Countries / availability as needed for Internal
4. Support contact on listing: **`mappm.pro@gmail.com`** · phone **empty**

### D. Internal testing upload (owner)

1. **Testing → Internal testing** → create release
2. Upload AAB: local `app/build/outputs/bundle/release/app-release.aab`  
   Version **1.1.1** / `versionCode` **10101**
3. Testers: add your Google account email
4. Publish the Internal release → open opt-in link on the phone → install from Play
5. Smoke (device):
   - About shows **1.1.1**, OFL font credits
   - Setup → match / solo → summary SHARE/SAVE
   - No ads (expected on 1.1.1)

### Release notes format (owner preference)

When drafting Play “notes de version”, always use locale tags (≤500 chars each).
Keep wording **track-agnostic** (no “test interne” / “internal test”) — notes reuse on
Closed and Production.

```text
<fr-FR>
…
</fr-FR>
<en-US>
…
</en-US>
```

### E. Production 1.2.0 — after Closed 12×14 (do not forget)

First stranger-facing production = **1.2.0** with AdMob + Remove Ads IAP.
**Prerequisite:** Closed testing track with ≥12 opted-in testers for ≥14 consecutive days,
then answer Play’s production-access questionnaire (Internal does not count).

Listing / icons / screenshots / category from Internal **1.1.1** mostly carry over —
revisit the declarations below when promoting the monetized build (before **2.0.0** /
`rc/2.0.0`).

**Closed track (start the clock now)**

- [x] Create **Closed testing** release with monetized **1.2.1** AAB (`102012`) —
  live 2026-08-20
- [x] ≥12 testers opted in (2026-08-20)
- [ ] Wait ≥14 consecutive days with ≥12 still enrolled → request production access
  (**clock started 2026-08-20** → earliest ~**2026-09-03** if count holds)

**Console — declarations done 2026-08-20 (owner); promote still waits on 12×14**

- [x] **Annonces** → Oui (Contains ads)
- [x] **Data safety** → Oui + disclose AdMob + Play Billing (was Non / local-only on 1.1.1)
- [x] **Privacy policy** URL still live; text covers ads / Billing
- [x] **App access / login** — Non (optional IAP ≠ gate)
- [x] **Content rating** — re-checked for online / AdMob content
- [x] **Play Billing** products (`remove_ads`) + Console Billing protection as needed
- [x] **Protégé avec Play** — device integrity **recommandé**
      (Play Integrity API in-app only if product asks later)
- [ ] Promote → **Production** once access granted; countries / pricing free + IAP
  (**OK to ship first with Google SAMPLE interstitial** in `gradle.properties` — gate
  already skips when no fill; do **not** leave sample IDs once AdMob serves)

**After Production is live — AdMob real unit (patch)**

1. AdMob → app RackTrack → **lier la fiche Google Play** (`com.racktrack`)
2. Demander / attendre l’**examen** AdMob jusqu’à fill réel
3. Patch `gradle.properties`: set `racktrack.admobInterstitialUnitId` to the real unit
   `ca-app-pub-5352846919427815/1720927691` (sample id out)
4. Bump `versionCode` (1.2.0 smoke or **1.2.1**) + Play release (Closed or Production)
5. Smoke: interstitial on Start for a non‑Remove‑ads install

**Build / crash quality (with monetization AAB)**

- [x] Enable **R8** minify + resource shrink; `ndk.debugSymbolLevel = SYMBOL_TABLE`
  (mapping embeds in the AAB as `BUNDLE-METADATA/.../proguard.map` — Console auto-detects)
- [x] Upload AAB; confirm **Fichier de mappage ReTrace** has a size in App bundle explorer
- [x] **Symboles de débogage natifs** — intentionally empty (AGP does not produce a usable
  symbols zip; third-party `.so` are pre-stripped). Revisit only if we ship our own NDK.

**Code** — full UX: [`09-monetization.md`](09-monetization.md).

---

## Repo support

| Item | Status |
|------|--------|
| `.gitignore` keystore / `keystore.properties` / `*.p12` | yes |
| `keystore.properties.example` | yes |
| `app/build.gradle.kts` release signing if properties present | yes |
| `:app:bundleRelease` local | yes |
| CI signed AAB artifact (`signed-aab.yml`) | yes — `workflow_dispatch` only |
| **CI → Play Internal** (`play-internal.yml`) | ✅ live — first auto upload **102015** (2026-08-20) |

### CI → Play Internal (auto)

After **CI** succeeds on a push to **`main`**, workflow **Play Internal** builds a signed AAB and
publishes it to the **Internal testing** track (`status: completed`) when
`racktrack.versionCode` changed vs the parent commit. Uses action input `tracks: internal`
(`track` is deprecated on `r0adkll/upload-google-play`).

**Also:** Actions → **Play Internal** → **Run workflow** (manual smoke; always uploads current
`versionCode`).

**Release notes:** edit [`play/internal-release-notes.txt`](../play/internal-release-notes.txt)
(`<fr-FR>` / `<en-US>`, ≤500 chars each) in the same PR as the `versionCode` bump. CI extracts
`whatsnew-*` files for the Publisher API. Keep copy **track-agnostic** (no “test interne” /
“internal test”) — the same notes promote to Closed then Production.

#### Owner checklist (one-time) — service account — ✅ done 2026-08-20

Without secret `RACKTRACK_PLAY_SERVICE_ACCOUNT_JSON`, auto runs **skip** (CI stays green).
Manual dispatch **fails** until the secret exists. (Secret + invite + smoke completed.)

1. **Google Cloud — API + service account** ✅
   1. Open [Google Cloud Console](https://console.cloud.google.com/) (same Google account as Play if possible).
   2. Create or pick a project (e.g. `racktrack-play-ci`).
   3. **APIs & Services → Library** → **Google Play Android Developer API** → **Enable**.
   4. **IAM & Admin → Service accounts** → **Create service account**
      - Name: `racktrack-play-publisher` (or similar)
      - No GCP project roles required for Play upload (auth is via Play Console invite).
   5. Open the SA → **Keys** → **Add key** → **Create new key** → **JSON** → download.
   6. **Never commit** that JSON (`racktrack-play-ci*.json` is gitignored).

2. **Play Console — invite the SA** ✅
   1. [Play Console](https://play.google.com/console) → **Users and permissions**.
   2. **Invite new users** → SA email (`…@….iam.gserviceaccount.com` from the JSON / SA page).
   3. App **RackTrack** (`com.racktrack`): rights to release on **Internal** (and read app if asked).
      Prefer not granting full account Admin.
   4. Confirm invite; wait a few minutes for propagation.

3. **GitHub — secret** ✅
   1. Repo → **Settings → Secrets and variables → Actions**.
   2. **New repository secret** `RACKTRACK_PLAY_SERVICE_ACCOUNT_JSON` = full JSON file contents.
   3. Signing secrets already required (unchanged):
      `RACKTRACK_KEYSTORE_BASE64`, `RACKTRACK_KEYSTORE_PASSWORD`,
      `RACKTRACK_KEY_ALIAS`, `RACKTRACK_KEY_PASSWORD`.

4. **Smoke test** ✅ — merge bumped `versionCode` **102015** → Internal `completed` release.

**Order (reference):** Enable API → Create SA + JSON → Invite in Play → Add GitHub secret → smoke.

**Out of scope:** auto Closed / Production (manual only).

---

## Data safety (sketch)

**Until ads (sideload / internal 1.1.x):** no accounts; match history on device; no analytics SDK; no shared data collected by RackTrack.

**From 1.2.0 production:** disclose AdMob (ads) + Play Billing (purchase); UMP consent where required; still no RackTrack backend accounts in 1.x.
