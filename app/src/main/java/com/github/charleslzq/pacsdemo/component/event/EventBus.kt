package com.github.charleslzq.pacsdemo.component.event

import android.util.Log
import io.reactivex.subjects.PublishSubject

/**
 * Created by charleslzq on 17-12-1.
 */
object EventBus {
    val DEFAULT = "DEFAULT"
    val registry = mutableMapOf<String, MutableList<PublishSubject<Any>>>()

    fun post(event: Any, name: String = DEFAULT) {
        registry[name]?.forEach { it.onNext(event) }
    }

    inline fun <reified T> castEvent(event: Any): T? {
        return when (event is T) {
            true -> event as T
            false -> null
        }
    }

    inline fun <reified T> onEvent(busName: String = DEFAULT, crossinline handler: (T) -> Unit) {
        val publisher = PublishSubject.create<Any>()
        publisher.subscribe {
            castEvent<T>(it)?.apply { handler(this) }
        }
        if (!registry.containsKey(busName)) {
            val logger = PublishSubject.create<Any>()
            logger.subscribe {
                Log.d("EventBus $busName", "${it.javaClass.simpleName} received")
            }
            registry[busName] = mutableListOf(logger)
        }
        registry[busName]?.add(publisher)
    }
}

interface Event