package com.devcode940.web.page.history

import android.content.Context
import com.devcode940.web.EasyApplication
import com.devcode940.web.contract.IHistory
import com.devcode940.web.entity.dao.AppDatabase
import com.devcode940.web.entity.dao.History
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class HistoryPresenterImpl(
    private var mContext: Context?,
    private var view: IHistory.View?
) : IHistory.Presenter {

    private val mDisposable = CompositeDisposable()

    override fun getHistory(pageNo: Int, pageSize: Int) {
        val dps = Observable.create(ObservableOnSubscribe<List<History>> { emitter ->
            val application = mContext!!.applicationContext as EasyApplication
            val db: AppDatabase = application.getAppDatabase()
            val result = db.historyDao().getHistory(pageNo, pageSize)
            emitter.onNext(result)
        })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                { historyEntities ->
                    if (historyEntities.isNullOrEmpty()) {
                        view?.showEmptyResult()
                    } else {
                        view?.showHistory(historyEntities)
                    }
                },
                { /* handle error */ }
            )
        mDisposable.add(dps)
    }

    override fun onDestroy() {
        mDisposable.clear()
        mContext = null
        view = null
    }
}
