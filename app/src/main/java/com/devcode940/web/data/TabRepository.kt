package com.devcode940.web.data

import com.devcode940.web.ui.session.SavedTab

/**
 * Repository for managing browser tabs (clean architecture)
 */
class TabRepository {

    private val tabs = mutableListOf<SavedTab>()

    fun addTab(tab: SavedTab) {
        tabs.add(tab)
    }

    fun removeTab(tab: SavedTab) {
        tabs.remove(tab)
    }

    fun getAllTabs(): List<SavedTab> = tabs.toList()

    fun clearTabs() {
        tabs.clear()
    }
}