package com.devcode940.web.ui.theme

import androidx.appcompat.app.AppCompatDelegate

/**
 * Dark Mode / Theme Manager for Phase 2
 */
object ThemeManager {

    fun applyDarkMode(enabled: Boolean) {
        val mode = if (enabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun applySystemDefault() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}