package com.devcode940.web.ui.address

import android.content.Context
import android.webkit.URLUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.net.URLEncoder

/**
 * Modern Address Bar + Search Manager
 * Handles URL detection, search engine integration, and suggestions
 */
class AddressBarManager(private val context: Context) {

    private val _currentUrl = MutableLiveData<String>()
    val currentUrl: LiveData<String> get() = _currentUrl

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Supported search engines
    enum class SearchEngine(val baseUrl: String, val name: String) {
        GOOGLE("https://www.google.com/search?q=", "Google"),
        BING("https://www.bing.com/search?q=", "Bing"),
        DUCKDUCKGO("https://duckduckgo.com/?q=", "DuckDuckGo")
    }

    private var currentSearchEngine = SearchEngine.GOOGLE

    fun setSearchEngine(engine: SearchEngine) {
        currentSearchEngine = engine
    }

    fun getCurrentSearchEngine(): SearchEngine = currentSearchEngine

    /**
     * Process user input from address bar
     * - If it's a valid URL → return as-is
     * - If it's a search term → return search engine URL
     */
    fun processInput(input: String): String {
        val trimmed = input.trim()

        return when {
            // Already a full URL
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed

            // Looks like a domain (contains dot)
            trimmed.contains(".") && !trimmed.contains(" ") -> {
                if (URLUtil.isValidUrl("https://$trimmed")) {
                    "https://$trimmed"
                } else {
                    buildSearchUrl(trimmed)
                }
            }

            // Plain search term
            else -> buildSearchUrl(trimmed)
        }
    }

    private fun buildSearchUrl(query: String): String {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        return "${currentSearchEngine.baseUrl}$encodedQuery"
    }

    fun updateCurrentUrl(url: String) {
        _currentUrl.value = url
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    // Future: Add history suggestions
    fun getSuggestions(query: String): List<String> {
        // TODO: Implement history + bookmark suggestions
        return emptyList()
    }
}