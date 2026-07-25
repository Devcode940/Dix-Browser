package com.devcode940.web.web.webkit

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import com.devcode940.web.utils.EasyLog

/**
 * Based on:
 * https://github.com/mozilla-mobile/focus-android/
 * NestedWebView.java
 */
class EasyNestedWebView : WebView, NestedScrollingChild {

    private var mLastY = 0
    private val mScrollOffset = IntArray(2)
    private val mScrollConsumed = IntArray(2)
    private var mNestedOffsetY = 0
    private lateinit var mChildHelper: NestedScrollingChildHelper

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (isInEditMode) return
        initDefaultSettings()
        mChildHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        if (isInEditMode) return
        initDefaultSettings()
        mChildHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    private fun initDefaultSettings() {
        EasyLog.d(TAG, "EasyNestedWebView init")
        val settings = settings
        settings.javaScriptEnabled = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.useWideViewPort = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings.loadWithOverviewMode = true
        settings.domStorageEnabled = true
        settings.blockNetworkImage = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val trackEvent = MotionEvent.obtain(event)
        val action = event.actionMasked

        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsetY = 0
        }

        val eventY = trackEvent.y.toInt()
        trackEvent.offsetLocation(0f, mNestedOffsetY.toFloat())

        when (action) {
            MotionEvent.ACTION_MOVE -> {
                var deltaY = mLastY - eventY

                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                    deltaY -= mScrollConsumed[1]
                    trackEvent.offsetLocation(0f, -mScrollOffset[1].toFloat())
                    mNestedOffsetY += mScrollOffset[1]
                }

                mLastY = eventY - mScrollOffset[1]

                if (dispatchNestedScroll(0, mScrollOffset[1], 0, deltaY, mScrollOffset)) {
                    mLastY -= mScrollOffset[1]
                    trackEvent.offsetLocation(0f, mScrollOffset[1].toFloat())
                    mNestedOffsetY += mScrollOffset[1]
                }
            }

            MotionEvent.ACTION_DOWN -> {
                mLastY = eventY
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stopNestedScroll()
            }

            else -> {
                // We don't care about other touch events
            }
        }

        // Execute event handler from parent class in all cases
        val eventHandled = super.onTouchEvent(trackEvent)

        // Recycle previously obtained event
        trackEvent.recycle()

        return eventHandled
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean = mChildHelper.isNestedScrollingEnabled

    override fun startNestedScroll(axes: Int): Boolean = mChildHelper.startNestedScroll(axes)

    override fun stopNestedScroll() {
        mChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean = mChildHelper.hasNestedScrollingParent

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean = mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)

    override fun dispatchNestedPreScroll(
        dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?
    ): Boolean = mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean =
        mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean =
        mChildHelper.dispatchNestedPreFling(velocityX, velocityY)

    companion object {
        const val TAG = "EasyNestedWebView"
    }
}
