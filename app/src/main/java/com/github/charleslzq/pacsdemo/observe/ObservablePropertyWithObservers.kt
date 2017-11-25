package com.github.charleslzq.pacsdemo.observe

import java.util.*
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

/**
 * Created by charleslzq on 17-11-25.
 */
class ObservablePropertyWithObservers<T>(
        initialValue: T
): ObservableProperty<T>(initialValue) {
    private val observerMap: MutableMap<String, (T) -> Unit> = emptyMap<String, (T) -> Unit>().toMutableMap()

    fun register(observer: (T) -> Unit, name: String = UUID.randomUUID().toString()) {
        observerMap[name] = observer
    }

    fun remove(name: String) {
        observerMap.remove(name)
    }

    fun clear() {
        observerMap.clear()
    }

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        observerMap.values.forEach { it(newValue) }
    }
}