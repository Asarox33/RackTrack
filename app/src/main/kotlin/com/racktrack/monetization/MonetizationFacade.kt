package com.racktrack.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * App-level glue: Premium store + interstitial + UMP.
 * Construct from [MainActivity]; keep out of domain / [com.racktrack.presentation.viewmodel.MatchCoordinator].
 *
 * [start] must never crash the process — Setup UI must remain usable if ads/billing fail.
 */
@Suppress("TooGenericExceptionCaught")
class MonetizationFacade(
    context: Context,
    private val scope: CoroutineScope,
) {
    val removeAdsStore = RemoveAdsStore(context)
    private val interstitial =
        InterstitialAdManager(
            context = context,
            adsRemoved = { removeAdsStore.adsRemoved.value },
        )

    val adsRemoved: StateFlow<Boolean> = removeAdsStore.adsRemoved
    val billingStatusMessage: StateFlow<String?> = removeAdsStore.statusMessage

    @Volatile
    private var started: Boolean = false

    fun start(activity: Activity) {
        if (started) return
        started = true
        try {
            removeAdsStore.start()
        } catch (t: Throwable) {
            Log.e(TAG, "Billing start failed", t)
        }
        scope.launch {
            try {
                removeAdsStore.adsRemoved.collectLatest { removed ->
                    if (removed) interstitial.onPremiumAcquired()
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Premium collect failed", t)
            }
        }
        try {
            val consent = ConsentManager(activity)
            consent.gatherConsent {
                try {
                    interstitial.initialize()
                } catch (t: Throwable) {
                    Log.e(TAG, "MobileAds initialize failed", t)
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "UMP consent failed", t)
            try {
                interstitial.initialize()
            } catch (initError: Throwable) {
                Log.e(TAG, "MobileAds initialize failed after consent error", initError)
            }
        }
    }

    fun stop() {
        try {
            removeAdsStore.end()
        } catch (t: Throwable) {
            Log.e(TAG, "Billing end failed", t)
        }
    }

    fun runAfterAdOpportunity(
        activity: Activity,
        onContinue: () -> Unit,
    ) {
        if (removeAdsStore.adsRemoved.value) {
            onContinue()
            return
        }
        try {
            interstitial.showOrSkip(activity, onContinue)
        } catch (t: Throwable) {
            Log.e(TAG, "Interstitial show failed — continuing", t)
            onContinue()
        }
    }

    fun launchRemoveAdsPurchase(activity: Activity) {
        try {
            removeAdsStore.launchPurchase(activity)
        } catch (t: Throwable) {
            Log.e(TAG, "Purchase launch failed", t)
        }
    }

    fun restorePurchases() {
        try {
            removeAdsStore.restorePurchases()
        } catch (t: Throwable) {
            Log.e(TAG, "Restore failed", t)
        }
    }

    fun consumeBillingStatusMessage() {
        removeAdsStore.consumeStatusMessage()
    }

    fun onSetupVisible() {
        if (!removeAdsStore.adsRemoved.value) {
            try {
                interstitial.preload()
            } catch (t: Throwable) {
                Log.e(TAG, "Preload failed", t)
            }
        }
    }

    private companion object {
        const val TAG = "Monetization"
    }
}
