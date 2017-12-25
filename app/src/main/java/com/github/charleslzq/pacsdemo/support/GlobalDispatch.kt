package com.github.charleslzq.pacsdemo.support

import android.util.Log
import com.github.charleslzq.kotlin.react.EventDispatcher.buildDispatcher
import com.github.charleslzq.kotlin.react.EventDispatcher.buildMiddleWare
import com.github.charleslzq.pacsdemo.BuildConfig

/**
 * Created by charleslzq on 17-12-25.
 */
object GlobalDispatch {
    val DEBUG_LOG = buildMiddleWare {
        val logTag = "Event Debug"
        if (BuildConfig.DEBUG) {
            Log.d(logTag, "Got $event")
            next(event)
            Log.d(logTag, "handled $event")
        } else {
            next(event)
        }
    }
    val DEBUG_DISPATCH = buildDispatcher(DEBUG_LOG)
}