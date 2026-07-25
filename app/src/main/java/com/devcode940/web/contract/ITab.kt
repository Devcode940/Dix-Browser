package com.devcode940.web.contract

import android.graphics.Bitmap
import com.devcode940.web.entity.bo.TabInfo

interface ITab {
    fun provideTabInfo(): TabInfo

    fun onBackPressed(): Boolean

    fun goForward()

    fun gotoHomePage()

    fun loadUrl(url: String)

    fun reload()

    fun getTabPreview(): Bitmap?
}
