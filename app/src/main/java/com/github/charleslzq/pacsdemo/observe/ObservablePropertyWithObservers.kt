package com.github.charleslzq.pacsdemo.observe

import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

/**
 * Created by charleslzq on 17-11-25.
 */
class ObservablePropertyWithObservers<T>(
        initialValue: T
): ObservableProperty<T>(initialValue), WithObservers<(T)->Unit> {
    private val observerMap: MutableMap<String, (T) -> Unit> = emptyMap<String, (T) -> Unit>().toMutableMap()

    override fun registerObserver(observer: (T) -> Unit, name: String) {
        observerMap[name] = observer
    }

    override fun removeObserver(name: String) {
        observerMap.remove(name)
    }

    override fun clearObservers() {
        observerMap.clear()
    }

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        if (oldValue != newValue) {
            observerMap.values.forEach { it(newValue) }
        }
    }
}