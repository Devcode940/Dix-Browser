package com.devcode940.web.ui.password

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure Password & Form Data Manager
 * Uses EncryptedSharedPreferences (recommended)
 */
object SecurePasswordManager {

    private const val PREF_NAME = "secure_passwords"
    private const val KEY_USERNAME = "username_"
    private const val KEY_PASSWORD = "password_"

    private fun getEncryptedPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun saveCredentials(context: Context, domain: String, username: String, password: String) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit()
            .putString(KEY_USERNAME + domain, username)
            .putString(KEY_PASSWORD + domain, password)
            .apply()
    }

    fun getCredentials(context: Context, domain: String): Pair<String, String>? {
        val prefs = getEncryptedPrefs(context)
        val username = prefs.getString(KEY_USERNAME + domain, null)
        val password = prefs.getString(KEY_PASSWORD + domain, null)

        return if (username != null && password != null) {
            Pair(username, password)
        } else null
    }

    fun clearAllCredentials(context: Context) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit().clear().apply()
    }

    fun removeCredentials(context: Context, domain: String) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit()
            .remove(KEY_USERNAME + domain)
            .remove(KEY_PASSWORD + domain)
            .apply()
    }
}