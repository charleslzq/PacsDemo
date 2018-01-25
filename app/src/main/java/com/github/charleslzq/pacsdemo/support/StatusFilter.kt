package com.github.charleslzq.pacsdemo.support

import android.util.Log
import com.github.charleslzq.kotlin.react.ObservableStatus

/**
 * Created by charleslzq on 18-1-15.
 */
object StatusFilter {
    /**
     * ObservableStatus的调试日志filter,用于打印其值的变化
     */
    fun <T> debugLog() = ObservableStatus.buildFilter<T> {
        Log.d(
            "StatusDebug",
            "Property ${valueChange.first} changed from ${valueChange.second} to ${valueChange.third}"
        )
        next(valueChange)
    }
}