package com.github.charleslzq.pacsdemo.component.store

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.github.charleslzq.pacsdemo.support.MemCache
import com.github.charleslzq.pacsdemo.support.RxScheduleSupport
import java.io.File
import java.net.URI

/**
 * Created by charleslzq on 17-12-20.
 */
class BitmapCache(size: Int = 10, private val useBigImageCache: Boolean = true) : RxScheduleSupport {
    private val cache = MemCache(Bitmap::class.java, size, { !useBigImageCache || it.byteCount <= ONE_MB })

    init {
        if (!useBigImageCache) {
            bigImageCache.clear()
        }
    }

    fun load(uri: URI): Bitmap? {
        return cache.load(uri.toString()) {
            if (useBigImageCache) {
                bigImageCache.load(uri.toString()) {
                    decode(uri)
                }
            } else {
                decode(uri)
            }
        }
    }

    fun preload(vararg uris: URI) {
        runOnIo {
            uris.forEach { load(it) }
        }
    }

    private fun decode(uri: URI): Bitmap? {
        return callOnIo {
            try {
                BitmapFactory.decodeFile(File(uri).absolutePath, BitmapFactory.Options().apply {
                    inMutable = true
                })
            } catch (ex: Throwable) {
                null
            }
        }
    }

    companion object {
        private val ONE_MB = 1024 * 1024 * 8
        private val bigImageCache = MemCache(Bitmap::class.java, 10, { it.byteCount > ONE_MB })
    }
}