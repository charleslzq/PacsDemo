package com.github.charleslzq.pacsdemo.support

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.net.URI

/**
 * Created by charleslzq on 17-12-20.
 * 图像缓存, 默认大小100
 */
class BitmapCache(size: Int = 100) {
    private val cache = MemCache(Bitmap::class.java, size)

    /**
     * 根据uri获取图像,未命中时从本地加载
     * @param uri 文件uri
     * @return 图像文件(可能为空)
     */
    fun load(uri: URI) = cache.load(uri.toString()) {
        decode(uri)
    }

    /**
     * 预先加载图像文件到缓存
     * @param uris 预先加载的文件uri列表
     */
    fun preload(vararg uris: URI) = uris.forEach { load(it) }

    companion object {
        /**
         * 根据文件uri解析本地图像文件
         * @param uri 文件uri
         * @return 图像文件(可能为空)
         */
        fun decode(uri: URI) = try {
            BitmapFactory.decodeFile(File(uri).absolutePath, BitmapFactory.Options().apply {
                inMutable = true
            })
        } catch (ex: Throwable) {
            null
        }
    }
}