package com.racktrack.monetization

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.racktrack.BuildConfig

/**
 * AdMob interstitial + 5‑minute cooldown. Never blocks the caller’s continue path.
 */
class InterstitialAdManager(
    context: Context,
    private val adsRemoved: () -> Boolean,
    private val clock: EpochClock = SystemEpochClock,
    private val unitId: String = BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID,
) {
    private val appContext = context.applicationContext
    private val prefs =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Volatile
    private var interstitial: InterstitialAd? = null

    @Volatile
    private var loading: Boolean = false

    @Volatile
    private var sdkReady: Boolean = false

    fun initialize(onReady: (() -> Unit)? = null) {
        MobileAds.initialize(appContext) {
            sdkReady = true
            onReady?.invoke()
            preload()
        }
    }

    fun onPremiumAcquired() {
        interstitial = null
        loading = false
    }

    fun preload() {
        if (!canStartLoad()) return
        loading = true
        InterstitialAd.load(
            appContext,
            unitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loading = false
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    interstitial = null
                }
            },
        )
    }

    private fun canStartLoad(): Boolean {
        if (adsRemoved()) return false
        if (loading) return false
        if (interstitial != null) return false
        return sdkReady
    }

    /**
     * Shows an interstitial when policy allows; always invokes [onContinue] after
     * dismiss, failure, or skip.
     */
    fun showOrSkip(
        activity: Activity,
        onContinue: () -> Unit,
    ) {
        val loaded = interstitial
        val show =
            MonetizationPolicy.shouldShowInterstitial(
                adsRemoved = adsRemoved(),
                lastShownAtEpochMs = lastShownAtOrNull(),
                nowEpochMs = clock.nowMs(),
                interstitialLoaded = loaded != null,
            )
        if (!show || loaded == null) {
            onContinue()
            preload()
            return
        }

        loaded.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    markShown(clock.nowMs())
                }

                override fun onAdDismissedFullScreenContent() {
                    interstitial = null
                    onContinue()
                    preload()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitial = null
                    onContinue()
                    preload()
                }
            }
        interstitial = null
        loaded.show(activity)
    }

    private fun lastShownAtOrNull(): Long? {
        if (!prefs.contains(KEY_LAST_SHOWN)) return null
        return prefs.getLong(KEY_LAST_SHOWN, 0L)
    }

    private fun markShown(nowMs: Long) {
        prefs.edit().putLong(KEY_LAST_SHOWN, nowMs).apply()
    }

    companion object {
        private const val PREFS_NAME = "racktrack_monetization"
        private const val KEY_LAST_SHOWN = "interstitial_last_shown_ms"
    }
}
