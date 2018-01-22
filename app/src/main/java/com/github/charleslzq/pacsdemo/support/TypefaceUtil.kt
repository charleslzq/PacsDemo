package com.github.charleslzq.pacsdemo.support

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView

/**
 * Created by charleslzq on 17-12-6.
 */
object TypefaceUtil {
    const val FONT_AWESOME = "fontawesome-webfont.ttf"
    private val registry = mutableMapOf<String, Typeface>()

    fun configureTextView(path: String, vararg textView: TextView) {
        textView.forEach { it.typeface = getTypeFace(it.context, path) }
    }

    private fun getTypeFace(context: Context, path: String): Typeface {
        if (!registry.containsKey(path)) {
            load(context, path)
        }
        return registry[path]!!
    }

    private fun load(context: Context, path: String) =
        registry.put(path, Typeface.createFromAsset(context.assets, path))
}