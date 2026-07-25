package com.devcode940.web.utils

import android.content.Context
import android.net.Uri
import com.devcode940.web.common.BrowserConst
import com.devcode940.web.contract.IBrowser
import com.devcode940.web.entity.bo.TabInfo

object TabHelper {

    @JvmStatic
    fun createTab(context: Context?, titleResId: Int, uriStr: String?, backStage: Boolean) {
        if (context == null) return
        val title = context.resources.getString(titleResId)
        createTab(context, title, uriStr, backStage)
    }

    @JvmStatic
    fun createTab(context: Context?, title: String, uriStr: String?, backStage: Boolean) {
        var tabController: IBrowser.ITabController? = null
        if (context is IBrowser) {
            val component = context.provideBrowserComponent(BrowserConst.TAB_COMPONENT)
            if (component is IBrowser.ITabController) {
                tabController = component
            }
        }
        if (tabController == null) return
        if (StringUtils.isEmpty(uriStr)) return
        var uri: Uri? = null
        try {
            uri = Uri.parse(uriStr)
        } catch (e: Exception) {
            uri = null
        }
        if (uri == null) return
        val tabInfo = TabInfo.create(System.currentTimeMillis().toString(), title, uri)
        tabController.onTabCreate(tabInfo, backStage)
    }
}
