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
    private val monitoredState: MutableSet<KProperty0<*>> = mutableSetOf()

    fun onNewState(ignoreUnchanged: Boolean = true, handler: (Triple<D, D, Boolean>) -> Unit) {
        handler(Triple(state, state, true))
        registerObserver(this::state, { oldModel, newModel ->
            if (ignoreUnchanged && oldModel != newModel) {
                clearOldStateMonitor()
                handler(Triple(oldModel, newModel, false))
            }
        })
    }

    fun <T> onStateChange(kProperty0: KProperty0<T>, ignoreUnchanged: Boolean = true,handler: (Triple<T, T, Boolean>) -> Unit) {
        monitoredState.add(kProperty0)
        handler(Triple(kProperty0.get(), kProperty0.get(), true))
        registerObserver(kProperty0, { old, new -> if (ignoreUnchanged && old != new) handler(Triple(old, new, false)) }, this.hashCode().toString())
    }

    fun reset() {
        state = initialState()
    }

    private fun clearOldStateMonitor() {
        monitoredState.forEach {
            ObserverUtil.removeObserver(it, this.hashCode().toString())
        }
    }
}