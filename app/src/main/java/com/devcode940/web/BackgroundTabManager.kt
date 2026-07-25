package com.devcode940.web

import android.webkit.WebView

/**
 * Manages background tabs for better performance.
 * Pauses WebViews when they are not visible.
 */
object BackgroundTabManager {

    private val activeWebViews = mutableSetOf<WebView>()

    fun registerWebView(webView: WebView) {
        activeWebViews.add(webView)
    }

    fun unregisterWebView(webView: WebView) {
        activeWebViews.remove(webView)
    }

    fun pauseAllExcept(currentWebView: WebView?) {
        activeWebViews.forEach { webView ->
            if (webView != currentWebView) {
                WebViewLifecycleManager.pauseWebView(webView)
            }
        }
    }

    fun resumeWebView(webView: WebView) {
        WebViewLifecycleManager.resumeWebView(webView)
    }

    fun pauseAll() {
        activeWebViews.forEach { webView ->
            WebViewLifecycleManager.pauseWebView(webView)
        }
    }
}