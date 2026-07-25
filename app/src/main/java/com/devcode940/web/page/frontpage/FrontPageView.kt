package com.devcode940.web.page.frontpage

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devcode940.web.R
import com.devcode940.web.common.BrowserConst
import com.devcode940.web.contract.IBrowser
import com.devcode940.web.contract.IFrontPage
import com.devcode940.web.entity.dao.WebSite
import com.devcode940.web.widget.BrowserNavBar

class FrontPageView : FrameLayout, IFrontPage.View {

    private var siteGird: RecyclerView? = null
    private var siteAdapter: SiteAdapterV2? = null
    private var navBar: BrowserNavBar? = null

    private var presenter: IFrontPage.Presenter? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.layout_new_tab, this)

        siteGird = findViewById(R.id.site_grid)
        siteGird?.layoutManager = GridLayoutManager(getContext(), 4)
        siteAdapter = SiteAdapterV2(getContext())
        siteGird?.adapter = siteAdapter

        navBar = findViewById(R.id.new_tab_nav_bar)
        navBar?.navListener = FrontPageNavListener(getContext())

        val gotoButton = findViewById<ImageView>(R.id.goto_button)
        gotoButton.setImageResource(R.mipmap.ic_arrow_forward_black_36dp)
        val addressBar = findViewById<TextView>(R.id.address_url)
        addressBar.setText(R.string.search_or_type_url)
        addressBar.setTextColor(resources.getColor(R.color.gray_600, null))
        addressBar.setOnClickListener {
            if (context is IBrowser) {
                val navController = context.provideBrowserComponent(BrowserConst.NAVIGATION_COMPONENT)
                    as IBrowser.INavController
                navController.showAddress("about:blank")
            }
        }
        presenter = FrontPagePresenterImpl(getContext(), this)
        presenter?.getWebSite()
    }

    fun setSiteListener(listener: SiteAdapterV2.OnSiteItemClickListener) {
        siteAdapter?.setListener(listener)
    }

    fun setTabTitle(name: String) {
        // FIXME
    }

    override fun showWebSite(webSiteList: List<WebSite>) {
        if (siteAdapter == null) {
            return
        }
        siteAdapter?.appendDataList(webSiteList)
        siteAdapter?.notifyDataSetChanged()
    }

    private class FrontPageNavListener(private val _context: Context) : BrowserNavBar.OnNavClickListener {
        override fun onItemClick(itemView: View) {
            val isBrowserController = _context is IBrowser
            if (!isBrowserController) {
                return
            }
            val browser = _context as IBrowser
            val navController = browser.provideBrowserComponent(BrowserConst.NAVIGATION_COMPONENT)
                as IBrowser.INavController
            when (itemView.id) {
                R.id.nav_back -> {}
                R.id.nav_forward -> {}
                R.id.nav_home -> {}
                R.id.nav_show_tabs -> navController.showTabs()
                R.id.nav_setting -> navController.showSetting()
            }
        }
    }
}
