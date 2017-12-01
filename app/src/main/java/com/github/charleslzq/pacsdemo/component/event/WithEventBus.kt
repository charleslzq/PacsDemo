package com.github.charleslzq.pacsdemo.component.event

import org.greenrobot.eventbus.EventBus

/**
 * Created by charleslzq on 17-12-1.
 */
interface WithEventBus {
    fun register(eventBus: EventBus = EventBus.getDefault()) {
        eventBus.register(this)
    }

    fun unregister(eventBus: EventBus = EventBus.getDefault()) {
        eventBus.unregister(this)
    }

    fun post(event: Any, eventBus: EventBus = EventBus.getDefault()) {
        eventBus.post(event)
    }
}