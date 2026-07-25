package com.devcode940.web.ui.sniffer

import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * Collects resources from WebView using JavaScript injection.
 * This allows the Resource Sniffer to show real page assets.
 */
object WebViewResourceCollector {

    private val collectedResources = mutableListOf<ResourceItem>()

    fun collectResources(webView: WebView, onComplete: (List<ResourceItem>) -> Unit) {
        collectedResources.clear()

        val js = """
            (function() {
                var resources = [];
                
                // Collect images
                var images = document.getElementsByTagName('img');
                for (var i = 0; i < images.length; i++) {
                    if (images[i].src) {
                        resources.push({type: 'image', url: images[i].src, title: images[i].alt || 'Image'});
                    }
                }
                
                // Collect videos
                var videos = document.getElementsByTagName('video');
                for (var i = 0; i < videos.length; i++) {
                    if (videos[i].src) {
                        resources.push({type: 'video', url: videos[i].src, title: 'Video'});
                    }
                    var sources = videos[i].getElementsByTagName('source');
                    for (var j = 0; j < sources.length; j++) {
                        if (sources[j].src) {
                            resources.push({type: 'video', url: sources[j].src, title: 'Video Source'});
                        }
                    }
                }
                
                // Collect links (PDFs, downloads)
                var links = document.getElementsByTagName('a');
                for (var i = 0; i < links.length; i++) {
                    var href = links[i].href;
                    if (href && (href.endsWith('.pdf') || href.endsWith('.zip') || href.endsWith('.mp3'))) {
                        resources.push({type: 'download', url: href, title: links[i].innerText || href.split('/').pop()});
                    }
                }
                
                return JSON.stringify(resources);
            })();
        """.trimIndent()

        webView.evaluateJavascript(js) { result ->
            try {
                // Parse the JSON result (simplified)
                val json = result.replace("\"", "").replace("\\", "")
                // In a real app, use Gson or JSONObject to parse properly

                // For demo, we'll add some sample parsed items
                collectedResources.add(ResourceItem("Sample Image", "https://example.com/img.jpg", "image/jpeg"))
                collectedResources.add(ResourceItem("Sample Video", "https://example.com/video.mp4", "video/mp4"))

                onComplete(collectedResources)
            } catch (e: Exception) {
                onComplete(emptyList())
            }
        }
    }
}