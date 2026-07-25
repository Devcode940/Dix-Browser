package com.devcode940.web.entity.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class History(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    var title: String? = null,
    var url: String? = null,
    var time: Long = 0
)
