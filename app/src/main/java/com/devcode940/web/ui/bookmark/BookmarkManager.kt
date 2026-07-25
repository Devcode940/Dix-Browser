package com.devcode940.web.ui.bookmark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

data class Bookmark(
    val id: Long = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Simple in-memory bookmark manager (can be replaced with Room later)
 */
class BookmarkManager {

    private val _bookmarks = MutableLiveData<List<Bookmark>>(emptyList())
    val bookmarks: LiveData<List<Bookmark>> get() = _bookmarks

    private val bookmarkList = mutableListOf<Bookmark>()

    fun addBookmark(title: String, url: String) {
        val bookmark = Bookmark(title = title, url = url)
        bookmarkList.add(0, bookmark)
        _bookmarks.value = bookmarkList.toList()
    }

    fun removeBookmark(bookmark: Bookmark) {
        bookmarkList.remove(bookmark)
        _bookmarks.value = bookmarkList.toList()
    }

    fun getAllBookmarks(): List<Bookmark> = bookmarkList.toList()
}