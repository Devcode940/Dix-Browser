package com.devcode940.web.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.devcode940.web.R

class WebSiteLogo : View {

    private val mPaint = Paint()
    private val bounds = Rect()

    private var name = "E"

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun getName(): String = name

    fun setName(name: String) {
        this.name = name
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = this.width
        val height = this.height

        mPaint.isAntiAlias = true
        mPaint.color = context.resources.getColor(R.color.blue_gray_600)
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), (width / 2).toFloat(), mPaint)

        mPaint.textAlign = Paint.Align.CENTER
        mPaint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        val textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 20f, resources.displayMetrics
        ).toInt()
        mPaint.textSize = textSize.toFloat()
        mPaint.color = Color.WHITE

        mPaint.getTextBounds(name, 0, name.length, bounds)
        val fontMetrics = mPaint.fontMetricsInt

        val x = width / 2 // Align.CENTER
        val y = height / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent
        canvas.drawText(name, x.toFloat(), y.toFloat(), mPaint)
    }
}
