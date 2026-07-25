package com.devcode940.web.utils

import android.content.Context
import android.util.TypedValue

object EasyViewUtils {

    fun dp2px(context: Context?, dpValue: Int): Float {
        if (context == null) return 0f
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue.toFloat(),
            context.resources.displayMetrics
        )
    }

    fun sp2px(context: Context?, spValue: Int): Float {
        if (context == null) return 0f
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spValue.toFloat(),
            context.resources.displayMetrics
        )
    }
}
