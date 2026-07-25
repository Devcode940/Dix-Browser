package com.devcode940.web.entity.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WebSiteDao {

    @Query("SELECT COUNT(*) FROM website")
    fun count(): Long

    @Query("SELECT * FROM website")
    fun getAll(): List<WebSite>

    @Insert
    fun insertWebSite(webSite: WebSite)

    @Insert
    fun insertAllWebSite(vararg webSites: WebSite)
}
