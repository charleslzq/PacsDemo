package com.github.charleslzq.pacsdemo.support

import android.util.Log
import com.github.charleslzq.kotlin.react.ObservableStatus
import kotlin.reflect.KProperty

/**
 * ObservableStatus的调试日志filter,用于打印其值的变化
 */
fun <T> debugLog(
    name: (KProperty<T>) -> String = { it.toString() },
    stringify: (T) -> String = { it.toString() }
) = ObservableStatus.buildFilter<T> {
    Log.d(
        "StatusDebug",
        "Property ${name(valueChange.first)} changed from ${stringify(valueChange.second)} to ${stringify(
            valueChange.third
        )}"
    )
    next(valueChange)
}