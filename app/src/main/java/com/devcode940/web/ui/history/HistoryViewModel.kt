package com.devcode940.web.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class HistoryItem(
    val id: Long = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HistoryViewModel : ViewModel() {

    private val _history = MutableLiveData<List<HistoryItem>>(emptyList())
    val history: LiveData<List<HistoryItem>> get() = _history

    private val historyList = mutableListOf<HistoryItem>()

    fun addHistory(title: String, url: String) {
        val item = HistoryItem(title = title, url = url)
        historyList.add(0, item)
        _history.value = historyList.toList()
    }

    fun removeHistory(item: HistoryItem) {
        historyList.remove(item)
        _history.value = historyList.toList()
    }

    fun clearAll() {
        historyList.clear()
        _history.value = emptyList()
    }

    fun search(query: String): List<HistoryItem> {
        return historyList.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.url.contains(query, ignoreCase = true)
        }
    }
}