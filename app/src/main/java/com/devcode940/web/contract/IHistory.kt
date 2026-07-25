package com.devcode940.web.contract

import com.devcode940.web.entity.dao.History

interface IHistory {

    interface View {
        fun showHistory(result: List<History>)
        fun showEmptyResult()
    }

    interface Presenter {
        fun getHistory(pageNo: Int, pageSize: Int)
        fun onDestroy()
    }
}
