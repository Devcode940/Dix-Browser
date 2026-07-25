package com.devcode940.web.ui.download

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java.io.File

/**
 * ViewModel for Download Manager
 */
class DownloadViewModel : ViewModel() {

    private val downloadManager = BrowserDownloadManager(null as Context) // Will be initialized properly

    val downloads: LiveData<List<DownloadItem>> = downloadManager.downloads

    fun startDownload(
        context: Context,
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long
    ): Long {
        val manager = BrowserDownloadManager(context)
        return manager.startDownload(url, userAgent, contentDisposition, mimeType, contentLength)
    }

    fun openDownload(context: Context, item: DownloadItem) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            item.fileName
        )

        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(item.fileName))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to file manager
                val fallback = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.fromFile(file), "*/*")
                }
                context.startActivity(fallback)
            }
        }
    }

    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".pdf") -> "application/pdf"
            fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> "image/jpeg"
            fileName.endsWith(".png") -> "image/png"
            fileName.endsWith(".mp4") -> "video/mp4"
            fileName.endsWith(".mp3") -> "audio/mpeg"
            else -> "*/*"
        }
    }

    fun clearAllDownloads() {
        downloadManager.clearAll()
    }
}