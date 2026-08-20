# Monetization — RackTrack

**Status:** product decision locked · implementation target **1.2.0** (first open /
production Play release to end users). See `docs/06-roadmap-todo.md`.

**Authority for placement & UX:** this document. Do not invent banners, mid-rack ads,
subscriptions, or “ad loading” wait screens.

---

## 1. Goals

- Fund a **free** store app with rare, non-intrusive ads.
- Offer a **one-time** purchase to remove ads forever.
- Keep the live scoreboard looking like a **serious sports tool**, not an ad-supported toy.
- Never block match start on ad availability.

---

## 2. Ad format

| Use | Do not use |
|-----|------------|
| **AdMob interstitial** (full-screen) | Banner ads |
| | Native / rewarded (unless product revisits later) |
| | Mid-game overlay on the board |

**Why no banners**

- No layout changes on setup or boards.
- The match dashboard must stay 100% dedicated to scoring.
- Banners look cheap, interrupt the felt UI, and pay poorly for this use case.

---

## 3. When to show

### 3.1 Entry point

The app’s main screen is **match setup** (mode, players, race/distance, rules, options).

Current flow:

```text
Setup → START MATCH / START TRAINING → Match board
```

Desired flow:

```text
Setup → START MATCH / START TRAINING → (maybe interstitial) → Match board
```

- Trigger: after a successful click on **START MATCH** or **START TRAINING** (same gate).
- Show the interstitial **before** navigating to the board.
- **Do not** add a dedicated intermediate “ad screen” in the app UI — only the AdMob
  interstitial system UI (or skip straight to the board).

Same rule for duel and solo: both start buttons share one ad + cooldown pipeline.

### 3.2 Why this moment

- Previous match (if any) is already over.
- User is deliberately starting a new session.
- No in-play action is interrupted.
- Players often prepare the rack / balls while a short interstitial can run.

### 3.3 Never show ads

- During a rack / visit / inning.
- Between racks on the live board.
- On the match dashboard / score UI.
- On summary, history, settings, or pause UI.
- Mid-gesture / mid-action on any scoring control.

---

## 4. Cooldown (5 minutes)

**At most one interstitial every 5 minutes.**

- The cooldown timer **starts when an interstitial is actually shown** (impression),
  not when the user taps Start.
- No hourly / session hard caps beyond this rule.
- If the user waits ≥ 5 minutes between starts, another interstitial may show.

### 4.1 Cases this protects

- User starts 8-ball, backs out, switches to 9-ball, starts again immediately.
- Very short matches (golden break, quick races) → rapid restarts.
- Solo training with many short sessions.

### 4.2 Decision tree

```text
START MATCH / START TRAINING
        ↓
  ads removed by IAP?
        ├── YES → board immediately
        └── NO
              ↓
        last interstitial shown < 5 minutes ago?
              ├── YES → board immediately
              └── NO
                    ↓
              interstitial loaded & ready?
                    ├── NO  → board immediately
                    └── YES → show interstitial → on dismiss/fail-safe → board
```

### 4.3 UX of the cooldown

- Cooldown is **internal only**.
- **Never** show a countdown, “next ad in…”, or any message about ad timing.

---

## 5. Ads must never block match start

Ads are optional. Match start is mandatory.

```text
if (canShowAds && interstitialIsLoaded) {
    showInterstitialThenStartMatch()
} else {
    startMatchImmediately()
}
```

- Load failure, no fill, timeout, or consent not granted for personalized ads (where
  policy requires skip) → **go to board**.
- Never spin / wait solely because an ad is still loading.
- After interstitial close (or error callback) → **always** land on the match board for
  that start intent (unless the user cancelled the start path — prefer completing start).

Preload the next interstitial in the background after a show (or on setup appear) so the
“loaded?” check is usually cheap; still never block if not ready.

---

## 6. Remove Ads — one-time IAP

| Field | Value |
|-------|--------|
| Model | One-time unlock (not subscription) |
| Suggested store price | **4,99 €** (adjust per store country if needed) |
| Product id (working name) | `remove_ads` |
| Effect | No interstitials forever on that Play / App Store entitlement |
| Essential features | **Never** locked behind paywall — only ads removed |

### 6.1 Platforms

- **1.2.0 (Android):** Google Play Billing Library.
- **Later (iOS / KMP):** same product concept, App Store non-consumable; shared
  “ads removed” preference / entitlement abstraction.

Purchases must be **restorable** after reinstall / new device (Play + App Store restore
APIs). Settings must expose:

- **Remove ads** (buy / already owned)
- **Restore purchases**

### 6.2 What Remove Ads is not

- Not a monthly / yearly subscription.
- Not a feature pack (modes, PDF, history stay free).
- Not required to use the scoreboard.

---

## 7. Consent & store compliance (Android 1.2.0)

