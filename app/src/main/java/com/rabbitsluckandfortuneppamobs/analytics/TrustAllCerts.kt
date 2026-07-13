package com.rabbitsluckandfortuneppamobs.analytics

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Mirrors Unity's BypassCertificate handler for gate endpoints with certificate issues.
 */
internal object TrustAllCerts {
    private val trustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }

    private val sslContext: SSLContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
    }

    val sslSocketFactory = sslContext.socketFactory

    fun apply(connection: HttpsURLConnection) {
        connection.sslSocketFactory = sslSocketFactory
        connection.hostnameVerifier = HostnameVerifier { _, _ -> true }
    }
}
