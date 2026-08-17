package com.racktrack.monetization

/**
 * Pure decision tree for optional interstitial before match start.
 * See docs/09-monetization.md — never blocks start when Premium, cooldown, or not loaded.
 */
object MonetizationPolicy {
    const val COOLDOWN_MS: Long = 5L * 60L * 1000L
    const val REMOVE_ADS_PRODUCT_ID: String = "remove_ads"

    /**
     * @return true if an interstitial should be shown now (caller must still have a loaded ad).
     */
    fun shouldShowInterstitial(
        adsRemoved: Boolean,
        lastShownAtEpochMs: Long?,
        nowEpochMs: Long,
        interstitialLoaded: Boolean,
    ): Boolean {
        if (adsRemoved) return false
        if (!interstitialLoaded) return false
        val last = lastShownAtEpochMs ?: return true
        return nowEpochMs - last >= COOLDOWN_MS
    }
}
