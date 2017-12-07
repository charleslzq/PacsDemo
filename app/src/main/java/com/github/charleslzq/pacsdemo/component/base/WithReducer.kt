package com.github.charleslzq.pacsdemo.component.base

import com.github.charleslzq.pacsdemo.component.event.Event
import com.github.charleslzq.pacsdemo.component.event.EventBus
import kotlin.reflect.KMutableProperty0

/**
 * Created by charleslzq on 17-12-7.
 */
interface WithReducer {
    fun <P> reduce(kProperty: KMutableProperty0<P>, predicate: () -> Boolean = { true }, handler: (Pair<P, Event>) -> P) {
        EventBus.onEvent<Event> {
            if (predicate()) {
                val rawValue = kProperty.get()
                val newValue = handler(rawValue to it)
                if (rawValue != newValue) {
                    kProperty.set(newValue)
                }
            }
        }
    }
}