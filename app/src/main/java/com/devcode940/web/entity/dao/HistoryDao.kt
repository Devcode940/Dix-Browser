package com.devcode940.web.entity.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {

    @Query("SELECT COUNT(*) FROM history")
    fun count(): Long

    @Query("SELECT * FROM history")
    fun getAll(): List<History>

    @Query("SELECT * FROM history LIMIT ((:pageNo - 1) * :pageSize), :pageSize")
    fun getHistory(pageNo: Int, pageSize: Int): List<History>

    @Insert
    fun insertHistory(history: History): Long
}
