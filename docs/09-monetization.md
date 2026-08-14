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
- Play **Data safety** + privacy policy updated for AdMob + Billing **before** production.
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

## 9. Implementation sketch (non-binding)

Suggested seams (keep domain pure):

- `presentation` / thin `ads` helper: preload, show-or-skip, cooldown timestamp in prefs.
- `billing` helper: query/purchase/restore `remove_ads`; persist owned flag locally +
  re-query on launch.
- Setup start button: call gate → then existing `startMatch` / training path.

Unit-test the cooldown / skip decision tree with fake clock + fake “loaded” / “owned”
flags; do not unit-test AdMob/Billing SDKs.
