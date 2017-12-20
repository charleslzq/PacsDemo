package com.github.charleslzq.pacsdemo.component.store

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.github.charleslzq.pacsdemo.support.CacheUtil
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.net.URI

/**
 * Created by charleslzq on 17-12-20.
 */
class BitmapCache(
        private val position: Int,
        size: Int = 10
) {
    init {
        CacheUtil.create(getCacheName(), Bitmap::class.java, size)
    }

    fun load(uri: URI): Bitmap? {
        return CacheUtil.cache(getCacheName(), Bitmap::class.java, uri.toString()) {
            decode(uri)
        }
    }

    fun preload(vararg uris: URI) {
        Observable.fromArray(*uris).observeOn(Schedulers.io()).forEach { load(it) }
    }

    private fun decode(uri: URI): Bitmap? {
        return BitmapFactory.decodeFile(File(uri).absolutePath, BitmapFactory.Options().apply {
            inMutable = true
        })
    }

    private fun getCacheName() = CACHE_PREFIX + position.toString()

    companion object {
        val CACHE_PREFIX = "Bitmap#"
    }
}