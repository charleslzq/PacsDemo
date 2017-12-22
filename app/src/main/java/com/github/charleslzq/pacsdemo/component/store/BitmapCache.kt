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
class BitmapCache(size: Int = 10) : RxScheduleSupport {
    private val cache = MemCache(Bitmap::class.java, size)

    fun load(uri: URI): Bitmap? {
        return bigImageCache.load(uri.toString()) {
            cache.load(args = uri.toString(), autoUpdate = false) {
                decode(uri)
            }
        }?.also {
            if (it.byteCount < ONE_MB) {
                cache.save(args = uri.toString(), data = it)
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