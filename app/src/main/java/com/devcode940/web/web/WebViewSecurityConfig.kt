package com.devcode940.web.web

import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView

/**
 * Centralized secure WebView configuration
 * Addresses multiple security vulnerabilities
 */
object WebViewSecurityConfig {

    /**
     * Applies secure settings to a WebView.
     * JavaScript is DISABLED by default for security.
     */
    fun applySecureSettings(webView: WebView, enableJavaScript: Boolean = false) {
        webView.settings.apply {
            // === CRITICAL SECURITY SETTINGS ===

            // 1. JavaScript - DISABLED by default (major attack vector)
            javaScriptEnabled = enableJavaScript

            // 2. JavaScript can open windows automatically
            javaScriptCanOpenWindowsAutomatically = false

            // 3. Disable file access from file URLs
            allowFileAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false

            // 4. Disable mixed content (HTTPS + HTTP)
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

            // 5. Disable DOM storage by default (privacy)
            domStorageEnabled = false

            // 6. Disable database
            databaseEnabled = false

            // 7. Disable geolocation
            setGeolocationEnabled(false)

            // 8. Safe browsing (Android 8.1+)
            safeBrowsingEnabled = true

            // 9. Disable third-party cookies
            setAcceptThirdPartyCookies(webView, false)

            // 10. User agent (can be customized later)
            userAgentString = WebSettings.getDefaultUserAgent(webView.context)

            // 11. Disable WebView debugging in production (security)
            // This should be called separately in Application class
        }
    }

    /**
     * Disable remote WebView debugging (call this in release builds)
     */
    fun disableRemoteDebugging() {
        android.webkit.WebView.setWebContentsDebuggingEnabled(false)
    }

    /**
     * Enable JavaScript only for trusted domains (use with caution)
     */
    fun enableJavaScriptForTrustedSite(webView: WebView, url: String) {
        // TODO: Add domain whitelist check
        webView.settings.javaScriptEnabled = true
    }

    /**
     * Reset WebView to maximum security mode
     */
    fun resetToMaximumSecurity(webView: WebView) {
        applySecureSettings(webView, enableJavaScript = false)
    }
}