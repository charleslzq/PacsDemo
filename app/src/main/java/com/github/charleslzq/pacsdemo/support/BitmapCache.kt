package com.github.charleslzq.pacsdemo.support

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.net.URI

/**
 * Created by charleslzq on 17-12-20.
 */
class BitmapCache(size: Int = 100) {
    private val cache = MemCache(Bitmap::class.java, size)

    fun load(uri: URI) = cache.load(uri.toString()) {
        decode(uri)
    }

    fun preload(vararg uris: URI) = uris.forEach { load(it) }

    companion object {
        fun decode(uri: URI) = try {
            BitmapFactory.decodeFile(File(uri).absolutePath, BitmapFactory.Options().apply {
                inMutable = true
            })
        } catch (ex: Throwable) {
            null
        }
    }
}