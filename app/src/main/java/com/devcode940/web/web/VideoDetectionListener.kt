package com.devcode940.web.web

import android.webkit.WebView

/**
 * Detects when a video starts playing in WebView and triggers PiP
 */
object VideoDetectionListener {

    fun injectVideoDetection(webView: WebView, onVideoStarted: () -> Unit) {
        val js = """
            (function() {
                var videos = document.getElementsByTagName('video');
                for (var i = 0; i < videos.length; i++) {
                    videos[i].addEventListener('play', function() {
                        // Notify Android that video started
                        if (window.AndroidVideoListener) {
                            window.AndroidVideoListener.onVideoStarted();
                        }
                    });
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(js, null)

        // Add JavaScript interface
        webView.addJavascriptInterface(object {
            @android.webkit.JavascriptInterface
            fun onVideoStarted() {
                onVideoStarted()
            }
        }, "AndroidVideoListener")
    }
}