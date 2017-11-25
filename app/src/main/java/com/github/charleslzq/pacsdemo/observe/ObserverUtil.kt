package com.github.charleslzq.pacsdemo.observe

import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

/**
 * Created by charleslzq on 17-11-25.
 */
object ObserverUtil {

    fun <T> register(kProperty0: KProperty0<T>, observer: (T) -> Unit) {
        val delegate = kProperty0.apply { isAccessible = true }.getDelegate()
        if (delegate != null) {
            (delegate as ObservablePropertyWithObservers<T>).register(observer)
        }
    }

}