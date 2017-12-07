package com.github.charleslzq.pacsdemo.component.base

import com.github.charleslzq.pacsdemo.component.event.Event
import com.github.charleslzq.pacsdemo.component.event.EventBus
import kotlin.reflect.KMutableProperty0

/**
 * Created by charleslzq on 17-12-7.
 */
interface WithReducer {
    fun <P> reduce(kProperty: KMutableProperty0<P>, predicate: () -> Boolean = { true }, busName: String = EventBus.DEFAULT,handler: (Pair<P, Event>) -> P) {
        EventBus.onEvent<Event>(busName) {
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