package com.devcode940.web.contract

import com.devcode940.web.entity.bo.TabInfo

interface ITabQuickView {

    interface Subject {
        fun attach(observer: Observer)

        fun detach()

        fun provideInfoList(): List<TabInfo>

        fun updateTabInfo(info: TabInfo)
    }

    interface Observer {
        fun updateQuickView()
    }
}
