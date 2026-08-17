package com.racktrack.monetization

import android.app.Activity
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * Google UMP consent before ad requests (EEA/UK as required).
 * Continues even if the form fails — ads may then be limited / skipped by AdMob.
 */
class ConsentManager(
    private val activity: Activity,
) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(activity)

    fun gatherConsent(onFinished: () -> Unit) {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    onFinished()
                }
            },
            {
                onFinished()
            },
        )
    }

    fun canRequestAds(): Boolean = consentInformation.canRequestAds()
}
