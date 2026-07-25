package com.devcode940.web.entity.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WebSite(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    var siteName: String? = null,
    var siteUrl: String? = null
)
