package com.devcode940.web.webkit

import android.content.Context
import android.view.View
import com.devcode940.web.R
import com.devcode940.web.common.BrowserConst
import com.devcode940.web.contract.IBrowser
import com.devcode940.web.widget.BrowserNavBar

class WebNavListener(context: Context?): BrowserNavBar.OnNavClickListener {

    var mContext = context

    override fun onItemClick(itemView: View) {
        val isBrowserController = mContext is IBrowser
        if (!isBrowserController) {
            return
        }
        val browser = mContext as IBrowser
        val navController = browser.provideBrowserComponent(BrowserConst.NAVIGATION_COMPONENT)
                as IBrowser.INavController
        when (itemView.id) {
            R.id.nav_back -> navController.goBack()
            R.id.nav_forward -> navController.goForward()
            R.id.nav_home -> navController.goHome()
            R.id.nav_show_tabs -> navController.showTabs()
            R.id.nav_setting -> navController.showSetting()
        }
    }
}