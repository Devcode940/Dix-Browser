package com.devcode940.web

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse

/**
 * Basic Content Security Policy enforcement
 */
object ContentSecurityPolicy {

    private val blockedPatterns = listOf(
        "javascript:",
        "data:text/html",
        "vbscript:"
    )

    fun shouldBlockRequest(request: WebResourceRequest): Boolean {
        val url = request.url.toString().lowercase()
        return blockedPatterns.any { url.contains(it) }
    }

    fun createBlockedResponse(): WebResourceResponse {
        return WebResourceResponse("text/plain", "UTF-8", "".byteInputStream())
    }
}