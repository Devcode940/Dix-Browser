package com.devcode940.web.page.tab

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.devcode940.web.R
import com.devcode940.web.common.TabConst
import com.devcode940.web.contract.ITab
import com.devcode940.web.contract.IWebView
import com.devcode940.web.entity.bo.ClickInfo
import com.devcode940.web.entity.bo.TabInfo
import com.devcode940.web.entity.dao.WebSite
import com.devcode940.web.page.frontpage.FrontPageView
import com.devcode940.web.utils.EasyLog
import com.devcode940.web.utils.StringUtils
import com.devcode940.web.web.webkit.PageNestedWebView

/**
 * 新标签页Fragment。显示收藏站点快捷按钮
 */
class NewTabFragmentV2 : Fragment(), ITab, IWebView.OnWebInteractListener {

    private var mTitle: String? = null
    private var mTag: String? = null
    private var loadUri: Uri? = null

    private var frameLayout: FrameLayout? = null
    private var frontPageView: FrontPageView? = null
    private var pageWebView: IWebView? = null

    private var webInteractParent: IWebView.OnWebInteractListener? = null

    /**
     * 创建新标签页，并指定标题与Tag
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_new_tab_v2, container, false)
        frameLayout = rootView.findViewById(R.id.new_tab_v2_frame)

        val fpv = FrontPageView(requireContext())
        frontPageView = fpv
        fpv.setTabTitle(mTitle ?: "")

        fpv.setSiteListener(object : com.devcode940.web.page.frontpage.SiteAdapterV2.OnSiteItemClickListener {
            override fun onSiteItemClick(webSite: WebSite) {
                // TODO
                val siteUri = Uri.parse(webSite.siteUrl)
                loadUri = siteUri
                addWebView(loadUri!!)
            }
        })
        if (loadUri == null) {
            frameLayout?.addView(fpv)
        } else {
            addWebView(loadUri!!)
        }

        return rootView
    }

    private fun addWebView(uri: Uri) {
        frameLayout?.removeAllViews()
        val pw = PageNestedWebView(requireContext())
        pageWebView = pw
        pw.setOnWebInteractListener(this)
        frameLayout?.addView(pw)
        pw.loadUrl(uri.toString())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IWebView.OnWebInteractListener) {
            webInteractParent = context
        } else {
            throw RuntimeException(
                context.toString() + " must implement IWebView.OnWebInteractListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        webInteractParent = null
    }

    override fun onBackPressed(): Boolean {
        if (frameLayout?.childCount ?: 0 <= 0) {
            return false
        }
        val view = frameLayout!!.getChildAt(0)
        // 已经在网站快捷方式 不能返回
        if (view is FrontPageView) {
            return false
        }
        // 不是IWebView 不能返回
        if (view !is IWebView) {
            return false
        }
        if (pageWebView?.canGoBack() == true) {
            // 网页可返回，执行网页的返回逻辑
            pageWebView?.goBack()
            return true
        } else {
            // 网页不能返回，将WebView移除，替换成网站快捷方式
            frameLayout?.removeAllViews()
            destroyWebView()
            frameLayout?.addView(frontPageView)
            try {
                mTitle = getString(R.string.new_tab_welcome)
                loadUri = null
                updateTitle(provideTabInfo())
            } catch (e: Exception) {
            }
            return true
        }
    }

    override fun onPageTitleChange(tabInfo: TabInfo) {
        tabInfo.tag = mTag
        updateTitle(tabInfo)
    }

    override fun onLongClick(clickInfo: ClickInfo) {
        webInteractParent?.onLongClick(clickInfo)
    }

    override fun provideTabInfo(): TabInfo = TabInfo.create(this.mTag ?: "", this.mTitle, this.loadUri)

    override fun goForward() {
        if (frameLayout?.childCount ?: 0 <= 0) {
            return
        }
        val view = frameLayout!!.getChildAt(0)
        if (view is FrontPageView) {
            return
        }
        if (view is IWebView) {
            val iWebView = view
            if (iWebView.canGoForward()) {
                iWebView.goForward()
            }
        }
    }

    override fun gotoHomePage() {
        if (frameLayout?.childCount ?: 0 <= 0) {
            return
        }
        val view = frameLayout!!.getChildAt(0)
        if (view is FrontPageView) {
            return
        }
        if (view is IWebView) {
            frameLayout?.removeAllViews()
            destroyWebView()
            frameLayout?.addView(frontPageView)
            try {
                mTitle = getString(R.string.new_tab_welcome)
                loadUri = null
                updateTitle(provideTabInfo())
            } catch (e: Exception) {
            }
        }
    }

    override fun loadUrl(url: String) {
        if (frameLayout?.childCount ?: 0 <= 0 || StringUtils.isEmpty(url)) {
            return
        }
        val view = frameLayout!!.getChildAt(0)
        if (view is IWebView) {
            view.loadUrl(url)
        } else {
            addWebView(Uri.parse(url))
        }
    }

    override fun getTabPreview(): Bitmap? {
        if (pageWebView != null) {
            return pageWebView?.capturePreview()
        }
        // TODO preview for shortcut
        return null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        destroyWebView()
        frameLayout?.removeAllViews()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            pauseWebView()
        } else {
            resumeWebView()
        }
    }

    override fun onResume() {
        super.onResume()
        resumeWebView()
    }

    override fun onPause() {
        super.onPause()
        pauseWebView()
    }

    private fun resumeWebView() {
        pageWebView?.onResume()
    }

    private fun pauseWebView() {
        pageWebView?.onPause()
    }

    private fun destroyWebView() {
        if (pageWebView != null) {
            pageWebView?.onDestroy()
            pageWebView = null
        }
    }

    private fun updateTitle(tabInfo: TabInfo) {
        mTitle = tabInfo.title
        if (arguments != null) {
            arguments?.putString(TabConst.ARG_TITLE, mTitle)
        }
        webInteractParent?.onPageTitleChange(tabInfo)
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getString(TabConst.ARG_TITLE)
            mTag = savedInstanceState.getString(TabConst.ARG_TAG)
            val resArg = Bundle()
            resArg.putString(TabConst.ARG_TITLE, mTitle)
            resArg.putString(TabConst.ARG_TAG, mTag)
            arguments = resArg

            loadUri = savedInstanceState.getParcelable(TabConst.ARG_URI)
        } else if (arguments != null) {
            mTitle = arguments?.getString(TabConst.ARG_TITLE)
            mTag = arguments?.getString(TabConst.ARG_TAG)
            loadUri = arguments?.getParcelable(TabConst.ARG_URI)
        } else {
            mTag = System.currentTimeMillis().toString() + ""
        }

        EasyLog.i("test", "title: $mTitle")
        EasyLog.i("test", "tag: $mTag")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TabConst.ARG_TITLE, mTitle)
        outState.putString(TabConst.ARG_TAG, mTag)
        loadUri?.let { outState.putParcelable(TabConst.ARG_URI, it) }
        EasyLog.i("test", "newtabfragment onsaveinstancestate: " + this.hashCode())
    }

    companion object {
        @JvmStatic
        fun newInstance(): NewTabFragmentV2 = NewTabFragmentV2()

        @JvmStatic
        fun newInstance(title: String?): NewTabFragmentV2 {
            val fragment = NewTabFragmentV2()
            val args = Bundle()
            args.putString(TabConst.ARG_TITLE, title)
            fragment.arguments = args
            return fragment
        }

        /**
         * 创建新标签页，并指定标题与Tag
         *
         * @param title 页面标题，在快捷列表中显示
         * @param tag   页面tag，用于缓存
         */
        @JvmStatic
        fun newInstance(title: String?, tag: String?): NewTabFragmentV2 {
            val fragment = NewTabFragmentV2()
            val args = Bundle()
            args.putString(TabConst.ARG_TITLE, title)
            args.putString(TabConst.ARG_TAG, tag)
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newInstance(title: String?, tag: String?, uri: Uri?): NewTabFragmentV2 {
            val fragment = NewTabFragmentV2()
            val args = Bundle()
            args.putString(TabConst.ARG_TITLE, title)
            args.putString(TabConst.ARG_TAG, tag)
            args.putParcelable(TabConst.ARG_URI, uri)
            fragment.arguments = args
            return fragment
        }
    }
}
