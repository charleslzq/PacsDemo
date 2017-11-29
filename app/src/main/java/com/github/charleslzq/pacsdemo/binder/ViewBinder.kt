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
        val view: V,
        private val initialModel: () -> D
) where V : View {
    var model: D by ObservablePropertyWithObservers(initialModel())
    private val monitoredProperties: MutableSet<KProperty0<*>> = emptySet<KProperty0<*>>().toMutableSet()

    fun onNewModel(handler: (Pair<D, D>) -> Unit) {
        handler(model to model)
        registerObserver(this::model, { oldModel, newModel ->
            if (oldModel != newModel) {
                clearOldModelMonitor()
                handler(oldModel to newModel)
            }
        })
    }

    fun <T> onModelChange(kProperty0: KProperty0<T>, handler: (Pair<T, T>) -> Unit) {
        monitoredProperties.add(kProperty0)
        handler(kProperty0.get() to kProperty0.get())
        registerObserver(kProperty0, { old, new -> if (old != new) handler(old to new) }, this::class.java.name)
    }

    fun <T> isInit(pair: Pair<T, T>) = pair.first == pair.second

    fun reset() {
        model = initialModel()
    }

    private fun clearOldModelMonitor() {
        monitoredProperties.forEach {
            ObserverUtil.removeObserver(it, this::class.java.name)
        }
    }
}