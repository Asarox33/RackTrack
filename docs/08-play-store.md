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

### B. GitHub secrets (CI signed AAB)

Repo → **Settings → Secrets and variables → Actions** — add:

| Secret | Value |
|--------|--------|
| `RACKTRACK_KEYSTORE_BASE64` | Base64 of `racktrack-upload.p12` |
| `RACKTRACK_KEYSTORE_PASSWORD` | store password |
| `RACKTRACK_KEY_ALIAS` | `racktrack` |
| `RACKTRACK_KEY_PASSWORD` | key password |

**Encode keystore (PowerShell):**

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$PWD\racktrack-upload.p12")) | Set-Clipboard
```

**Encode keystore (bash):**

```bash
base64 -w0 racktrack-upload.p12 | pbcopy   # or clip.exe / xclip
```

Workflow: [`.github/workflows/signed-aab.yml`](../.github/workflows/signed-aab.yml)  
Triggers: **`workflow_dispatch`** or tag **`v*`**.  
Output: **Actions** artifact `app-release-aab` only (private to the repo, expires in **14 days**).
That is intentional CI backup — **not** a public download.
**Never** attach an AAB/APK to a GitHub **Release** (notes-only / `LICENSE`).

Decode helper: [`.github/scripts/setup-upload-signing.sh`](../.github/scripts/setup-upload-signing.sh).

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

When drafting Play “notes de version”, always use locale tags (≤500 chars each):

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

- [ ] Create **Closed testing** release with current monetized **1.2.0** AAB
- [ ] Add ≥12 testers (email list); each must open the opt-in link and stay enrolled
- [ ] Wait ≥14 consecutive days with ≥12 still enrolled → request production access

**Console — update before / with production release**

- [ ] **Annonces** → Oui (Contains ads)
- [ ] **Data safety** → Oui + disclose AdMob + Play Billing (was Non / local-only on 1.1.1)
- [ ] **Privacy policy** URL still live; expand text if needed for ads / Billing
- [ ] **App access / login** — still Non unless something is truly gated (optional IAP ≠ gate)
- [ ] **Content rating** — re-check “contenu en ligne” if AdMob changes answers
- [ ] **Play Billing** products (`remove_ads`) + Console Billing protection as needed
- [ ] **Protégé avec Play** — device integrity **recommandé** (was Aucun on Internal);
      Play Integrity API in-app only if product asks
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
  (native symbols packaged in the AAB for Play)
- [ ] Upload AAB; confirm Play accepts mapping (from
  `app/build/outputs/mapping/release/mapping.txt` if Console asks — often paired with the
  release) + native symbols from the bundle
  (Internal 1.1.1 Console warnings were deferred on purpose — see `06-roadmap-todo.md`)

**Code** — full UX: [`09-monetization.md`](09-monetization.md).

---

## Repo support

| Item | Status |
|------|--------|
| `.gitignore` keystore / `keystore.properties` / `*.p12` | yes |
| `keystore.properties.example` | yes |
| `app/build.gradle.kts` release signing if properties present | yes |
| `:app:bundleRelease` local | yes |
| CI signed AAB artifact (`signed-aab.yml`) | yes — needs secrets |
| Fastlane / auto Play upload | **out of scope** for 1.1.1 — see backlog below |

### Later — CI upload to Play Internal (not built yet)

Today `signed-aab.yml` only uploads a **GitHub Actions artifact**. To publish the AAB
straight to **Internal testing** from CI, still need:

1. Google Cloud project linked to Play + **Android Publisher API** enabled
2. Service account invited in Play Console (Users & permissions) with release rights
   on the Internal track
3. GitHub secret for SA JSON (or Workload Identity Federation)
4. Workflow step after `bundleRelease` (e.g. `r0adkll/upload-google-play` or Fastlane
   `supply`) — `packageName: com.racktrack`, `track: internal`, release notes
   (`<fr-FR>` / `<en-US>`)

Prereq already met: upload keystore secrets + first manual Internal release.
Defer until owner wants less manual Internal shipping (can wait for **1.2.0** cadence).

---

## Data safety (sketch)

**Until ads (sideload / internal 1.1.x):** no accounts; match history on device; no analytics SDK; no shared data collected by RackTrack.

**From 1.2.0 production:** disclose AdMob (ads) + Play Billing (purchase); UMP consent where required; still no RackTrack backend accounts in 1.x.
