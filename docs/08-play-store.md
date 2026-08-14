# Google Play Store — RackTrack

Publish **binaries via Play** (AAB). GitHub Releases stay **notes-only** (`LICENSE`).

**End-user / production gate:** version **1.2.0** with monetization
([`09-monetization.md`](09-monetization.md)). Do **not** soft-launch ad-free production
then add ads later. See [`00-release-trains.md`](00-release-trains.md).

**Testing:** **internal only** (owner device). No Play closed-testing track planned.
Internal smoke may use 1.1.x or 1.2.0 depending on Google account validation timing;
strangers still get production **1.2.0** with ads.

### Public identity (Personal account) — what users may see

Google distinguishes **private account contact** (for Google only) from **public** profile /
store fields. Typical Personal-account picture (confirm in Console / Help Center):

| Field | Usually public on Play? | Practical tip |
|-------|-------------------------|---------------|
| Support **email** (store listing) | **Yes** (required) | Use `mappm.pro@gmail.com` |
| Support **phone** (store listing) | **Only if you fill it** | Leave **empty** — no secondary number needed if the goal is just “no perso phone online” |
| Account contact phone (Google ↔ you) | **No** | Can stay your real number for OTP / Google |
| **Legal name** / **legal address** (Personal) | **May be shown**; with **monetization**, Google may show the **full address** | Expected trade-off of a Personal + monetized account |
| Developer display name | **Yes** | **MappM** on Play; GitHub remains Asarox33 |

If the goal is **no personally identifiable legal name/address** on the store at all, a
Personal account is the wrong tool — that is when a **micro-entreprise / société**
(Organization Play account) becomes relevant (different verification, public org details
instead of home address). That is a business choice, not required for 1.2.0 launch.

---

## Checklist (owner)

### Account
- [ ] Play Console developer account validated (Personal default; 25 USD one-time)
- [ ] 2FA on the Google account
- [ ] App created: `applicationId` **`com.racktrack`**, title RackTrack

### Signing (never commit secrets)
- [ ] Generate **upload** keystore locally (keep encrypted backup offline)
- [ ] Copy [`keystore.properties.example`](../keystore.properties.example) → `keystore.properties`
- [ ] `./gradlew :app:bundleRelease` produces a signed AAB when properties exist
- [ ] Enable **Play App Signing** on first upload (Google holds app signing key)

### Listing & policy
- [ ] Store listing (short/full description, screenshots, icon, feature graphic)
- [ ] Content rating questionnaire
- [ ] **Privacy policy URL** (HTTPS) — start from [`privacy-policy.md`](privacy-policy.md);
      publish via GitHub Pages or another host; **update before 1.2.0** for AdMob + Billing
- [ ] **Data safety** form (local-only until ads; then AdMob/Billing disclosures)
- [ ] Countries, pricing (**free** + optional IAP `remove_ads` ~4,99 €)

### Testing → production
- [ ] Internal install / smoke on owner device(s) (no closed-test track)
- [ ] Production release = **1.2.0** (ads + Remove Ads) — first build for strangers

---

## Repo support

| Item | Status |
|------|--------|
| `.gitignore` keystore / `keystore.properties` | required |
| `keystore.properties.example` | required |
| `app/build.gradle.kts` release signing if properties present, else debug fallback | required |
| `:app:bundleRelease` | local / CI with secrets |
| Fastlane / automated Play upload | optional later — manual upload OK for first ship |

---

## Data safety (sketch)

**Until ads (sideload / internal 1.1.x):** no accounts; match history on device; no analytics SDK.

**From 1.2.0 production:** disclose AdMob (ads) + Play Billing (purchase); UMP consent where
required; still no RackTrack backend accounts in 1.x.
