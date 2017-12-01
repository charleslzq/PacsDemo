package com.github.charleslzq.pacsdemo.component.observe

import io.reactivex.subjects.PublishSubject
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

/**
 * Created by charleslzq on 17-11-25.
 */
class ObservableStatus<T>(
        initialValue: T
) : ObservableProperty<T>(initialValue) {
    private val publisher = PublishSubject.create<Pair<T, T>>()

    fun onChange(handler: (Pair<T, T>) -> Unit) {
        publisher.subscribe(handler)
    }

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        publisher.onNext(oldValue to newValue)
    }

    companion object {
        fun <T> getDelegate(kProperty0: KProperty0<T>): ObservableStatus<T>? {
            val delegate = kProperty0.apply { isAccessible = true }.getDelegate()
            return if (delegate != null && delegate is ObservableStatus<*>) {
                @Suppress("UNCHECKED_CAST")
                delegate as ObservableStatus<T>
            } else {
                null
            }
        }
    }
}