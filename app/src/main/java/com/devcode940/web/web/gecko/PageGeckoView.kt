package com.devcode940.web.web.gecko

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.widget.LinearLayout
import com.devcode940.web.contract.IWebView

class PageGeckoView : LinearLayout, IWebView {

    // TODO implement this class

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setOnWebInteractListener(listener: IWebView.OnWebInteractListener?) {}

    override fun getOnWebInteractListener(): IWebView.OnWebInteractListener? = null

    override fun loadUrl(url: String) {}

    override fun goBack() {}

    override fun canGoBack(): Boolean = false

    override fun goForward() {}

    override fun canGoForward(): Boolean = false

    override fun releaseSession() {}

    override fun onResume() {}

    override fun onPause() {}

    override fun onDestroy() {}

    override fun capturePreview(): Bitmap? = null
}
