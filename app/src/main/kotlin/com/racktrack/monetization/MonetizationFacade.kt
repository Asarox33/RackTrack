package com.racktrack.monetization

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * App-level glue: Premium store + interstitial + UMP.
 * Construct from [MainActivity]; keep out of domain / [com.racktrack.presentation.viewmodel.MatchCoordinator].
 */
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

    fun start(activity: Activity) {
        removeAdsStore.start()
        scope.launch {
            removeAdsStore.adsRemoved.collectLatest { removed ->
                if (removed) interstitial.onPremiumAcquired()
            }
        }
        val consent = ConsentManager(activity)
        consent.gatherConsent {
            interstitial.initialize()
        }
    }

    fun stop() {
        removeAdsStore.end()
    }

    fun runAfterAdOpportunity(
        activity: Activity,
        onContinue: () -> Unit,
    ) {
        if (removeAdsStore.adsRemoved.value) {
            onContinue()
            return
        }
        interstitial.showOrSkip(activity, onContinue)
    }

    fun launchRemoveAdsPurchase(activity: Activity) {
        removeAdsStore.launchPurchase(activity)
    }

    fun restorePurchases() {
        removeAdsStore.restorePurchases()
    }

    fun consumeBillingStatusMessage() {
        removeAdsStore.consumeStatusMessage()
    }

    fun onSetupVisible() {
        if (!removeAdsStore.adsRemoved.value) {
            interstitial.preload()
        }
    }
}
