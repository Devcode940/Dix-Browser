package com.devcode940.web.contract

import com.devcode940.web.entity.dao.WebSite

interface IFrontPage {

    interface View {
        fun showWebSite(webSiteList: List<WebSite>)
    }

    interface Presenter {
        fun getWebSite()
    }
}
