package com.devcode940.web.web

import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

/**
 * Secure WebViewClient with proper SSL certificate validation
 * and improved error handling.
 */
open class SecureWebViewClient : WebViewClient() {

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        // IMPORTANT: Never call handler.proceed() without user confirmation.
        // This is a major security risk.

        when (error?.primaryError) {
            SslError.SSL_UNTRUSTED,
            SslError.SSL_EXPIRED,
            SslError.SSL_IDMISMATCH,
            SslError.SSL_NOTYETVALID,
            SslError.SSL_DATE_INVALID -> {
                handler?.cancel()
                view?.context?.let {
                    Toast.makeText(
                        it,
                        "⚠️ Security Warning: Invalid SSL certificate detected.\nConnection has been blocked for your safety.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            else -> {
                handler?.cancel()
                view?.context?.let {
                    Toast.makeText(it, "SSL Error: Connection blocked", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)

        if (request?.isForMainFrame == true) {
            view?.context?.let {
                Toast.makeText(it, "Failed to load page: ${error?.description}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false

        // Block dangerous schemes
        if (url.startsWith("javascript:") || url.startsWith("vbscript:") || url.startsWith("data:text/html")) {
            return true
        }

        return super.shouldOverrideUrlLoading(view, request)
    }
}