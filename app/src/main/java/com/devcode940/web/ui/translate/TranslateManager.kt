package com.devcode940.web.ui.translate

import android.content.Context

/**
 * Translate Manager - Uses Free Translation (MyMemory API)
 */
class TranslateManager(private val context: Context) {

    fun translatePage(url: String) {
        FreeTranslateManager.translatePage(context, url)
    }

    fun translateText(text: String) {
        FreeTranslateManager.translateText(context, text)
    }
}