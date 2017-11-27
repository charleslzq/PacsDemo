package com.github.charleslzq.pacsdemo.binder

import android.view.View
import com.github.charleslzq.pacsdemo.observe.ObservablePropertyWithObservers
import com.github.charleslzq.pacsdemo.observe.ObserverUtil
import com.github.charleslzq.pacsdemo.observe.ObserverUtil.registerObserver
import kotlin.reflect.KProperty0

/**
 * Created by charleslzq on 17-11-27.
 */
abstract class ViewBinder<out V, D>(
        val view: V
) where V : View {
    var model: D? by ObservablePropertyWithObservers(null)
    private val monitoredProperties: MutableSet<KProperty0<*>> = emptySet<KProperty0<*>>().toMutableSet()

    fun onNewModel(handler: (D?) -> Unit) {
        registerObserver(this::model, { oldModel, newModel ->
            if (oldModel != newModel) {
                clearOldModelMonitor()
                handler(newModel)
            }
        })
    }

    fun <T> onModelChange(kProperty0: KProperty0<T>, handler: (T, T) -> Unit) {
        monitoredProperties.add(kProperty0)
        registerObserver(kProperty0, handler, this::class.java.name)
    }

    private fun clearOldModelMonitor() {
        monitoredProperties.forEach {
            ObserverUtil.removeObserver(it, this::class.java.name)
        }
    }
}