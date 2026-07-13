package com.rabbitsluckandfortuneppamobs.analytics

import android.content.Context
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

class AnalyticsManager(private val appContext: Context) : AppsFlyerConversionListener {

    private val attributionReady = CompletableDeferred<Unit>()

    var isOrganic: Boolean = true
        private set

    var appsFlyerId: String? = null
        private set

    fun init() {
        AppsFlyerLib.getInstance().init(AnalyticsGate.APPSFLYER_DEV_KEY, this, appContext)
        AppsFlyerLib.getInstance().start(appContext)
        appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(appContext)
        Log.d(TAG, "AppsFlyer UID: $appsFlyerId")
    }

    suspend fun awaitAttribution(timeoutMs: Long = 10_000) {
        withTimeoutOrNull(timeoutMs) { attributionReady.await() }
    }

    override fun onConversionDataSuccess(conversionData: MutableMap<String, Any>?) {
        conversionData?.get("af_status")?.toString()?.let { status ->
            isOrganic = status == "Organic"
        }
        Log.d(TAG, "Conversion data success, organic=$isOrganic")
        completeAttribution()
    }

    override fun onConversionDataFail(error: String?) {
        Log.w(TAG, "Conversion data fail: $error")
        completeAttribution()
    }

    override fun onAppOpenAttribution(attributionData: MutableMap<String, String>?) = Unit

    override fun onAttributionFailure(error: String?) = Unit

    private fun completeAttribution() {
        if (!attributionReady.isCompleted) {
            attributionReady.complete(Unit)
        }
    }

    private companion object {
        const val TAG = "AnalyticsManager"
    }
}
