package com.devcode940.web.entity.dao

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [History::class, WebSite::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    abstract fun webSiteDao(): WebSiteDao
}
