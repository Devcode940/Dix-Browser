package com.devcode940.web.contract

import android.graphics.Bitmap
import com.devcode940.web.entity.bo.ClickInfo
import com.devcode940.web.entity.bo.TabInfo

interface IWebView {

    fun setOnWebInteractListener(listener: OnWebInteractListener?)

    fun getOnWebInteractListener(): OnWebInteractListener?

    fun loadUrl(url: String)

    fun goBack()

    fun canGoBack(): Boolean

    fun goForward()

    fun canGoForward(): Boolean

    fun releaseSession()

    fun onResume()

    fun onPause()

    fun onDestroy()

    fun capturePreview(): Bitmap?

    interface OnWebInteractListener {
        fun onPageTitleChange(tabInfo: TabInfo)

        fun onLongClick(clickInfo: ClickInfo)
    }
}
