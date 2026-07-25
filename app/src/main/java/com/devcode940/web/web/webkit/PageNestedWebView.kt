package com.devcode940.web.web.webkit

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.ContentLoadingProgressBar
import com.devcode940.web.R
import com.devcode940.web.common.BrowserConst
import com.devcode940.web.contract.IBrowser
import com.devcode940.web.contract.IWebView
import com.devcode940.web.entity.bo.TabInfo
import com.devcode940.web.entity.dao.History
import com.devcode940.web.page.browser.BrowserActivity
import com.devcode940.web.utils.EasyViewUtils
import com.devcode940.web.utils.SharedPreferencesUtils
import com.devcode940.web.web.ContentSecurityPolicy
import com.devcode940.web.web.SecureWebChromeClient
import com.devcode940.web.web.SecureWebViewClient
import com.devcode940.web.web.VideoDetectionListener
import com.devcode940.web.web.WebViewLifecycleManager
import com.devcode940.web.web.WebViewSecurityConfig
import com.devcode940.web.widget.BrowserNavBar

class PageNestedWebView : LinearLayout, IWebView {

    private var mContext: Context? = null

    private var webView: EasyNestedWebView? = null
    private var addressBar: AddressBar? = null

    private var goButton: ImageView? = null
    private var webAddress: TextView? = null
    private var progressBar: ContentLoadingProgressBar? = null
    private var browserNavBar: BrowserNavBar? = null

    private var preview: Bitmap? = null

    private var onWebInteractListener: IWebView.OnWebInteractListener? = null
    private var handler: WebViewClickHandler? = null

    private var sp: SharedPreferences? = null
    private var noPicMode = false

