package com.devcode940.web.ui.browser

/**
 * UI State for Browser screen (MVI / State pattern)
 */
sealed class BrowserState {
    object Idle : BrowserState()
    object Loading : BrowserState()
    data class Loaded(val url: String, val title: String) : BrowserState()
    data class Error(val message: String) : BrowserState()
}