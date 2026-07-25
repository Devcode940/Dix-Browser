package com.devcode940.web.web.legacy

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.ContentLoadingProgressBar
import com.devcode940.web.R
import com.devcode940.web.common.BrowserConst
import com.devcode940.web.contract.IBrowser
import com.devcode940.web.contract.IWebView
import com.devcode940.web.entity.bo.TabInfo
import com.devcode940.web.entity.dao.History
import com.devcode940.web.utils.EasyLog
import com.devcode940.web.utils.SharedPreferencesUtils
import com.devcode940.web.utils.StringUtils
import com.devcode940.web.web.webkit.AddressBar
import com.devcode940.web.web.webkit.WebNavListener
import com.devcode940.web.widget.BrowserNavBar

class PageWebView : FrameLayout, IWebView {

    private var webView: EasyWebView? = null
    private var webLinear: RelativeLayout? = null

    private var addressBar: AddressBar? = null
    private var addressBarPlaceholder: PlaceholderView? = null
    private var navBarPlaceholder: PlaceholderView? = null
    private var goButton: ImageView? = null
    private var webAddress: EditText? = null
    private var progressBar: ContentLoadingProgressBar? = null

    private var browserNavBar: BrowserNavBar? = null

    private var onWebInteractListener: IWebView.OnWebInteractListener? = null

    private var mContext: Context? = null

    private var noPicMode = false

    private var orgAddressBarHeight = 0
    private var orgBrowserNavBarHeight = 0

