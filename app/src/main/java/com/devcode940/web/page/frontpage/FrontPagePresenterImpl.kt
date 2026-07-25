package com.devcode940.web.page.frontpage

import android.content.Context
import com.devcode940.web.EasyApplication
import com.devcode940.web.contract.IFrontPage
import com.devcode940.web.entity.dao.AppDatabase
import com.devcode940.web.entity.dao.DaoManager
import com.devcode940.web.entity.dao.WebSite
import com.devcode940.web.utils.SharedPreferencesUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.ref.WeakReference

class FrontPagePresenterImpl(context: Context, view: IFrontPage.View) : IFrontPage.Presenter {

    private val mContext: WeakReference<Context> = WeakReference(context)
    private val mView: WeakReference<IFrontPage.View> = WeakReference(view)
    private val mDisposable = CompositeDisposable()

    override fun getWebSite() {
        val source = ObservableOnSubscribe<List<WebSite>> { emitter ->
            val ctx = mContext.get()
            val sp = if (ctx != null) SharedPreferencesUtils.getSettingSP(ctx) else null
            if (ctx != null && sp != null) {
                val application = ctx.applicationContext as EasyApplication
                val db: AppDatabase = application.getAppDatabase()
                val firstBoot = sp.getBoolean(SharedPreferencesUtils.KEY_FIRST_BOOT, true)
                val siteListCreated = sp.getBoolean(SharedPreferencesUtils.KEY_SITE_LIST_CREATED, false)
                if (firstBoot && !siteListCreated) {
                    DaoManager.createDefaultSiteList(db)
                    sp.edit().putBoolean(SharedPreferencesUtils.KEY_SITE_LIST_CREATED, true).apply()
                }
                val result = db.webSiteDao().all
                emitter.onNext(result)
            } else {
                emitter.onError(NullPointerException())
            }
        }

        val dps = Observable.create<List<WebSite>>(source)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                { webSiteList ->
                    val v = mView.get()
                    if (v != null && webSiteList != null) {
                        v.showWebSite(webSiteList)
                    }
                },
                { /* handle error */ }
            )
        mDisposable.add(dps)
    }
}
