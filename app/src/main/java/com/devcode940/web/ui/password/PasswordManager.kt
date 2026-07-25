package com.devcode940.web.ui.password

import android.content.Context
import android.webkit.WebViewDatabase

/**
 * Password & Form Data Manager
 * Delegates to SecurePasswordManager for encrypted storage
 */
object PasswordManager {

    fun enablePasswordSaving(context: Context) {
        // Enable WebView form data (still useful for autofill)
        val webViewDatabase = WebViewDatabase.getInstance(context)
        // Note: Modern apps should use SecurePasswordManager instead
    }

    fun saveCredentials(context: Context, domain: String, username: String, password: String) {
        SecurePasswordManager.saveCredentials(context, domain, username, password)
    }

    fun getCredentials(context: Context, domain: String): Pair<String, String>? {
        return SecurePasswordManager.getCredentials(context, domain)
    }

    fun clearSavedPasswords(context: Context) {
        SecurePasswordManager.clearAllCredentials(context)
        val webViewDatabase = WebViewDatabase.getInstance(context)
        webViewDatabase.clearFormData()
    }
}