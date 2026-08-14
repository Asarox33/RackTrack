# Google Play Store — RackTrack

Publish **binaries via Play** (AAB). GitHub Releases stay **notes-only** (`LICENSE`).

**End-user / production gate:** version **1.2.0** with monetization
([`09-monetization.md`](09-monetization.md)). Do **not** soft-launch ad-free production
then add ads later. See [`00-release-trains.md`](00-release-trains.md).

**Testing:** **internal only** (owner device). No Play closed-testing track planned.
Internal smoke may use 1.1.x or 1.2.0 depending on Google account validation timing;
strangers still get production **1.2.0** with ads.

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
