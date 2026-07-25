package com.devcode940.web.ui.offline

import android.content.Context
import android.os.Environment
import android.webkit.WebView
import java.io.File

/**
 * Offline Page Saving Manager
 */
object OfflinePageManager {

    fun saveCurrentPage(webView: WebView, context: Context, onSaved: (String) -> Unit) {
        val fileName = "saved_page_${System.currentTimeMillis()}.mht"
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "OfflinePages")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, fileName)

        webView.saveWebArchive(file.absolutePath, false) { path ->
            if (path != null) {
                onSaved(path)
            }
        }
    }

    fun getSavedPages(context: Context): List<File> {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "OfflinePages")
        return dir.listFiles()?.toList() ?: emptyList()
    }
}