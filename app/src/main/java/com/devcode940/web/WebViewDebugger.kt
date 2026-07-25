package com.devcode940.web

import android.os.Build
import android.webkit.WebView

/**
 * Controls WebView debugging (should be disabled in release)
 */
object WebViewDebugger {

    fun setDebuggingEnabled(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(enabled)
        }
    }
}