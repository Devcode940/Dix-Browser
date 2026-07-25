package com.devcode940.web.ui.session

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Auto Session Restoration Manager - Enhanced Version
 */
object SessionManager {

    private const val PREF_NAME = "session_prefs"
    private const val KEY_TABS = "saved_tabs"

    private val gson = Gson()

    fun saveTabs(context: Context, tabs: List<SavedTab>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(tabs)
        prefs.edit().putString(KEY_TABS, json).apply()
    }

    fun getSavedTabs(context: Context): List<SavedTab> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TABS, null) ?: return emptyList()
        val type = object : TypeToken<List<SavedTab>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun clearTabs(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_TABS).apply()
    }

    // Helper to convert TabInfo to SavedTab
    fun createSavedTab(title: String, url: String): SavedTab {
        return SavedTab(title, url)
    }
}

data class SavedTab(
    val title: String,
    val url: String
)