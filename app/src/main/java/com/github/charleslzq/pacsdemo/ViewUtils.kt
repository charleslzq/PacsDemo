package com.github.charleslzq.pacsdemo

import android.view.View
import android.view.ViewGroup

/**
 * Created by charleslzq on 17-11-24.
 */
object ViewUtils {

    fun getAllChildren(viewGroup: ViewGroup): List<View> {
        return (0..(viewGroup.childCount - 1)).map { viewGroup.getChildAt(it) }
    }

    fun <T : View> getTypedChildren(viewGroup: ViewGroup, klass: Class<T>): List<T> {
        return getAllChildren(viewGroup).filter { it::class.java == klass }.map { it as T }
    }
}