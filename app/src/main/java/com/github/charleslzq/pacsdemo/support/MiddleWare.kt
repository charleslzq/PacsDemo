package com.github.charleslzq.pacsdemo.support

import android.util.Log
import com.github.charleslzq.kotlin.react.Store

/**
 * Created by charleslzq on 17-12-28.
 */
object MiddleWare {
    val debugLog = Store.buildMiddleWare {
        val logTag = "MiddleWareDebug"
        Log.d(logTag, "handle $event")
        next(event)
        Log.d(logTag, "complete $event")
    }
}