package com.devcode940.web.webkit

import android.os.Handler
import android.os.Message
import com.devcode940.web.contract.IWebView
import com.devcode940.web.entity.bo.ClickInfo
import java.lang.ref.WeakReference

class WebViewClickHandler(webView: IWebView) : Handler() {

    private val webViewWeakReference = WeakReference(webView)

    override fun handleMessage(msg: Message) {
        val webView = webViewWeakReference.get() ?: return
        val listener = webView.getOnWebInteractListener() ?: return
        val data = msg.data ?: return
        val clickInfo = ClickInfo()
        clickInfo.url = data.getString(KEY_URL)
        clickInfo.type = msg.what
        listener.onLongClick(clickInfo)
    }

    companion object {
        const val KEY_URL = "url"
    }
}
