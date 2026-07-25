package com.devcode940.web.ui.translate

import android.content.Context
import android.widget.Toast
import java.net.URLEncoder

/**
 * Free Auto Web Translation using MyMemory API (no API key required for basic use)
 */
object FreeTranslateManager {

    private const val MYMEMORY_API = "https://api.mymemory.translated.net/get?q="

    fun translateText(context: Context, text: String, targetLang: String = "en") {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val url = "$MYMEMORY_API$encodedText&langpair=auto|$targetLang"

        // For now, we open the translation URL in browser
        // In a full implementation, we would parse the JSON response
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
        context.startActivity(intent)

        Toast.makeText(context, "Opening translation...", Toast.LENGTH_SHORT).show()
    }

    fun translatePage(context: Context, url: String, targetLang: String = "en") {
        val encodedUrl = URLEncoder.encode(url, "UTF-8")
        val translateUrl = "https://translate.google.com/translate?sl=auto&tl=$targetLang&u=$encodedUrl"

        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(translateUrl))
        context.startActivity(intent)
    }
}