package com.devcode940.web.ui.browser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devcode940.web.ui.address.AddressBarManager

/**
 * ViewModel for the main Browser screen.
 * Manages current tab state, loading, navigation, and address bar.
 */
class BrowserViewModel : ViewModel() {

    private val addressBarManager = AddressBarManager(null) // Will be injected later

    private val _currentUrl = MutableLiveData<String>()
    val currentUrl: LiveData<String> get() = _currentUrl

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> get() = _progress

    private val _pageTitle = MutableLiveData<String>()
    val pageTitle: LiveData<String> get() = _pageTitle

    private var currentSearchEngine = AddressBarManager.SearchEngine.GOOGLE

    fun setSearchEngine(engine: AddressBarManager.SearchEngine) {
        currentSearchEngine = engine
        addressBarManager.setSearchEngine(engine)
    }

    fun processAndLoadUrl(input: String): String {
        val processedUrl = addressBarManager.processInput(input)
        _currentUrl.value = processedUrl
        return processedUrl
    }

    fun updateUrl(url: String) {
        _currentUrl.value = url
        addressBarManager.updateCurrentUrl(url)
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun setProgress(progress: Int) {
        _progress.value = progress
    }

    fun setPageTitle(title: String) {
        _pageTitle.value = title
    }

    fun getCurrentSearchEngine(): AddressBarManager.SearchEngine = currentSearchEngine
}