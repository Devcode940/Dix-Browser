package com.devcode940.web.entity.dao

object DaoManager {

    @JvmStatic
    fun createDefaultSiteList(db: AppDatabase) {
        try {
            val sites = arrayOf(
                WebSite(null, "Baidu", "https://www.baidu.com"),
                WebSite(null, "Bing", "https://bing.com"),
                WebSite(null, "QQ", "https://www.qq.com"),
                WebSite(null, "网易", "https://www.163.com"),
                WebSite(null, "掘金", "https://juejin.im"),
                WebSite(null, "快科技", "https://mydrivers.com"),
                WebSite(null, "V2ex", "https://v2ex.com"),
                WebSite(null, "36Kr", "https://36kr.com")
            )
            db.webSiteDao().insertAllWebSite(*sites)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
