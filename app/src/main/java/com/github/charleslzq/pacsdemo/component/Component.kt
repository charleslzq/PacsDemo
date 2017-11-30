package com.github.charleslzq.pacsdemo.component

import android.view.View
import com.github.charleslzq.pacsdemo.observe.ObservablePropertyWithObservers
import com.github.charleslzq.pacsdemo.observe.ObserverUtil
import com.github.charleslzq.pacsdemo.observe.ObserverUtil.registerObserver
import kotlin.reflect.KProperty0

/**
 * Created by charleslzq on 17-11-27.
 */
abstract class Component<out V, D>(
        val view: V,
        private val initialState: () -> D
) where V : View {
    var state: D by ObservablePropertyWithObservers(initialState())
    private val monitoredProperties: MutableSet<KProperty0<*>> = emptySet<KProperty0<*>>().toMutableSet()

    fun onNewState(handler: (Pair<D, D>) -> Unit) {
        handler(state to state)
        registerObserver(this::state, { oldModel, newModel ->
            if (oldModel != newModel) {
                clearOldStateMonitor()
                handler(oldModel to newModel)
            }
        })
    }

    fun <T> onStateChange(kProperty0: KProperty0<T>, handler: (Pair<T, T>) -> Unit) {
        monitoredProperties.add(kProperty0)
        handler(kProperty0.get() to kProperty0.get())
        registerObserver(kProperty0, { old, new -> if (old != new) handler(old to new) }, this::class.java.name)
    }

    fun <T> isInit(pair: Pair<T, T>) = pair.first == pair.second

    fun reset() {
        state = initialState()
    }

    private fun clearOldStateMonitor() {
        monitoredProperties.forEach {
            ObserverUtil.removeObserver(it, this::class.java.name)
        }
    }
}