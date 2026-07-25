package com.devcode940.web.page.frontpage

import android.content.Context
import com.devcode940.web.EasyApplication
import com.devcode940.web.contract.IFrontPage
import com.devcode940.web.entity.dao.AppDatabase
import com.devcode940.web.entity.dao.DaoManager
import com.devcode940.web.entity.dao.WebSite
import com.devcode940.web.utils.SharedPreferencesUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FrontPagePresenterImpl(
    context: Context,
    private val view: IFrontPage.View
) : IFrontPage.Presenter {

    private val appContext: Context = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun getWebSite() {
        scope.launch {
            try {
                val sp = SharedPreferencesUtils.getSettingSP(appContext) ?: return@launch
                val app = appContext as EasyApplication
                val db: AppDatabase = app.getAppDatabase()

                val firstBoot = sp.getBoolean(SharedPreferencesUtils.KEY_FIRST_BOOT, true)
                val siteListCreated = sp.getBoolean(SharedPreferencesUtils.KEY_SITE_LIST_CREATED, false)
                if (firstBoot && !siteListCreated) {
                    withContext(Dispatchers.IO) { DaoManager.createDefaultSiteList(db) }
                    sp.edit().putBoolean(SharedPreferencesUtils.KEY_SITE_LIST_CREATED, true).apply()
                }

                val result: List<WebSite> = withContext(Dispatchers.IO) { db.webSiteDao().all }
                view.showWebSite(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
