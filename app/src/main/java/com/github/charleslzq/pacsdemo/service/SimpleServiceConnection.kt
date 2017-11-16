package com.github.charleslzq.pacsdemo.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log

/**
 * Created by charleslzq on 17-11-16.
 */
class SimpleServiceConnection<T>(
        private val setter: (T?) -> Unit
): ServiceConnection {
    private val logTag = this.javaClass.name

    override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
        setter.invoke(binder as T)
        Log.i(logTag, "$componentName connected")
    }

    override fun onServiceDisconnected(componentName: ComponentName?) {
        setter.invoke(null)
        Log.i(logTag, "$componentName disconnected")
    }
}