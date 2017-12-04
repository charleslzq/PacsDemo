package com.github.charleslzq.pacsdemo.component.event

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by charleslzq on 17-12-1.
 */
object EventBus {
    private val registry = mutableMapOf<String, PublishSubject<Any>>()

    fun get(name: String = "DEFAULT"): Observable<Any> {
        return registry.getOrDefault(name, registerNew(name))
    }

    fun send(event: Any, name: String = "DEFAULT") {
        if (!registry.containsKey(name)) {
            registerNew(name)
        }
        registry[name]?.onNext(event)
    }

    private fun registerNew(name: String): PublishSubject<Any> {
        val bus = PublishSubject.create<Any>()
        registry.put(name, bus)
        return bus
    }

    inline fun <reified T> castEvent(event: Any): T? {
        return when (event is T) {
            true -> event as T
            false -> null
        }
    }

    inline fun <reified T> onEvent(busName: String = "DEFAULT", crossinline handler: (T) -> Unit) {
        get(busName).subscribe({
            castEvent<T>(it)?.apply(handler)
        })
    }
}