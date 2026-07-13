package com.rabbitsluckandfortuneppamobs.ui.screens

import android.annotation.SuppressLint
import android.net.http.SslError
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

private const val TAG = "StubScreen"

/** In-app WebView gate, mirrors the Unity SDK's UniWebView.Load(uri) branch. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun StubScreen(url: String) {
    var isLoading by remember { mutableStateOf(true) }

    Log.d(TAG, "StubScreen shown, url=$url")

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, finishedUrl: String?) {
                            Log.d(TAG, "onPageFinished: $finishedUrl")
                            isLoading = false
                        }

                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: SslError?,
                        ) {
                            Log.w(TAG, "SSL error bypassed: ${error?.primaryError}")
                            handler?.proceed()
                        }
                    }

                    webChromeClient = WebChromeClient()
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
