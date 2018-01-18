package com.github.charleslzq.pacsdemo.support

import android.util.LruCache

/**
 * Created by charleslzq on 17-12-22.
 */
class MemCache<T>(
    private val storageType: Class<T>,
    size: Int,
    private val updateWhen: (T) -> Boolean = { true },
    private val keyGenerator: (Array<String>) -> String = { it.joinToString("#") }
) {
    private val cache = LruCache<String, T>(size)

    fun load(vararg args: String, onMiss: (Array<String>) -> T?): T? {
        val arguments = arrayOf(*args)
        val objectKey = keyGenerator(arguments)
        val objectInCache = storageType.cast(cache[objectKey])
        return objectInCache ?: onMiss(arguments)?.also {
            if (updateWhen(it)) {
                cache.put(objectKey, it)
            }
        }
    }

    fun clear() = cache.evictAll()
}