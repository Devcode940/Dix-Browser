package com.devcode940.web.utils

object StringUtils {

    fun isEmpty(str: String?): Boolean = str == null || str.isEmpty()

    fun isNotEmpty(str: String?): Boolean = !isEmpty(str)

    fun isValidUrl(url: String?): Boolean {
        if (url.isNullOrEmpty()) return false
        return url.startsWith("http://") ||
            url.startsWith("https://") ||
            url.startsWith("ftp://") ||
            url.startsWith("file://") ||
            url == "about:blank"
    }
}
