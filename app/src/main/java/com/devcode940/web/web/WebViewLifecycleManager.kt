package com.devcode940.web.web

import android.webkit.WebView

/**
 * Manages WebView lifecycle for better performance and memory usage.
 * Especially useful for multi-tab browsers.
 */
object WebViewLifecycleManager {

    fun pauseWebView(webView: WebView?) {
        webView?.apply {
            onPause()
            pauseTimers()
        }
    }

    fun resumeWebView(webView: WebView?) {
        webView?.apply {
            onResume()
            resumeTimers()
        }
    }

    fun destroyWebView(webView: WebView?) {
        webView?.apply {
            stopLoading()
            settings.javaScriptEnabled = false
            clearHistory()
            clearCache(true)
            loadUrl("about:blank")
            pauseTimers()
            removeAllViews()
            destroy()
        }
    }
}