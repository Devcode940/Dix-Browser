package com.devcode940.web.ui.incognito

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Incognito / Private Mode Manager for Phase 2
 */
class IncognitoManager {

    private val _isIncognito = MutableLiveData(false)
    val isIncognito: LiveData<Boolean> get() = _isIncognito

    private val incognitoTabs = mutableListOf<Any>() // Placeholder for incognito tabs

    fun enableIncognito() {
        _isIncognito.value = true
        // Clear cookies, cache, history for this session
    }

    fun disableIncognito() {
        _isIncognito.value = false
        incognitoTabs.clear()
    }

    fun isIncognitoMode(): Boolean = _isIncognito.value ?: false
}