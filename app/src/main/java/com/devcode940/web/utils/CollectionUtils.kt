package com.devcode940.web.utils

object CollectionUtils {

    fun isEmpty(collection: Collection<*>?): Boolean = collection == null || collection.isEmpty()

    fun isNotEmpty(collection: Collection<*>?): Boolean = !isEmpty(collection)
}
