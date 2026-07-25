package com.devcode940.web.web

import android.webkit.WebView

/**
 * Kotlin extensions for WebView
 */
fun WebView.loadSecureUrl(url: String) {
    WebViewSecurityConfig.applySecureSettings(this)
    this.loadUrl(url)
}

fun WebView.enableJavaScriptSafely() {
    this.settings.javaScriptEnabled = true
}