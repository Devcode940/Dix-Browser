package com.devcode940.web.page.history

import android.content.Context
import com.devcode940.web.EasyApplication
import com.devcode940.web.contract.IHistory
import com.devcode940.web.entity.dao.AppDatabase
import com.devcode940.web.entity.dao.History
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryPresenterImpl(
    context: Context,
    private var view: IHistory.View?
) : IHistory.Presenter {

    private var appContext: Context? = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun getHistory(pageNo: Int, pageSize: Int) {
        val ctx = appContext ?: return
        scope.launch {
            try {
                val db: AppDatabase = (ctx as EasyApplication).getAppDatabase()
                val result: List<History> = withContext(Dispatchers.IO) {
                    db.historyDao().getHistory(pageNo, pageSize)
                }
                val v = view
                if (result.isNullOrEmpty()) v?.showEmptyResult() else v?.showHistory(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        appContext = null
        view = null
    }
}
