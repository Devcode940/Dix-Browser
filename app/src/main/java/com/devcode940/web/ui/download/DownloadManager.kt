package com.devcode940.web.ui.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.*

data class DownloadItem(
    val id: Long,
    val title: String,
    val url: String,
    val fileName: String,
    val status: String = "Pending",
    val progress: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Modern Download Manager for Dix Browser
 * Handles WebView downloads using Android DownloadManager
 */
class BrowserDownloadManager(private val context: Context) {

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    private val _downloads = MutableLiveData<List<DownloadItem>>(emptyList())
    val downloads: LiveData<List<DownloadItem>> get() = _downloads

    private val downloadList = mutableListOf<DownloadItem>()

    /**
     * Start a download from WebView
     */
    fun startDownload(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long
    ): Long {
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        val request = DownloadManager.Request(Uri.parse(url))

        request.setMimeType(mimeType)
        request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url))
        request.addRequestHeader("User-Agent", userAgent)
        request.setDescription("Downloading file...")
        request.setTitle(fileName)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadId = downloadManager.enqueue(request)

        val item = DownloadItem(
            id = downloadId,
            title = fileName,
            url = url,
            fileName = fileName
        )

        downloadList.add(0, item)
        _downloads.value = downloadList.toList()

        return downloadId
    }

    /**
     * Get current download status
     */
    fun getDownloadStatus(downloadId: Long): DownloadItem? {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)

        return if (cursor.moveToFirst()) {
            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            val progress = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

            val percent = if (total > 0) (progress * 100 / total) else 0

            val statusText = when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> "Completed"
                DownloadManager.STATUS_FAILED -> "Failed"
                DownloadManager.STATUS_PAUSED -> "Paused"
                DownloadManager.STATUS_RUNNING -> "Downloading"
                else -> "Pending"
            }

            DownloadItem(
                id = downloadId,
                title = "",
                url = "",
                fileName = "",
                status = statusText,
                progress = percent
            )
        } else {
            null
        }.also { cursor.close() }
    }

    fun removeDownload(downloadId: Long) {
        downloadList.removeAll { it.id == downloadId }
        _downloads.value = downloadList.toList()
        downloadManager.remove(downloadId)
    }

    fun clearAll() {
        downloadList.clear()
        _downloads.value = emptyList()
    }

    fun getAllDownloads(): List<DownloadItem> = downloadList.toList()
}