    private var imageActionsDialog: AlertDialog? = null
    private var urlActionsDialog: AlertDialog? = null
    private var hitResultExtra: String? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        LayoutInflater.from(context).inflate(R.layout.fragment_web_page_v1, this)
        initViews()
    }

    private fun initViews() {
        configureWebView()

        addressBar = findViewById(R.id.web_address_bar)
        addressBar?.post { orgAddressBarHeight = addressBar!!.measuredHeight }

        webLinear = findViewById(R.id.web_linear)
        addressBarPlaceholder = findViewById(R.id.address_bar_placeholder)
        navBarPlaceholder = findViewById(R.id.nav_bar_placeholder)

        goButton = findViewById(R.id.goto_button)
        goButton?.setOnClickListener { loadInputUrl() }

        webAddress = findViewById(R.id.page_url_edittext)
        webAddress?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED ||
                actionId == EditorInfo.IME_ACTION_SEND ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                webAddress?.clearFocus()
                val ctx = context
                if (ctx is Activity) {
                    val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(ctx.window.decorView.windowToken, 0)
                }
                loadInputUrl()
            }
            false
        }
        progressBar = findViewById(R.id.web_loading_progress_bar)

        browserNavBar = findViewById(R.id.web_nav_bar)
        browserNavBar?.post { orgBrowserNavBarHeight = browserNavBar!!.measuredHeight }
        browserNavBar?.setNavListener(WebNavListener(context))
    }

    private fun configureWebView() {
        webView = findViewById(R.id.page_webview)
        val wv = webView!!
        wv.webChromeClient = object : WebChromeClient() {
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
        wv.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webAddress?.setText(url)
                onWebInteractListener?.onPageTitleChange(TabInfo.create("", view?.title ?: ""))

                val ctx = mContext
                if (ctx !is IBrowser) {
                    return
                }
                // FIXME 通过进度 == 100 判断，避免网页重定向生成多条无效历史记录
                if (wv.progress == 100) {
                    val browser = ctx
                    val historyController = browser.provideBrowserComponent(BrowserConst.HISTORY_COMPONENT)
                        as IBrowser.IHistoryController
                    val history = History()
                    history.title = view?.title
                    history.url = view?.url
                    history.time = System.currentTimeMillis()
                    historyController.addHistory(history)
                }
            }

            @Nullable
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                try {
                    val targetPath = request?.url?.path
                    if (StringUtils.isEmpty(targetPath)) {
                        return super.shouldInterceptRequest(view, request)
                    }
                    if (noPicMode && isPicResources(targetPath)) {
                        val placeHolderIS = mContext!!.assets.open("emptyplaceholder.png")
                        return WebResourceResponse("image/png", "UTF-8", placeHolderIS)
                    }
                } catch (e: Exception) {
                }
                return super.shouldInterceptRequest(view, request)
            }

            private fun isPicResources(path: String): Boolean {
                return path.endsWith(".jpg") ||
                    path.endsWith(".jpeg") ||
                    path.endsWith(".png") ||
                    path.endsWith(".gif")
            }
        }
        wv.setOnLongClickListener(OnLongClickListener { v ->
            val result = (v as WebView).hitTestResult ?: return@OnLongClickListener false
            val type = result.type
            val extra = result.extra
            hitResultExtra = result.extra
            when (type) {
                WebView.HitTestResult.IMAGE_TYPE -> {
                    EasyLog.i("test", "press image: $extra")
                    showImageActionsDialog()
                }
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                    EasyLog.i("test", "press image anchor: $extra")
                    // TODO 实现image anchor类型弹窗，需要获取图片url及父节点<a>标签的url
                }
                WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                    EasyLog.i("test", "press url: $extra")
                    showUrlActionsDialog()
                }
                else -> {
                }
            }
            true
        })
        wv.webViewScrollListener = object : EasyWebView.WebViewScrollListener {
            override fun onScrollUp() {
                hideAddressBar()
            }

            override fun onScrollDown() {
                showAddressBar()
            }
        }
    }

    private fun loadInputUrl() {
        val text = webAddress?.text
        if (text != null) {
            val url = text.toString()
            loadUrl(url)
        }
    }

    override fun loadUrl(url: String) {
        val sp = SharedPreferencesUtils.getSettingSP(context)
        if (sp != null) {
            noPicMode = sp.getBoolean(SharedPreferencesUtils.KEY_NO_PIC_MODE, false)
        }
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

    override fun getOnWebInteractListener(): IWebView.OnWebInteractListener? = this.onWebInteractListener

    override fun releaseSession() {
        // donothing, for geckoView
    }

    override fun onResume() {
        webView?.onResume()
        webView?.resumeTimers()
    }

    override fun onPause() {
        webView?.onPause()
        webView?.pauseTimers()
    }

    override fun onDestroy() {
        val wv = webView ?: return
        wv.stopLoading()
        wv.settings.javaScriptEnabled = false
        wv.clearHistory()
        wv.clearCache(true)
        wv.loadUrl("about:blank")
        wv.pauseTimers()
        wv.removeAllViews()
        wv.destroy()
        webView = null
    }

    override fun capturePreview(): Bitmap? {
        return null
    }

    /**
     * 点击图片弹窗
     */
    private fun showImageActionsDialog() {
        if (imageActionsDialog != null) {
            imageActionsDialog?.show()
            return
        }
        val imageDialogBuilder = AlertDialog.Builder(mContext)
        imageDialogBuilder.setItems(R.array.image_actions) { _, which ->
            if (which == 0) {  // backstage
                notifyAddNewTab(true)
            } else if (which == 1) {
                notifyAddNewTab(false)
            }
        }
        imageActionsDialog = imageDialogBuilder.create()
        imageActionsDialog?.show()
    }

    /**
     * 点击网页链接弹窗
     */
    private fun showUrlActionsDialog() {
        if (urlActionsDialog != null) {
            urlActionsDialog?.show()
            return
        }
        val urlDialogBuilder = AlertDialog.Builder(mContext)
        urlDialogBuilder.setItems(R.array.url_actions) { _, which ->
            if (which == 0) {  // backstage
                notifyAddNewTab(true)
            } else if (which == 1) {
                notifyAddNewTab(false)
            }
        }
        urlActionsDialog = urlDialogBuilder.create()
        urlActionsDialog?.show()
    }

    private fun notifyAddNewTab(backStage: Boolean) {
        var tabController: IBrowser.ITabController? = null
        val ctx = mContext
        if (ctx is IBrowser) {
            val component = ctx.provideBrowserComponent(BrowserConst.TAB_COMPONENT)
            if (component is IBrowser.ITabController) {
                tabController = component
            }
        }
        if (tabController == null) {
            return
        }
        if (StringUtils.isEmpty(hitResultExtra)) {
            return
        }
        var uri: Uri? = null
        try {
            uri = Uri.parse(hitResultExtra)
        } catch (e: Exception) {
            uri = null
        }
        if (uri == null) {
            return
        }
        val tabInfo = TabInfo.create(
            System.currentTimeMillis().toString(),
            mContext!!.resources.getString(R.string.new_tab_welcome),
            uri
        )
        tabController.onTabCreate(tabInfo, backStage)
    }

    /**
     * translation动画较流畅，需要优化下拉手势判断，避免网页底部经常不可见
     * 动态调整LayoutParam方式频繁调用requestLayout，性能稍差
     */
    private fun hideAddressBar() {
        val animatorAddressBar = ObjectAnimator.ofFloat(addressBar, "translationY", 0f, -orgAddressBarHeight.toFloat())
        animatorAddressBar.duration = 300
        animatorAddressBar.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                val scrollY = webView!!.scrollY
                addressBarPlaceholder?.visibility = View.GONE
                navBarPlaceholder?.visibility = View.GONE
                webView!!.scrollY = scrollY - orgAddressBarHeight
                webView!!.setAnimating(true)
            }

            override fun onAnimationEnd(animation: Animator) {
                addressBar?.visibility = View.GONE
                webView!!.setAnimating(false)
                browserNavBar?.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator) {
                addressBar?.visibility = View.GONE
                webView!!.setAnimating(false)
                browserNavBar?.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animator) {}
        })

        val animatorNavBar = ObjectAnimator.ofFloat(browserNavBar, "translationY", 0f, orgBrowserNavBarHeight.toFloat())
        animatorNavBar.duration = 300

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animatorAddressBar, animatorNavBar)
        animatorSet.start()
    }

    private fun showAddressBar() {
        val animatorAddressBar =
            ObjectAnimator.ofFloat(addressBar, "translationY", -orgAddressBarHeight.toFloat(), 0f)
        animatorAddressBar.duration = 300
        animatorAddressBar.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                addressBar?.visibility = View.VISIBLE
                browserNavBar?.visibility = View.VISIBLE
                webView!!.setAnimating(true)

                val scrollY = webView!!.scrollY
                addressBarPlaceholder?.visibility = View.VISIBLE
                navBarPlaceholder?.visibility = View.VISIBLE
                webView!!.scrollY = scrollY + orgAddressBarHeight
            }

            override fun onAnimationEnd(animation: Animator) {
                webView!!.setAnimating(false)
            }

            override fun onAnimationCancel(animation: Animator) {
                addressBar?.translationY = 0f
                webView!!.setAnimating(false)
            }

            override fun onAnimationRepeat(animation: Animator) {}
        })
        val animatorNavBar = ObjectAnimator.ofFloat(browserNavBar, "translationY", orgBrowserNavBarHeight.toFloat(), 0f)
        animatorNavBar.duration = 300

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animatorAddressBar, animatorNavBar)
        animatorSet.start()
    }

    companion object {
        @JvmStatic
        fun newInstance(context: Context): PageWebView = PageWebView(context)
    }
}
