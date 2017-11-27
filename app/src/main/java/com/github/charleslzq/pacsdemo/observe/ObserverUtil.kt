package com.github.charleslzq.pacsdemo.observe

import java.util.*
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

/**
 * Created by charleslzq on 17-11-25.
 */
object ObserverUtil {

    fun <T> registerObserver(kProperty0: KProperty0<T>, observer: (T, T) -> Unit, name: String = UUID.randomUUID().toString()) {
        val delegate = kProperty0.apply { isAccessible = true }.getDelegate()
        if (delegate != null) {
            (delegate as ObservablePropertyWithObservers<T>).registerObserver(observer, name)
        }
    }

    fun removeObserver(kProperty0: KProperty0<*>, name: String) {
        val delegate = kProperty0.apply { isAccessible = true }.getDelegate()
        if (delegate != null) {
            (delegate as ObservablePropertyWithObservers<*>).removeObserver(name)
        }
    }

}