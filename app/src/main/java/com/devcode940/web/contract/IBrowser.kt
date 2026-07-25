package com.devcode940.web.contract

import android.graphics.Bitmap
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import com.devcode940.web.entity.bo.TabInfo
import com.devcode940.web.entity.dao.History

/**
 * 抽象的浏览器接口。负责提供导航，历史记录，下载，书签，标签页控制等管理对象
 */
interface IBrowser {

    @UiThread
    fun provideBrowserComponent(componentName: String): IComponent

    interface IComponent

    interface INavController : IComponent {
        fun goBack()
        fun goForward()
        fun goHome()
        fun showTabs()
        fun showAddress(current: String)
        fun showSetting()
        fun showHistory()
    }

    interface IHistoryController : IComponent {
        fun addHistory(entity: History)
    }

    interface IDownloadController : IComponent

    interface IBookmarkController : IComponent

    interface ITabController : ITabQuickView.Subject, IComponent {
        fun onTabSelected(tabInfo: TabInfo)
        fun onTabClose(tabInfo: TabInfo)
        fun onTabCreate(tabInfo: TabInfo, backstage: Boolean)
        fun onTabGoHome()
        fun onTabGoForward()
        fun onTabLoadUrl(url: String)
        fun onTabRefresh()
        fun onRestoreTabCache(infoCopy: TabInfo, fragment: Fragment?)
        fun onCloseAllTabs()
        fun onDestroy()
        fun getCurrentTab(): TabInfo?
        fun getPreviewForTab(tabInfo: TabInfo): Bitmap?
    }
}
