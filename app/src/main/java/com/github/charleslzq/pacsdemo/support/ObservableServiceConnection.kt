package com.github.charleslzq.pacsdemo.support

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import io.reactivex.Observable

/**
 * Created by charleslzq on 17-12-20.
 */
class ObservableServiceConnection<T>(
        private val onConnect: (Observable<T>) -> Unit,
        private val onDisConnect: () -> Unit
) : ServiceConnection {
    private val logTag = this.javaClass.name

    override fun onServiceDisconnected(componentName: ComponentName) {
        onDisConnect()
        Log.i(logTag, "$componentName disconnected")
    }

    override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
        @Suppress("UNCHECKED_CAST")
        onConnect((service as ObservableService<T>).get())
        Log.i(logTag, "$componentName connected")
    }
}