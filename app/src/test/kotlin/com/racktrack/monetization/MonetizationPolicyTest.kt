package com.racktrack.monetization

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MonetizationPolicyTest {
    @Test
    fun premiumSkipsAd() {
        assertFalse(
            MonetizationPolicy.shouldShowInterstitial(
                adsRemoved = true,
                lastShownAtEpochMs = null,
                nowEpochMs = 10_000L,
                interstitialLoaded = true,
            ),
        )
    }

    @Test
    fun notLoadedSkipsAd() {
        assertFalse(
            MonetizationPolicy.shouldShowInterstitial(
                adsRemoved = false,
                lastShownAtEpochMs = null,
                nowEpochMs = 10_000L,
                interstitialLoaded = false,
            ),
        )
    }

    @Test
    fun firstStartShowsWhenLoaded() {
        assertTrue(
            MonetizationPolicy.shouldShowInterstitial(
                adsRemoved = false,
                lastShownAtEpochMs = null,
                nowEpochMs = 10_000L,
                interstitialLoaded = true,
            ),
        )
    }

    @Test
    fun cooldownBlocksImmediateRestart() {
        val shownAt = 1_000L
        assertFalse(
            MonetizationPolicy.shouldShowInterstitial(
                adsRemoved = false,
                lastShownAtEpochMs = shownAt,
                nowEpochMs = shownAt + MonetizationPolicy.COOLDOWN_MS - 1,
                interstitialLoaded = true,
            ),
        )
    }

    @Test
    fun cooldownElapsedAllowsShow() {
        val shownAt = 1_000L
        assertTrue(
            MonetizationPolicy.shouldShowInterstitial(
                adsRemoved = false,
                lastShownAtEpochMs = shownAt,
                nowEpochMs = shownAt + MonetizationPolicy.COOLDOWN_MS,
                interstitialLoaded = true,
            ),
        )
    }
}
