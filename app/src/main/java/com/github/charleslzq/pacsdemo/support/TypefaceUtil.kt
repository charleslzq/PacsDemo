package com.github.charleslzq.pacsdemo.support

import android.content.Context
import android.graphics.Typeface

/**
 * Created by charleslzq on 17-12-6.
 */
object TypefaceUtil {
    val fontAwesome = "fontawesome-webfont.ttf"
    private val registry = mutableMapOf<String, Typeface>()

    fun getTypeFace(context: Context, path: String): Typeface {
        if (!registry.containsKey(path)) {
            load(context, path)
        }
        return registry[path]!!
    }

    private fun load(context: Context, path: String) {
        registry.put(path, Typeface.createFromAsset(context.assets, path))
    }
}