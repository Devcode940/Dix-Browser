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
import com.devcode940.web.page.frontpage.SiteAdapterV2
import com.devcode940.web.utils.EasyLog
import com.devcode940.web.utils.StringUtils
import com.devcode940.web.webkit.PageNestedWebView

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_new_tab_v2, container, false)
        val frame = rootView.findViewById<FrameLayout>(R.id.new_tab_v2_frame)
        frameLayout = frame

        val fpv = FrontPageView(requireContext())
        frontPageView = fpv
        fpv.setTabTitle(mTitle ?: "")

        fpv.setSiteListener(object : SiteAdapterV2.OnSiteItemClickListener {
            override fun onSiteItemClick(webSite: WebSite) {
                loadUri = Uri.parse(webSite.siteUrl)
                loadUri?.let { addWebView(it) }
            }
        })

        val uri = loadUri
        if (uri == null) {
            frame.addView(fpv)
        } else {
            addWebView(uri)
        }

        return rootView
    }

    private fun addWebView(uri: Uri) {
        val frame = frameLayout ?: return
        frame.removeAllViews()
        val pw = PageNestedWebView(requireContext())
        pageWebView = pw
        pw.setOnWebInteractListener(this)
        frame.addView(pw)
        pw.loadUrl(uri.toString())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        webInteractParent = context as? IWebView.OnWebInteractListener
            ?: throw RuntimeException("$context must implement IWebView.OnWebInteractListener")
    }

    override fun onDetach() {
        super.onDetach()
        webInteractParent = null
    }

    private fun currentChildView(): View? = frameLayout?.takeIf { it.childCount > 0 }?.getChildAt(0)

    override fun onBackPressed(): Boolean {
        val view = currentChildView() ?: return false
        if (view is FrontPageView) return false
        if (view !is IWebView) return false

        if (pageWebView?.canGoBack() == true) {
            pageWebView?.goBack()
            return true
        }
        // Web history exhausted: drop the WebView and return to the shortcuts page.
        val frame = frameLayout ?: return false
        frame.removeAllViews()
        destroyWebView()
        frame.addView(frontPageView)
        try {
            mTitle = getString(R.string.new_tab_welcome)
            loadUri = null
            updateTitle(provideTabInfo())
        } catch (e: Exception) {
        }
        return true
    }

    override fun onPageTitleChange(tabInfo: TabInfo) {
        tabInfo.tag = mTag
        updateTitle(tabInfo)
    }

    override fun onLongClick(clickInfo: ClickInfo) {
        webInteractParent?.onLongClick(clickInfo)
    }

    override fun provideTabInfo(): TabInfo = TabInfo.create(mTag ?: "", mTitle, loadUri)

    override fun goForward() {
        val view = currentChildView() ?: return
        if (view is FrontPageView) return
        if (view is IWebView && view.canGoForward()) {
            view.goForward()
        }
    }

    override fun gotoHomePage() {
        val view = currentChildView() ?: return
        if (view is FrontPageView) return
        if (view is IWebView) {
            val frame = frameLayout ?: return
            frame.removeAllViews()
            destroyWebView()
            frame.addView(frontPageView)
            try {
                mTitle = getString(R.string.new_tab_welcome)
                loadUri = null
                updateTitle(provideTabInfo())
            } catch (e: Exception) {
            }
        }
    }

    override fun loadUrl(url: String) {
        if (StringUtils.isEmpty(url)) return
        val view = currentChildView() ?: return
        if (view is IWebView) {
            view.loadUrl(url)
        } else {
            addWebView(Uri.parse(url))
        }
    }

    override fun reload() {
        val view = currentChildView() ?: return
        if (view is IWebView) {
            view.reload()
        }
    }

    override fun getTabPreview(): Bitmap? = pageWebView?.capturePreview()

    override fun onDestroyView() {
        super.onDestroyView()
        destroyWebView()
        frameLayout?.removeAllViews()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) pauseWebView() else resumeWebView()
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
        pageWebView?.onDestroy()
        pageWebView = null
    }

    private fun updateTitle(tabInfo: TabInfo) {
        mTitle = tabInfo.title
        arguments?.putString(TabConst.ARG_TITLE, mTitle)
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
        } else {
            arguments?.let { args ->
                mTitle = args.getString(TabConst.ARG_TITLE)
                mTag = args.getString(TabConst.ARG_TAG)
                loadUri = args.getParcelable(TabConst.ARG_URI)
            } ?: run {
                mTag = System.currentTimeMillis().toString()
            }
        }

        EasyLog.i("test", "title: $mTitle")
        EasyLog.i("test", "tag: $mTag")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TabConst.ARG_TITLE, mTitle)
        outState.putString(TabConst.ARG_TAG, mTag)
        loadUri?.let { outState.putParcelable(TabConst.ARG_URI, it) }
    }

    companion object {
        @JvmStatic
        fun newInstance(): NewTabFragmentV2 = NewTabFragmentV2()

        @JvmStatic
        fun newInstance(title: String?): NewTabFragmentV2 {
            val args = Bundle()
            args.putString(TabConst.ARG_TITLE, title)
            return NewTabFragmentV2().apply { arguments = args }
        }

        @JvmStatic
        fun newInstance(title: String?, tag: String?): NewTabFragmentV2 {
            val args = Bundle()
            args.putString(TabConst.ARG_TITLE, title)
            args.putString(TabConst.ARG_TAG, tag)
            return NewTabFragmentV2().apply { arguments = args }
        }

        @JvmStatic
        fun newInstance(title: String?, tag: String?, uri: Uri?): NewTabFragmentV2 {
            val args = Bundle()
            args.putString(TabConst.ARG_TITLE, title)
            args.putString(TabConst.ARG_TAG, tag)
            args.putParcelable(TabConst.ARG_URI, uri)
            return NewTabFragmentV2().apply { arguments = args }
        }
    }
}
