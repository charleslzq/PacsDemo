package com.github.charleslzq.pacsdemo.component.event

import android.util.Log
import io.reactivex.subjects.PublishSubject

/**
 * Created by charleslzq on 17-12-1.
 */
object EventBus {
    val DEFAULT = "DEFAULT"
    private val registry = mutableMapOf<String, PublishSubject<Any>>()
    val handlers = mutableMapOf<String, MutableList<(Any) -> Unit>>()

    fun post(event: Any, name: String = DEFAULT) {
        if (!registry.containsKey(name)) {
            registerNew(name)
        }
        registry[name]?.onNext(event)
    }

    private fun registerNew(name: String): PublishSubject<Any> {
        val bus = PublishSubject.create<Any>()
        registry.put(name, bus)
        bus.subscribe { event ->
            Log.d("EventBus", "${event.javaClass.name} received")
            handlers.getOrDefault(name, mutableListOf()).forEach {
                it(event)
            }
        }
        return bus
    }

    inline fun <reified T> castEvent(event: Any): T? {
        return when (event is T) {
            true -> event as T
            false -> null
        }
    }

    inline fun <reified T> onEvent(busName: String = DEFAULT, crossinline handler: (T) -> Unit) {
        val existHandlers = handlers.getOrDefault(busName, mutableListOf())
        existHandlers.add({
            castEvent<T>(it)?.apply { handler(this) }
        })
        handlers[busName] = existHandlers
    }
}

interface Event