    private var hitResultExtra: String? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        LayoutInflater.from(context).inflate(R.layout.fragment_web_page_v2, this)
        initViews()
        handler = WebViewClickHandler(this)
    }

    private fun initViews() {
        configureWebView()

        addressBar = findViewById(R.id.web_address_bar)

        // TODO: Inflate error view and pass it to WebViewErrorHandler if needed

        goButton = findViewById(R.id.goto_button)
        goButton?.setOnClickListener { loadUrlFromAddressBar() }

        webAddress = findViewById(R.id.address_url)
        webAddress?.setOnClickListener {
            // 地址栏弹窗
            val browser = mContext as IBrowser
            val navController = browser.provideBrowserComponent(BrowserConst.NAVIGATION_COMPONENT)
                as IBrowser.INavController
            val text = webAddress?.text
            if (text != null) {
                navController.showAddress(text.toString())
            } else {
                navController.showAddress("about:blank")
            }
        }
        progressBar = findViewById(R.id.web_loading_progress_bar)

        browserNavBar = findViewById(R.id.web_nav_bar)
        browserNavBar?.setNavListener(WebNavListener(context))
    }

    private fun configureWebView() {
        val wv = findViewById<EasyNestedWebView>(R.id.page_webview)
        webView = wv

        // Apply centralized secure WebView settings (JavaScript disabled by default)
        WebViewSecurityConfig.applySecureSettings(wv)

        // Use secure WebChromeClient (blocks dangerous JS dialogs)
        wv.webChromeClient = SecureWebChromeClient()

        // Use secure WebViewClient with proper error handling
        wv.webViewClient = SecureWebViewClient()
        wv.setOnLongClickListener(MyWebLongClickListener())

        // Wire up Download Listener
        wv.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val ctx = mContext
            if (ctx is BrowserActivity) {
                ctx.onDownloadStart(url, userAgent, contentDisposition, mimeType, contentLength)
            }
        }
    }

    private fun loadUrlFromAddressBar() {
        val text = webAddress?.text
        if (text != null) {
            val url = text.toString()
            loadUrl(url)
        }
    }

    override fun loadUrl(url: String) {
        updateWebSettings()
        webView?.loadUrl(url)
    }

    override fun canGoBack(): Boolean = webView?.canGoBack() ?: false

    override fun goBack() {
        webView?.goBack()
    }

    override fun goForward() {
        webView?.goForward()
    }

    override fun canGoForward(): Boolean = webView?.canGoForward() ?: false

    override fun setOnWebInteractListener(listener: IWebView.OnWebInteractListener?) {
        this.onWebInteractListener = listener
    }

    override fun getOnWebInteractListener(): IWebView.OnWebInteractListener? = onWebInteractListener

    override fun releaseSession() {
        // donothing, for geckoView
    }

    override fun onResume() {
        WebViewLifecycleManager.resumeWebView(webView)
    }

    override fun onPause() {
        WebViewLifecycleManager.pauseWebView(webView)
    }

    override fun onDestroy() {
        WebViewLifecycleManager.destroyWebView(webView)
        webView = null
        onWebInteractListener = null
    }

    override fun capturePreview(): Bitmap? {
        val wv = webView ?: return null
        val width = EasyViewUtils.dp2px(mContext, 90).toInt()
        val height = EasyViewUtils.dp2px(mContext, 160).toInt()

        if (width > 0 && height > 0) {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)

            val left = wv.scrollX
            val top = wv.scrollY
            canvas.translate(-left.toFloat(), -top.toFloat())

            val scaleX = width.toFloat() / wv.width
            val scaleY = height.toFloat() / wv.height
            canvas.scale(scaleX, scaleY, left.toFloat(), top.toFloat())

            wv.draw(canvas)

            canvas.setBitmap(null)

            return bitmap
        }
        return null
    }

    private fun updateWebSettings() {
        if (sp == null) {
            sp = SharedPreferencesUtils.getSettingSP(context)
        }
        val wv = webView ?: return
        val webSettings = wv.settings
        if (sp != null) {
            noPicMode = sp!!.getBoolean(SharedPreferencesUtils.KEY_NO_PIC_MODE, false)
            wv.settings.blockNetworkImage = noPicMode
        }
    }

    inner class MyWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            if (newProgress == 100) {
                progressBar?.progress = 0
                progressBar?.hide()
                return
            }

            if (newProgress > 0 && (progressBar?.visibility == View.INVISIBLE ||
                    progressBar?.visibility == View.GONE)
            ) {
                progressBar?.show()
            }
            progressBar?.progress = newProgress
        }
    }

    inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            webAddress?.text = url
            onWebInteractListener?.onPageTitleChange(TabInfo.create("", url ?: ""))
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onWebInteractListener?.onPageTitleChange(TabInfo.create("", view?.title ?: ""))

            val ctx = mContext
            if (ctx !is IBrowser) {
                return
            }
            // FIXME 通过进度 == 100 判断，避免网页重定向生成多条无效历史记录
            // https://stackoverflow.com/questions/3149216/how-to-listen-for-a-webview-finishing-loading-a-url
            if (webView?.progress == 100) {
                val browser = ctx
                val historyController = browser.provideBrowserComponent(BrowserConst.HISTORY_COMPONENT)
                    as IBrowser.IHistoryController
                val history = History()
                history.title = view?.title
                history.url = view?.url
                history.time = System.currentTimeMillis()
                historyController.addHistory(history)
            }

            // Auto PiP Video Detection
            view?.let {
                VideoDetectionListener.injectVideoDetection(it) {
                    val c = mContext
                    if (c is BrowserActivity) {
                        c.enterPictureInPicture()
                    }
                }
            }
        }

        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            // Apply Content Security Policy
            val req = request
            if (req != null && ContentSecurityPolicy.shouldBlockRequest(req)) {
                return ContentSecurityPolicy.createBlockedResponse()
            }
            return super.shouldInterceptRequest(view, request)
        }
    }

    inner class MyWebLongClickListener : View.OnLongClickListener {
        override fun onLongClick(v: View): Boolean {
            val result = (v as WebView).hitTestResult ?: return false
            val type = result.type
            hitResultExtra = result.extra
            when (type) {
                WebView.HitTestResult.IMAGE_TYPE, WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                    val hitMsg = handler!!.obtainMessage(type)
                    val imageBundle = Bundle()
                    imageBundle.putString(WebViewClickHandler.KEY_URL, hitResultExtra)
                    hitMsg.data = imageBundle
                    handler!!.sendMessage(hitMsg)
                }
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                    // image anchor类型弹窗，需要获取图片url及父节点<a>标签的url
                    val msg = handler!!.obtainMessage(type)
                    msg.target = handler
                    webView?.requestFocusNodeHref(msg)
                }
                else -> {
                }
            }
            return true
        }
    }
}
