package com.devcode940.web.entity.bo

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 缓存中对应的页面信息
 */
@Parcelize
data class TabInfo(
    var tag: String? = null,
    var title: String? = null,
    var uri: Uri? = null
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (tag == null || other == null) return false
        if (other is TabInfo) return tag == other.tag
        return false
    }

    override fun hashCode(): Int = tag?.hashCode() ?: 0

    /** Convenience: the page URL as a string (derived from [uri]). */
    val url: String?
        get() = uri?.toString()

    companion object {
        @JvmStatic
        fun create(tag: String, title: String?): TabInfo = TabInfo(tag = tag, title = title)

        @JvmStatic
        fun create(tag: String, title: String?, uri: Uri?): TabInfo =
            TabInfo(tag = tag, title = title, uri = uri)
    }
}
