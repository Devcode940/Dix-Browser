package com.devcode940.web.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesUtils {

    const val KEY_NO_PIC_MODE = "no_pic_mode"
    const val KEY_FIRST_BOOT = "first_boot"
    const val KEY_SITE_LIST_CREATED = "site_list_created"

    fun getSettingSP(context: Context?): SharedPreferences? {
        if (context == null) return null
        return context.getSharedPreferences("setting-sp", Context.MODE_PRIVATE)
    }
}
