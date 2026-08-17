package com.racktrack.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Play Billing one-time [MonetizationPolicy.REMOVE_ADS_PRODUCT_ID] + local cache.
 * Domain engines must not depend on this type.
 */
class RemoveAdsStore(
    context: Context,
) : PurchasesUpdatedListener {
    private val appContext = context.applicationContext
    private val prefs =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _adsRemoved = MutableStateFlow(prefs.getBoolean(KEY_ADS_REMOVED, false))
    val adsRemoved: StateFlow<Boolean> = _adsRemoved.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private var productDetails: ProductDetails? = null

    private val billingClient: BillingClient =
        BillingClient.newBuilder(appContext)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
            )
            .build()

    fun start() {
        if (billingClient.isReady) {
            queryProductDetails()
            refreshPurchases()
            return
        }
        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        queryProductDetails()
                        refreshPurchases()
                    } else {
                        Log.w(TAG, "Billing setup failed: ${result.debugMessage}")
                        _statusMessage.value =
                            "Billing unavailable (${result.responseCode}). Use Play Internal install."
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // Next purchase/restore will retry via startConnection if needed.
                }
            },
        )
    }

    fun end() {
        billingClient.endConnection()
    }

    fun consumeStatusMessage() {
        _statusMessage.value = null
    }

    fun launchPurchase(activity: Activity) {
        if (!billingClient.isReady) {
            _statusMessage.value = "Connecting to Play Billing…"
            start()
            return
        }
        val details = productDetails
        if (details == null) {
            queryProductDetails()
            _statusMessage.value =
                "Product not ready. Install from Play Internal (not sideload) and retry."
            return
        }
        val offerToken = details.oneTimePurchaseOfferDetails?.offerToken
        val productParamsBuilder =
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
        if (!offerToken.isNullOrEmpty()) {
            productParamsBuilder.setOfferToken(offerToken)
        }
        val flowParams =
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParamsBuilder.build()))
                .build()
        val launchResult = billingClient.launchBillingFlow(activity, flowParams)
        if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "launchBillingFlow: ${launchResult.debugMessage}")
            _statusMessage.value = "Purchase UI failed (${launchResult.responseCode})."
        }
    }

    fun restorePurchases() {
        if (!billingClient.isReady) {
            _statusMessage.value = "Connecting to Play Billing…"
            start()
            return
        }
        refreshPurchases(showEmptyFeedback = true)
    }

    override fun onPurchasesUpdated(
        result: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) handlePurchases(purchases)
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                // No toast needed.
            }
            else -> {
                Log.w(TAG, "onPurchasesUpdated: ${result.debugMessage}")
                _statusMessage.value = "Purchase failed (${result.responseCode})."
            }
        }
    }

    private fun queryProductDetails() {
        val product =
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(MonetizationPolicy.REMOVE_ADS_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        val params =
            QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(product))
                .build()
        billingClient.queryProductDetailsAsync(params) { result, detailsResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = detailsResult.productDetailsList.firstOrNull()
                if (productDetails == null) {
                    Log.w(TAG, "No productDetails for remove_ads")
                }
            } else {
                Log.w(TAG, "queryProductDetails: ${result.debugMessage}")
            }
        }
    }

    private fun refreshPurchases(showEmptyFeedback: Boolean = false) {
        val params =
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val before = _adsRemoved.value
                handlePurchases(purchases)
                if (showEmptyFeedback && before == _adsRemoved.value && !_adsRemoved.value) {
                    _statusMessage.value = "No purchases to restore."
                } else if (showEmptyFeedback && _adsRemoved.value) {
                    _statusMessage.value = "Ads removed restored."
                }
            } else {
                Log.w(TAG, "queryPurchases: ${result.debugMessage}")
                if (showEmptyFeedback) {
                    _statusMessage.value = "Restore failed (${result.responseCode})."
                }
            }
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        val owned =
            purchases.any { purchase ->
                purchase.products.contains(MonetizationPolicy.REMOVE_ADS_PRODUCT_ID) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            }
        purchases
            .filter { purchase ->
                purchase.products.contains(MonetizationPolicy.REMOVE_ADS_PRODUCT_ID) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    !purchase.isAcknowledged
            }.forEach { purchase ->
                val ack =
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                billingClient.acknowledgePurchase(ack) { /* best-effort */ }
            }
        setAdsRemoved(owned)
    }

    private fun setAdsRemoved(value: Boolean) {
        if (_adsRemoved.value == value) return
        _adsRemoved.value = value
        prefs.edit().putBoolean(KEY_ADS_REMOVED, value).apply()
    }

    companion object {
        private const val TAG = "RemoveAdsStore"
        private const val PREFS_NAME = "racktrack_monetization"
        private const val KEY_ADS_REMOVED = "ads_removed"
    }
}
