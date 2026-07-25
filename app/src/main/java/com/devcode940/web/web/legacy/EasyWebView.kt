package com.devcode940.web.web.legacy

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.webkit.WebSettings
import android.webkit.WebView
import com.devcode940.web.utils.EasyLog
import kotlin.math.abs

open class EasyWebView : WebView {

    var webViewScrollListener: WebViewScrollListener? = null

    private var lastScrollType = 0
    private var animating = false
    private var initTouchY = 0f
    private var touchUpY = 0f
    private var mTouchSlop = 0

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (isInEditMode) return
        initDefaultSettings()
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        if (isInEditMode) return
        initDefaultSettings()
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    private fun initDefaultSettings() {
        EasyLog.d(TAG, "EasyWebView init")
        val settings = settings
        settings.javaScriptEnabled = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.useWideViewPort = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings.loadWithOverviewMode = true
        settings.domStorageEnabled = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (animating) {
            return false
        }

        val trackEvent = MotionEvent.obtain(event)
        val action = event.actionMasked

        if (action == MotionEvent.ACTION_DOWN) {
            initTouchY = trackEvent.y
        }

        if (action == MotionEvent.ACTION_UP) {
            touchUpY = trackEvent.y
            val deltaY = touchUpY - initTouchY
            EasyLog.i(TAG, "initTouchY: $initTouchY, touchUpY: $touchUpY, deltaY: $deltaY")
            if (touchUpY > initTouchY && abs(deltaY) > mTouchSlop * 15) {  // 增加滑动距离
                if (webViewScrollListener != null && lastScrollType != SCROLL_DOWN) {
                    webViewScrollListener?.onScrollDown()
                    lastScrollType = SCROLL_DOWN
                }
            } else if (touchUpY < initTouchY && abs(deltaY) > mTouchSlop) {
                if (webViewScrollListener != null && lastScrollType != SCROLL_UP) {
                    webViewScrollListener?.onScrollUp()
                    lastScrollType = SCROLL_UP
                }
            }
        }
        trackEvent.recycle()
        return super.onTouchEvent(event)
    }

    fun isAnimating(): Boolean = animating

    fun setAnimating(animating: Boolean) {
        this.animating = animating
    }

    interface WebViewScrollListener {
        fun onScrollUp()

        fun onScrollDown()
    }

    companion object {
        const val TAG = "EasyWebView"
        const val SCROLL_UP = 1
        const val SCROLL_DOWN = 2
    }
}
