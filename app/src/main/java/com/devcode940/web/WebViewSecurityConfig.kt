package com.devcode940.web

import android.webkit.WebSettings
import android.webkit.WebView

object WebViewSecurityConfig {

    /**
     * Default WebView configuration for a general-purpose browser.
     *
     * JavaScript and DOM storage are enabled: a browser that disables them
     * cannot render the modern web. The genuinely dangerous vectors
     * (file:// access, universal file access) remain locked down.
     */
    fun applySecureSettings(webView: WebView, enableJavaScript: Boolean = true) {
        webView.settings.apply {
            javaScriptEnabled = enableJavaScript
            javaScriptCanOpenWindowsAutomatically = false

            allowFileAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false

            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

            domStorageEnabled = true
            databaseEnabled = true
            setGeolocationEnabled(false)

            safeBrowsingEnabled = true

            userAgentString = WebSettings.getDefaultUserAgent(webView.context)
        }
    }

    fun disableRemoteDebugging() {
        android.webkit.WebView.setWebContentsDebuggingEnabled(false)
    }
}
