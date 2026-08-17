# Google Play Store — RackTrack

Publish **binaries via Play** (AAB). GitHub Releases stay **notes-only** (`LICENSE`).

**End-user / production gate:** version **1.2.0** with monetization
([`09-monetization.md`](09-monetization.md)). Do **not** soft-launch ad-free production
then add ads later. See [`00-release-trains.md`](00-release-trains.md).

**Testing:** **internal only** (owner device). No Play closed-testing track planned.
Internal smoke may use **1.1.1** (this doc) or 1.2.0 depending on timing;
strangers still get production **1.2.0** with ads.

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

### E. Production (later — not this train)

- First stranger-facing production = **1.2.0** with AdMob + Remove Ads IAP
- Update privacy + Data safety before that ship

---

## Repo support

| Item | Status |
|------|--------|
| `.gitignore` keystore / `keystore.properties` / `*.p12` | yes |
| `keystore.properties.example` | yes |
| `app/build.gradle.kts` release signing if properties present | yes |
| `:app:bundleRelease` local | yes |
| CI signed AAB artifact (`signed-aab.yml`) | yes — needs secrets |
| Fastlane / auto Play upload | **out of scope** for 1.1.1 |

---

## Data safety (sketch)

**Until ads (sideload / internal 1.1.x):** no accounts; match history on device; no analytics SDK; no shared data collected by RackTrack.

**From 1.2.0 production:** disclose AdMob (ads) + Play Billing (purchase); UMP consent where required; still no RackTrack backend accounts in 1.x.
