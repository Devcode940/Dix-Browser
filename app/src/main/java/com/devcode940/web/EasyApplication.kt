package com.devcode940.web

import android.app.Application
import androidx.room.Room
import com.devcode940.web.common.Const
import com.devcode940.web.entity.dao.AppDatabase
import com.devcode940.web.utils.SharedPreferencesUtils
import com.devcode940.web.web.WebViewSecurityConfig
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class with Hilt support.
 *
 * Merged from the legacy Java Application (DB + first-boot prefs) and the
 * modernized Hilt/security stub.
 */
@HiltAndroidApp
class EasyApplication : Application() {

    private lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        // Security: disable remote WebView debugging in production
        WebViewSecurityConfig.disableRemoteDebugging()
        initSetting()
        initDB()
    }

    private fun initSetting() {
        val sp = SharedPreferencesUtils.getSettingSP(this) ?: return
        val editor = sp.edit()
        if (sp.contains(SharedPreferencesUtils.KEY_FIRST_BOOT)) {
            editor.putBoolean(SharedPreferencesUtils.KEY_FIRST_BOOT, false)
        } else {
            editor.putBoolean(SharedPreferencesUtils.KEY_FIRST_BOOT, true)
        }
        editor.apply()
    }

    private fun initDB() {
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            Const.APP_DATABASE_NAME
        ).build()
    }

    fun getAppDatabase(): AppDatabase = db
}
