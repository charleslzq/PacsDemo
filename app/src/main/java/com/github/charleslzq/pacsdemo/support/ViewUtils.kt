package com.github.charleslzq.pacsdemo.support

import android.view.View
import android.view.ViewGroup

/**
 * Created by charleslzq on 17-11-24.
 */
object ViewUtils {

    fun getAllChildren(viewGroup: ViewGroup) =
        (0 until viewGroup.childCount).map { viewGroup.getChildAt(it) }

    @Suppress("UNCHECKED_CAST")
    fun <T : View> getTypedChildren(viewGroup: ViewGroup, klass: Class<T>) =
        getAllChildren(viewGroup).filter { it::class.java == klass }.map { it as T }
}