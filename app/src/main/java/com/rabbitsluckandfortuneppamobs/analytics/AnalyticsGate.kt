package com.rabbitsluckandfortuneppamobs.analytics

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object AnalyticsGate {
    const val SDK_VERSION = "a_af_2.3"
    const val APPSFLYER_DEV_KEY = "BBdg5uc2LTqAuHVhf2C84j"
    const val BASE_STUB_URL = "https://fortunegamesonlymob.online/W1t9RI"

    private const val EXTERNAL_MARKER = "EXTERNALIDFILE"
    private const val PREFS = "app_prefs"
    private const val TAG = "AnalyticsGate"

    sealed class GateDecision {
        data object ShowApp : GateDecision()
        data class ShowWebView(val url: String) : GateDecision()
        data class OpenExternalBrowser(val url: String) : GateDecision()
        data object Error : GateDecision()
    }

    /**
     * Builds the gate URL with the same parameters as the Unity AppsFlyer SDK:
     *   external_id, creative_id, cv, organic, ud (first launch), afid (AppsFlyer UID).
     */
    fun buildStubUrl(context: Context, isOrganic: Boolean, appsFlyerId: String?): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val creativeId = if (prefs.contains("creativeid")) {
            prefs.getString("creativeid", "")!!
        } else {
            UUID.randomUUID().toString().also {
                prefs.edit().putString("creativeid", it).apply()
            }
        }

        val externalId = if (prefs.contains("externalid")) {
            prefs.getString("externalid", "")!!
        } else {
            SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date()).also {
                prefs.edit().putString("externalid", it).apply()
            }
        }

        val trimmedBase = BASE_STUB_URL.substringBefore('?')
        val organicFlag = if (isOrganic) "true" else "false"

        var url = buildString {
            append(trimmedBase)
            append("?external_id=").append(encode(externalId))
            append("&creative_id=").append(encode(creativeId))
            append("&cv=").append(encode(SDK_VERSION))
            append("&organic=").append(organicFlag)
        }

        if (!prefs.contains("ud")) {
            url += "&ud=1"
            prefs.edit().putString("ud", "1").apply()
        }

        if (!appsFlyerId.isNullOrEmpty()) {
            url += "&afid=${encode(appsFlyerId)}"
        }

        return url
    }

    suspend fun fetchGateDecision(context: Context, url: String): GateDecision = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        try {
            Log.d(TAG, "GET $url")
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 15_000
                if (this is HttpsURLConnection) {
                    TrustAllCerts.apply(this)
                }
            }

            prefs.getString("cookie", null)?.let { cookie ->
                if (cookie.isNotEmpty()) connection.setRequestProperty("Cookie", cookie)
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "responseCode=$responseCode")
            if (responseCode == 404 || responseCode == 500) {
                connection.disconnect()
                return@withContext GateDecision.Error
            }

            connection.getHeaderField("Set-Cookie")?.let { setCookie ->
                prefs.edit().putString("cookie", setCookie).apply()
            }

            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
            connection.disconnect()
            Log.d(TAG, "body length=${body.length}")

            when {
                body.contains(EXTERNAL_MARKER) -> GateDecision.OpenExternalBrowser(url)
                body.isNotEmpty() -> GateDecision.ShowWebView(url)
                else -> GateDecision.ShowApp
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gate request failed: ${e.javaClass.simpleName}: ${e.message}", e)
            GateDecision.Error
        }
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, Charsets.UTF_8.name())
}
