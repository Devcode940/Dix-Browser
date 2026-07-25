package com.devcode940.web.ui.adblock

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse

/**
 * Basic Ad Blocker for Phase 2
 * Uses a simple domain blacklist
 */
object AdBlocker {

    private val blockedDomains = setOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "facebook.com/tr",
        "ads.yahoo.com",
        "adnxs.com",
        "advertising.com",
        "adserver.com",
        "adservice.google.com",
        "pagead2.googlesyndication.com"
    )

    fun shouldBlockRequest(request: WebResourceRequest): Boolean {
        val url = request.url.toString().lowercase()
        return blockedDomains.any { domain ->
            url.contains(domain)
        }
    }

    fun createEmptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            "".byteInputStream()
        )
    }
}