- Google **UMP** (or equivalent) for EEA/UK consent before ad requests as required.
- Play **Data safety** + privacy policy updated for AdMob + Billing ✅ (2026-08-20);
  Production promote still waits on Closed 12×14.
- First **open / production** end-user build already includes this stack (no ad-free
  soft launch then ads later — trust risk). Closed testing may use pre-monetization builds.

---

## 8. Out of scope (for 1.2.0)

- Banner / rewarded / native ads.
- Ads on summary share, history open, or settings.
- Artificial “watch ad for bonus” flows.
- Server-side ad mediation beyond AdMob defaults (unless needed later).
- Cross-promo interstitial of our own inventing.

---

## 9. Architecture (locked)

Ads and billing stay **outside** the pool engines. `FourteenOneEngine` / `MatchEngine` /
`MatchCoordinator` domain logic must not import AdMob, Play Billing, or UMP.

### 9.1 Components

| Component | Role |
|-----------|------|
| **`RemoveAdsStore`** (name flexible) | Owns Premium entitlement: purchase, restore, local cache, re-query on launch. No AdMob. |
| **`InterstitialAdManager`** | Load interstitial, 5‑min cooldown, show-or-skip, callbacks on dismiss/fail. Asks `RemoveAdsStore` before any ad work. |
| **Setup / start path** | Calls a single gate (e.g. `runWithOptionalInterstitial { startMatch… }`) then existing coordinator start. |

Preferred package (Android app module only), e.g.:

```text
com.racktrack.monetization/
  InterstitialAdManager.kt   # AdMob interstitial + cooldown
  RemoveAdsStore.kt          # Play Billing remove_ads + restore
  MonetizationGate.kt        # glue: premium? → skip; else maybe show ad → then action
```

No Hilt required unless we later introduce DI project-wide — constructor injection from
`MainActivity` / ViewModel factory is enough for 1.2.0.

### 9.2 `InterstitialAdManager` responsibilities

- Preload an interstitial (setup appear / after previous show).
- Decide whether one **may** be shown (not Premium, cooldown elapsed, ad loaded).
- Show the interstitial when appropriate.
- Notify the app when the ad is **closed** or **fails** so the pending start can continue.
- **Bypass immediately** if not loaded / no fill / error.
- **Do nothing** (no load, no show) when the user owns Remove Ads — short-circuit via
  `RemoveAdsStore` (or the gate checks Premium first and never calls the manager).

Cooldown clock and “last shown” timestamp live here (or in prefs owned by this manager),
not in domain.

### 9.3 Start-match flow

UI / ViewModel keeps a simple intent:

```text
onStartMatchClicked()
  → MonetizationGate.runAfterAdOpportunity {
        matchViewModel.startMatch(...)   // or start training — unchanged domain call
     }
```

The gate decides ad vs skip; the billiard start API stays “just start”. Domains and
boards never know why there was a delay.

```text
Setup  →  Gate  →  (optional AdMob UI)  →  existing startMatch / board
              ↘________________↗
                 skip / Premium
```

### 9.4 Premium (Remove Ads owned)

```text
START MATCH / START TRAINING → board immediately
```

- No interstitial load attempt.
- No show attempt.
- No cooldown bookkeeping required for that tap.
- Entitlement must survive reinstall / new device via **Restore purchases** + store
  re-query on cold start (Play Billing; later App Store for iOS).

Settings: **Remove ads** (buy / owned) + **Restore purchases** talk only to
`RemoveAdsStore`, which notifies the gate / ad manager (e.g. clear loaded ad, stop preload).

### 9.5 Tests

- Unit-test the **gate / cooldown / Premium skip** decision tree with fakes
  (`adsRemoved`, `lastShownAt`, `isLoaded`, fake clock).
- Do **not** unit-test the AdMob or Play Billing SDKs themselves.

---

## 10. Implementation checklist (1.2.0)

- [x] `RemoveAdsStore` + Settings buy / restore / owned UI
- [x] `InterstitialAdManager` + preload + 5‑min cooldown
- [x] `MonetizationGate` wired on START MATCH / START TRAINING only
- [x] UMP consent before first ad request
- [x] Privacy / Data safety / listing updated (**promote checklist:** `08-play-store.md` §E —
  declarations done 2026-08-20; Production promote still waits on Closed 12×14)
- [x] R8 minify + shrink + native `SYMBOL_TABLE` in release AAB (mapping at
  `app/build/outputs/mapping/release/`); verify on next Play upload
  (deferred from Internal 1.1.1 warnings; do before strangers see 1.2.0)
- [x] Gate unit tests (Premium, cooldown, not loaded → immediate start)
- [x] Play Billing product `remove_ads` Active (~4,99 € HT / TTC per store)
- [ ] **AdMob real interstitial** — after Production listing is linkable: associate Play in
  AdMob → review → switch `gradle.properties` off Google SAMPLE onto
  `ca-app-pub-5352846919427815/1720927691` (see `08-play-store.md` §E “After Production”)
