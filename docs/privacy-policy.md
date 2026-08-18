---
title: Privacy policy
permalink: /privacy-policy.html
---

# Privacy policy — RackTrack

**Last updated:** 2026-08-18  
**Publisher:** MappM  
**Contact:** [mappm.pro@gmail.com](mailto:mappm.pro@gmail.com)  
**Source:** [github.com/Asarox33/RackTrack](https://github.com/Asarox33/RackTrack)

This is the privacy notice for the RackTrack Android app on Google Play (and any other
authorized store build published by MappM).

## Who we are

RackTrack is an American-pool **race / 14.1 scoreboard** published by **MappM**. Scoring
runs on your phone; there is **no RackTrack account** and **no RackTrack backend** that
receives your live match scores in version 1.x.

## Data stored on your device (local only)

The app keeps on **your device** (not on MappM servers):

- Setup preferences (felt, defaults, keep screen on, haptics, etc.)
- Completed match history / summaries (`match_history.json` and related local files)
- A local flag that ads were removed after a successful purchase (also re-checked with
  Google Play on launch / Restore)

Uninstalling the app deletes this local data. There is no MappM cloud copy to erase.

## What MappM does not do

- We do not operate a RackTrack server that collects your match scores for live play.
- We do not sell your personal data.
- We do not require an email or password to use the scoreboard.
- We do not use analytics SDKs such as Firebase Analytics or Crashlytics in 1.x.

Google Play may still show crash / ANR statistics to the developer through Play Console
(platform vitals). That is handled by Google Play, not by a RackTrack telemetry SDK.

## Advertising (AdMob) — store builds from 1.2.0

Official store builds may show a **full-screen interstitial** when you tap
**Start match / Start training** (not during live scoring on the board). Ads are served by
**Google AdMob**.

In connection with ads, Google may process data such as:

- **Device or other identifiers** (including the advertising ID, where available)
- Standard ad-serving / measurement information described in
  [Google’s privacy policy](https://policies.google.com/privacy) and AdMob documentation

**Purposes:** advertising and marketing (serving and measuring ads).  
**Sharing:** with Google (AdMob) as part of ad delivery.

Where required (e.g. EEA / UK), a **consent form (UMP)** may appear before personalized
ads are requested. You can also limit ad personalization in your device / Google settings.

Ads are skipped if you purchased **Remove ads**, if an ad fails to load, or during the
short cooldown after a shown ad — starting a match is never blocked waiting for an ad.

## In-app purchases (Google Play Billing)

**Remove ads** is an optional **one-time** unlock (~store list price; tax may apply by
country). Payment and entitlements are processed by **Google Play**.

In connection with purchases, Google Play may process **purchase history** (and related
billing data) so the app can grant and **restore** the unlock. MappM does not receive your
card number.

**Purposes:** app functionality (remove ads / restore).  
**Sharing:** with Google Play as required to process the purchase.

## Network permissions

When monetization is present, the app uses network access for AdMob, Play Billing, and
consent (UMP). Haptics / keep-screen-on only apply if you enable those settings.

## Children

RackTrack is a general sports utility. Follow the age rating shown on Google Play and
applicable children’s privacy rules. Do not use the app in ways that violate those rules.

## Your choices

- **Ads:** consent form where required; device ad settings; or buy **Remove ads**.
- **Purchases:** manage / restore via Google Play; Settings → Restore in the app.
- **Local history:** delete matches in-app, or uninstall the app to clear local storage.
- **Questions / requests:** email [mappm.pro@gmail.com](mailto:mappm.pro@gmail.com).

Because there is no RackTrack account and no MappM-hosted personal profile, we do not
operate a separate “delete my cloud account” flow. Play / AdMob data held by Google is
subject to Google’s controls and policies.

## Data safety summary (Play)

Aligned with the Play Data safety form for monetized builds:

| Data | Collected / shared | Main purpose |
|------|--------------------|--------------|
| Device or other IDs (e.g. advertising ID) | Yes / shared with Google (AdMob) | Advertising |
| Purchase history | Yes / shared with Google Play | App functionality |
| Match history & settings | On device only (not sent to MappM) | App functionality |

Third-party transfers related to ads and billing use encrypted transport (HTTPS) as
implemented by Google’s SDKs.

## Changes

We may update this policy when features change (ads, purchases, accounts, etc.). The
**Last updated** date will change; material changes are also reflected in Play Data safety
when required.

## Source code & distribution

Source is available under the RackTrack Source-Available License for viewing and study.
Official binaries are distributed through authorized app stores (not as redistributable
GitHub release APKs).
