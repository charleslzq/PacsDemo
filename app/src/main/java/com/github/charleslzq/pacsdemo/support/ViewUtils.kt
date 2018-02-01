package com.github.charleslzq.pacsdemo.support

import android.view.View
import android.view.ViewGroup

/**
 * 获取一个ViewGroup的所有子View
 */
fun getAllChildren(viewGroup: ViewGroup) =
    (0 until viewGroup.childCount).map { viewGroup.getChildAt(it) }

/**
 * 获取一个ViewGroup的类型为klass的所有子View
 */
@Suppress("UNCHECKED_CAST")
fun <T : View> getTypedChildren(viewGroup: ViewGroup, klass: Class<T>) =
    getAllChildren(viewGroup).filter { it::class.java == klass }.map { it as T }