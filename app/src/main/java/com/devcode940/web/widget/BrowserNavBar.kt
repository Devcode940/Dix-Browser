package com.devcode940.web.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.devcode940.web.R

class BrowserNavBar : FrameLayout {

    var navBack: ImageView? = null
        internal set
    var navForward: ImageView? = null
        internal set
    var navHome: ImageView? = null
        internal set
    var navTab: ImageView? = null
        internal set
    var navSetting: ImageView? = null
        internal set

    var navListener: OnNavClickListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        LayoutInflater.from(context).inflate(R.layout.layout_bottom_navbar, this)

        val clickListener = View.OnClickListener { v -> navListener?.onItemClick(v) }

        navBack = findViewById(R.id.nav_back)
        navBack?.setOnClickListener(clickListener)
        navForward = findViewById(R.id.nav_forward)
        navForward?.setOnClickListener(clickListener)
        navHome = findViewById(R.id.nav_home)
        navHome?.setOnClickListener(clickListener)
        navTab = findViewById(R.id.nav_show_tabs)
        navTab?.setOnClickListener(clickListener)
        navSetting = findViewById(R.id.nav_setting)
        navSetting?.setOnClickListener(clickListener)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    interface OnNavClickListener {
        fun onItemClick(itemView: View)
    }
}
