package com.devcode940.web.page.browser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel for BrowserActivity (MVVM Architecture)
 */
class BrowserViewModel : ViewModel() {

    private val _currentUrl = MutableLiveData<String>()
    val currentUrl: LiveData<String> get() = _currentUrl

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _pageTitle = MutableLiveData<String>()
    val pageTitle: LiveData<String> get() = _pageTitle

    fun updateUrl(url: String) {
        _currentUrl.value = url
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun setPageTitle(title: String) {
        _pageTitle.value = title
    }
}