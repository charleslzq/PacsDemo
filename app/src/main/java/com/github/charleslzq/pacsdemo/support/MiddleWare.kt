package com.github.charleslzq.pacsdemo.support

import android.util.Log
import com.github.charleslzq.kotlin.react.Store

/**
 * Created by charleslzq on 17-12-28.
 */
object MiddleWare {
    /**
     * store的日志中间件,记录处理的事件
     */
    val debugLog = Store.buildMiddleWare {
        val logTag = "MiddleWareDebug"
        Log.d(logTag, "handle $event")
        next(event)
        Log.d(logTag, "complete $event")
    }
}