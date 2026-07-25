package com.devcode940.web.page.browser

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.devcode940.web.R
import com.devcode940.web.contract.IBrowser
import com.devcode940.web.contract.ITab
import com.devcode940.web.contract.ITabQuickView
import com.devcode940.web.entity.bo.TabInfo
import com.devcode940.web.page.tab.NewTabFragmentV2

/**
 * LRU实现的标签页缓存。负责标签页的缓存及切换显示逻辑。
 */
class TabCacheManager(
    private val mContext: Context,
    private val fm: FragmentManager?,
    maxSize: Int,
    private var browserLayoutId: Int
) : IBrowser.ITabController {

    private var observer: ITabQuickView.Observer? = null

    private val lruCache: LruCache<TabInfo, Fragment> = object : LruCache<TabInfo, Fragment>(maxSize) {
        override fun entryRemoved(evicted: Boolean, key: TabInfo, oldValue: Fragment, newValue: Fragment?) {
            /**
             * Tab页面被移除或替换后，进行remove操作
             */
            if (fm == null || key == null) {
                return
            }
            oldValue.let { fm.beginTransaction().remove(it).commitAllowingStateLoss() }
        }
    }

    private val infoList: MutableList<TabInfo> = ArrayList()

    /**
     * 还原Tab页缓存，使用从Fragment中还原的参数生成TabInfo对象
     *
     * 此方法仅还原一个标签页，上层可能需要在循环中调用
     *
     * @param infoCopy 由Fragment中的参数还原的TabInfo对象，与复原列表里的hash值不同，put时需判断
     * @param fragment 目标Fragment
     */
    private fun restoreTabCache(infoCopy: TabInfo, fragment: Fragment?) {
        var prevIndex = -1
        for (i in infoList.indices) {
            if (infoCopy == infoList[i]) {
                prevIndex = i
                break
            }
        }
        if (prevIndex >= 0) {
            // 之前有缓存，直接put进cache，不更新列表，需要从infoList中拿到真正的TabInfo
            val info = infoList[prevIndex]
            lruCache.put(info, fragment as Fragment)
        } else {
            // 缓存列表里不存在此项
            infoList.add(infoCopy)
            lruCache.put(infoCopy, fragment as Fragment)
        }
    }

    private fun addToCache(info: TabInfo, fragment: Fragment) {
        var prevIndex = -1
        for (i in infoList.indices) {
            if (info == infoList[i]) {
                prevIndex = i
                break
            }
        }
        if (prevIndex >= 0) {
            // 之前有缓存，直接put进cache，不更新列表
            lruCache.put(info, fragment)
        } else {
            infoList.add(info)
            lruCache.put(info, fragment)
        }
    }

    private fun getFromCache(info: TabInfo): Fragment? = lruCache.get(info)

    private fun removeFromCache(info: TabInfo) {
        lruCache.remove(info)  // throw error when tag is null

        // 只有用户主动操作，才从recyclerview使用的列表中移除tag
        for (i in infoList.indices) {
            if (info == infoList[i]) {
                infoList.removeAt(i)
                break
            }
        }
    }

    private fun closeAllTabs() {
        lruCache.evictAll()
        infoList.clear()
    }

    /**
     * 用户点击标签页后，切换到目标页面
     */
    private fun switchToTab(info: TabInfo) {
        val manager = fm ?: return
        val current = findVisibleFragment(manager)
        val target = getFromCache(info)

        if (current == null) {
            // 当前没有显示任何Fragment，直接show，对应某一页面被关闭的情况
            manager.beginTransaction().show(target).commit()
            return
        }

        if (target != null) {
            // 点击的是缓存过的页面，替换显示新的Fragment
            manager.beginTransaction().hide(current).show(target).commit()
        } else {
            // 没有缓存页，原页面被回收。重新创建Fragment，复用tag并放至缓存中
            val fragmentToAdd = NewTabFragmentV2.newInstance()
            val fragmentInfo = info
            manager.beginTransaction().hide(current).add(browserLayoutId, fragmentToAdd, info.tag).commit()
            addToCache(fragmentInfo, fragmentToAdd)
        }
    }

    private fun addNewTab(info: TabInfo, backstage: Boolean) {
        val manager = fm ?: return
        val current = findVisibleFragment(manager)
        val fragmentToAdd = NewTabFragmentV2.newInstance(info.title, info.tag, info.uri)
        val transaction = manager.beginTransaction()
        transaction.add(browserLayoutId, fragmentToAdd)
        if (current != null && !backstage) {
            transaction.hide(current)
        } else if (current != null) {
            transaction.hide(fragmentToAdd)
        }
        transaction.commit()
        addToCache(info, fragmentToAdd)
        observer?.updateQuickView()
    }

    /**
     * 关闭标签页
     */
    private fun closeTab(info: TabInfo) {
        val orgIndex = findTabIndex(info)
        removeFromCache(info)
        observer?.updateQuickView()

        if (infoList.size <= 0 && observer != null) {
            val tabInfo = TabInfo.create(
                System.currentTimeMillis().toString(),
                mContext.getString(R.string.new_tab_welcome)
            )
            onTabCreate(tabInfo, false)
            return
        }

        if (orgIndex <= 0) {
            switchToTab(infoList[0])
        } else {
            switchToTab(infoList[orgIndex - 1])
        }
    }

    private fun findTabIndex(info: TabInfo): Int {
        var index = -1
        for (i in infoList.indices) {
            if (info == infoList[i]) {
                index = i
                break
            }
        }
        return index
    }

    private fun findTabByTag(tag: String): Int {
        var index = -1
        for (i in infoList.indices) {
            if (tag == infoList[i].tag) {
                index = i
                break
            }
        }
        return index
    }

    /**
     * 在当前的FragmentManager中寻找可见页面
     */
    private fun findVisibleFragment(manager: FragmentManager?): Fragment? {
        if (manager == null) {
            return null
        }
        var current: Fragment? = null
        val fragments = manager.fragments
        for (i in fragments.indices) {
            val tmp = fragments[i]
            if (!tmp.isHidden && tmp is ITab) {
                current = fragments[i]
            }
        }
        return current
    }

    override fun attach(observer: ITabQuickView.Observer) {
        this.observer = observer
    }

    override fun detach() {
        this.observer = null
    }

    override fun provideInfoList(): MutableList<TabInfo> = this.infoList

    override fun updateTabInfo(tabInfo: TabInfo) {
        try {
            val i = findTabIndex(tabInfo)
            if (i < 0) {
                return
            }
            val nTitle = tabInfo.title
            infoList[i].title = nTitle
        } catch (e: Exception) {
            e.printStackTrace()
        }

        observer?.updateQuickView()
    }

    override fun onTabSelected(tabInfo: TabInfo) {
        switchToTab(tabInfo)
    }

    override fun onTabClose(tabInfo: TabInfo) {
        closeTab(tabInfo)
    }

    override fun onTabCreate(tabInfo: TabInfo, backstage: Boolean) {
        addNewTab(tabInfo, backstage)
    }

    override fun onTabGoHome() {
        val manager = fm ?: return
        val target = findVisibleFragment(manager) ?: return
        if (target is ITab) {
            target.gotoHomePage()
        }
    }

    override fun onTabGoForward() {
        val manager = fm ?: return
        val target = findVisibleFragment(manager) ?: return
        if (target is ITab) {
            target.goForward()
        }
    }

    override fun onTabLoadUrl(url: String) {
        val manager = fm ?: return
        val target = findVisibleFragment(manager) ?: return
        if (target is ITab) {
            target.loadUrl(url)
        }
    }

    override fun onTabRefresh() {
        val manager = fm ?: return
        val target = findVisibleFragment(manager) ?: return
        if (target is ITab) {
            target.reload()
        }
    }

    override fun onRestoreTabCache(infoCopy: TabInfo, fragment: Fragment?) {
        restoreTabCache(infoCopy, fragment)
    }

    override fun onCloseAllTabs() {
        closeAllTabs()
    }

    override fun onDestroy() {}

    override fun getCurrentTab(): TabInfo? {
        if (this.fm == null) {
            return null
        }
        val fragment = findVisibleFragment(this.fm)
        if (fragment is ITab) {
            return fragment.provideTabInfo()
        }
        return null
    }

    override fun getPreviewForTab(tabInfo: TabInfo): Bitmap? {
        val fragment = getFromCache(tabInfo)
        if (fragment is ITab) {
            return fragment.getTabPreview()
        }
        return null
    }
}
