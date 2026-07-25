package com.devcode940.web.webkit

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
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
import com.devcode940.web.ContentSecurityPolicy
import com.devcode940.web.SecureWebChromeClient
import com.devcode940.web.SecureWebViewClient
import com.devcode940.web.VideoDetectionListener
import com.devcode940.web.WebViewLifecycleManager
import com.devcode940.web.WebViewSecurityConfig
import com.devcode940.web.widget.BrowserNavBar

class PageNestedWebView : LinearLayout, IWebView {

    private var mContext: Context? = null

    private var webView: EasyNestedWebView? = null
    private var addressBar: AddressBar? = null

    private var goButton: ImageView? = null
    private var webAddress: TextView? = null
    private var progressBar: ContentLoadingProgressBar? = null
    private var browserNavBar: BrowserNavBar? = null

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

        goButton = findViewById(R.id.goto_button)
        goButton?.setOnClickListener { loadUrlFromAddressBar() }

        webAddress = findViewById(R.id.address_url)
        webAddress?.setOnClickListener {
            val browser = mContext as? IBrowser ?: return@setOnClickListener
            val navController = browser.provideBrowserComponent(BrowserConst.NAVIGATION_COMPONENT)
                as IBrowser.INavController
            val text = webAddress?.text
            navController.showAddress(if (text != null) text.toString() else "about:blank")
        }
        progressBar = findViewById(R.id.web_loading_progress_bar)

        browserNavBar = findViewById(R.id.web_nav_bar)
        navBar?.navListener = WebNavListener(context)
    }

    private fun configureWebView() {
        val wv = findViewById<EasyNestedWebView>(R.id.page_webview)
        webView = wv

        WebViewSecurityConfig.applySecureSettings(wv)
        wv.webChromeClient = SecureWebChromeClient()

        // Compose the secure client with the browser behaviours the secure base
        // does not provide: title updates and history recording.
        wv.webViewClient = object : SecureWebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onWebInteractListener?.onPageTitleChange(TabInfo.create("", url ?: ""))
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onWebInteractListener?.onPageTitleChange(TabInfo.create("", view?.title ?: ""))

                val ctx = mContext
                if (view != null && view.progress == 100 && ctx is IBrowser) {
                    val historyController = ctx.provideBrowserComponent(BrowserConst.HISTORY_COMPONENT)
                        as IBrowser.IHistoryController
                    val history = History()
                    history.title = view.title
                    history.url = view.url
                    history.time = System.currentTimeMillis()
                    historyController.addHistory(history)
                }

                view?.let {
                    VideoDetectionListener.injectVideoDetection(it) {
                        val c = mContext
                        if (c is BrowserActivity) {
                            c.enterPictureInPicture()
                        }
                    }
                }
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: android.webkit.WebResourceRequest?
            ): android.webkit.WebResourceResponse? {
                val req = request
                if (req != null && ContentSecurityPolicy.shouldBlockRequest(req)) {
                    return ContentSecurityPolicy.createBlockedResponse()
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
        wv.setOnLongClickListener(MyWebLongClickListener())

        wv.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val ctx = mContext
            if (ctx is BrowserActivity) {
                ctx.onDownloadStart(url, userAgent, contentDisposition, mimeType, contentLength)
            }
        }
    }

    private fun loadUrlFromAddressBar() {
        val text = webAddress?.text ?: return
        loadUrl(text.toString())
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

    override fun reload() {
        webView?.reload()
    }

    override fun setOnWebInteractListener(listener: IWebView.OnWebInteractListener?) {
        this.onWebInteractListener = listener
    }

    override fun getOnWebInteractListener(): IWebView.OnWebInteractListener? = onWebInteractListener

    override fun releaseSession() {
        // no-op for WebView (present for parity with the Gecko renderer interface)
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
        if (width <= 0 || height <= 0) return null

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        val left = wv.scrollX
        val top = wv.scrollY
        canvas.translate(-left.toFloat(), -top.toFloat())
        canvas.scale(
            width.toFloat() / wv.width,
            height.toFloat() / wv.height,
            left.toFloat(),
            top.toFloat()
        )
        wv.draw(canvas)
        canvas.setBitmap(null)
        return bitmap
    }

    private fun updateWebSettings() {
        if (sp == null) {
            sp = SharedPreferencesUtils.getSettingSP(context)
        }
        val wv = webView ?: return
        val prefs = sp ?: return
        noPicMode = prefs.getBoolean(SharedPreferencesUtils.KEY_NO_PIC_MODE, false)
        wv.settings.blockNetworkImage = noPicMode
    }

    inner class MyWebLongClickListener : View.OnLongClickListener {
        override fun onLongClick(v: View): Boolean {
            val h = handler ?: return false
            val result = (v as WebView).hitTestResult ?: return false
            val type = result.type
            hitResultExtra = result.extra
            when (type) {
                WebView.HitTestResult.IMAGE_TYPE, WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                    val hitMsg = h.obtainMessage(type)
                    val imageBundle = Bundle()
                    imageBundle.putString(WebViewClickHandler.KEY_URL, hitResultExtra)
                    hitMsg.data = imageBundle
                    h.sendMessage(hitMsg)
                }
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                    val msg = h.obtainMessage(type)
                    msg.target = h
                    webView?.requestFocusNodeHref(msg)
                }
                else -> {
                }
            }
            return true
        }
    }
}
